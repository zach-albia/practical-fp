package practical.fp

import java.io.{ File => JFile }

import zio.blocking._
import zio.console._
import zio.{ App, Ref, ZEnv, ZIO }

import scala.collection.immutable.TreeSet

object ScalaMain extends App {

  sealed trait FileRead
  final case class File(file: JFile)                extends FileRead
  final case class Dir(files: Option[Array[JFile]]) extends FileRead
  final case object NoRead                          extends FileRead

  trait Env {
    val config: Env.Config
  }

  object Env {
    case class Config(n: Int, topNFiles: Ref[TreeSet[JFile]])
  }

  type FileRIO[R <: Blocking, A] = ZIO[R, SecurityException, A]
  type FileIO[A]                 = FileRIO[Blocking, A]
  type EnvFileIO[A]              = FileRIO[Env with Blocking, A]

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      _      <- putStr("Enter a file path: ")
      path   <- getStrLn
      _      <- putStr("Enter number of top files by file size: ")
      number <- getStrLn.map(_.toInt).refineToOrDie[NumberFormatException]
      files  <- topNFiles(path, number)
      _      <- ZIO.foreach_(files)(file => putStrLn(file.getAbsolutePath))
    } yield ()).foldM(
      e => putStrLn(e.getMessage) *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )

  def topNFiles(pathStr: String, n: Int): FileIO[Iterable[JFile]] =
    for {
      topNFiles <- Ref.make(TreeSet[JFile]()(Ordering.by(_.length())))
      filePath  <- read(new JFile(pathStr))
      _ <- doTopNFiles(filePath).provideSome[Blocking] { base =>
            new Env with Blocking {
              val config   = Env.Config(n, topNFiles)
              val blocking = base.blocking
            }
          }
      files <- topNFiles.get
    } yield files

  def read(file: JFile): FileIO[FileRead] =
    fileEffect(file.exists).flatMap { fileExists =>
      if (fileExists) {
        for {
          isFile      <- fileEffect(file.isFile)
          isDirectory <- fileEffect(file.isDirectory)
          filePath <- if (isFile)
                       ZIO.succeed(File(file))
                     else if (isDirectory)
                       fileEffect(file.listFiles())
                         .map(files => Dir(Option(files)))
                     else ZIO.succeed(NoRead)
        } yield filePath
      } else ZIO.succeed(NoRead)
    }

  private def fileEffect[A](a: A): FileIO[A] =
    blocking(ZIO.effect(a).refineToOrDie[SecurityException])

  private def doTopNFiles(fileRead: FileRead): EnvFileIO[Unit] =
    fileRead match {
      case File(file) =>
        updateAndBound(file)
      case Dir(files) =>
        files.fold[EnvFileIO[Unit]](ZIO.unit)(
          files => ZIO.foreach_(files)(read(_).flatMap(doTopNFiles))
        )
      case NoRead =>
        ZIO.unit
    }

  private def updateAndBound(root: JFile): ZIO[Env, Nothing, Unit] =
    for {
      env            <- ZIO.environment[Env]
      (topNFiles, n) = (env.config.topNFiles, env.config.n)
      _ <- topNFiles.update { topNFiles =>
            val newFiles = topNFiles + root
            if (newFiles.size > n)
              newFiles.drop(1)
            else newFiles
          }
    } yield ()
}
