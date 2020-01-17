package practical.fp

import java.io.{ File => JFile }

import practical.fp.FileRead._
import zio.blocking.{ effectBlocking, Blocking }
import zio.{ Ref, URIO, ZIO }

import scala.collection.immutable.TreeSet

object TopNFilesFP {

  trait Env {
    val config: Env.Config
  }

  object Env {
    case class Config(n: Int, topNFiles: Ref[TreeSet[JFile]])
  }

  type FileRIO[R <: Blocking, A] = ZIO[R, SecurityException, A]
  type FileIO[A]                 = FileRIO[Blocking, A]
  type EnvFileIO[A]              = FileRIO[Env with Blocking, A]

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

  private def doTopNFiles(fileRead: FileRead): EnvFileIO[Unit] =
    fileRead match {
      case File(file) =>
        addTopN(file)
      case Dir(files) =>
        files.fold[EnvFileIO[Unit]](ZIO.unit)(
          files => ZIO.foreach_(files)(read(_).flatMap(doTopNFiles))
        )
      case NoRead =>
        ZIO.unit
    }

  private def read(file: JFile): FileIO[FileRead] =
    effectBlocking(
      if (file.exists) {
        if (file.isFile) {
          File(file)
        } else if (file.isDirectory)
          Dir(Option(file.listFiles()))
        else NoRead
      } else NoRead
    ).refineToOrDie[SecurityException]

  private def addTopN(root: JFile): URIO[Env, Unit] =
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
