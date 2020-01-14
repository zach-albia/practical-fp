package practical.fp

import zio.console._
import java.io.{ File => JFile }
import zio._

import scala.collection.immutable.TreeSet

object ScalaMain extends App {

  sealed trait FilePath
  final case class File(file: JFile)                           extends FilePath
  final case class DirectoryFiles(files: Option[Array[JFile]]) extends FilePath
  final case object NonFile                                    extends FilePath

  case class Env(n: Int, topNFiles: Ref[TreeSet[JFile]])

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _      <- putStr("Enter a file path: ")
      path   <- getStrLn
      _      <- putStr("Enter number of top files by file size: ")
      number <- getStrLn.map(_.toInt).refineToOrDie[NumberFormatException]
      files  <- topNFiles(path, number)
      _      <- ZIO.sequence(files.map(file => putStrLn(file.getAbsolutePath)))
    } yield ()).foldM(
      e => putStrLn(e.getMessage) *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )

  // TODO: Fix race condition issue, STM to the rescue?
  // TODO: Use Blocking thread pool!
  def toFilePath(file: JFile): FilePath =
    if (file.exists()) {
      if (file.isFile) File(file)
      else if (file.isDirectory) {
        val files = file.listFiles()
        DirectoryFiles(Option(files))
      } else NonFile
    } else NonFile

  def topNFiles(pathStr: String, n: Int): UIO[TreeSet[JFile]] =
    for {
      topNFiles <- Ref.make(TreeSet[JFile]()(Ordering.by(_.length())))
      _         <- doTopNFiles(toFilePath(new JFile(pathStr))).provide(Env(n, topNFiles))
      files     <- topNFiles.get
    } yield files

  private def doTopNFiles(root: FilePath): URIO[Env, Unit] =
    root match {
      case File(file) =>
        updateAndBound(file)
      case DirectoryFiles(files) =>
        topNFilesDir(files)
      case NonFile =>
        ZIO.unit
    }

  private def topNFilesDir(files: Option[Array[JFile]]) =
    files.fold[URIO[Env, Unit]](ZIO.unit) { arr =>
      ZIO.sequence(arr.map((toFilePath _).andThen(topNSubPath))).unit
    }

  private def topNSubPath(filePath: FilePath) = filePath match {
    case File(file) =>
      updateAndBound(file)
    case DirectoryFiles(files) =>
      files.fold[URIO[Env, Unit]](ZIO.unit) { files =>
        val recurseTopN =
          files.map((toFilePath _).andThen(doTopNFiles))
        ZIO.sequencePar(recurseTopN).unit
      }
    case NonFile =>
      ZIO.unit
  }

  private def updateAndBound(root: JFile): ZIO[Env, Nothing, Unit] =
    for {
      env            <- ZIO.environment[Env]
      (topNFiles, n) = (env.topNFiles, env.n)
      files          <- topNFiles.update(_ + root)
      _ <- if (files.size > n) {
            topNFiles.update(_.drop(1))
          } else ZIO.unit
    } yield ()
}
