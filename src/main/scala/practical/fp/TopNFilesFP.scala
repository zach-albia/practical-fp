package practical.fp

import java.io.{ File => JFile }

import practical.fp.FileRead._
import zio.blocking.{ effectBlocking, Blocking }
import zio.{ Ref, URIO, ZIO }

import scala.collection.immutable.TreeSet

object TopNFilesFP {

  trait Config {
    val config: Config.Data
  }

  object Config {
    case class Data(n: Int, topNFiles: Ref[TreeSet[JFile]])
  }

  type FileRIO[R <: Blocking, A] = ZIO[R, SecurityException, A]
  type FileIO[A]                 = FileRIO[Blocking, A]
  type Env                       = Config with Blocking
  type TopNFileIO[A]             = FileRIO[Env, A]

  def topNFiles(pathStr: String, n: Int): FileIO[Iterable[JFile]] =
    for {
      topNFiles <- Ref.make(TreeSet[JFile]()(Ordering.by(_.length())))
      filePath  <- read(new JFile(pathStr))
      _ <- doTopNFiles(filePath).provideSome[Blocking] { base =>
            new Config with Blocking {
              val config   = Config.Data(n, topNFiles)
              val blocking = base.blocking
            }
          }
      files <- topNFiles.get
    } yield files

  private def doTopNFiles(fileRead: FileRead): TopNFileIO[Unit] =
    fileRead match {
      case File(file) =>
        addTopN(file)
      case Dir(files) =>
        files.fold[TopNFileIO[Unit]](ZIO.unit)(
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

  private def addTopN(file: JFile): URIO[Config, Unit] =
    for {
      env            <- ZIO.environment[Config]
      (topNFiles, n) = (env.config.topNFiles, env.config.n)
      _ <- topNFiles.update { topNFiles =>
            val newFiles = topNFiles + file
            if (newFiles.size > n)
              newFiles.drop(1)
            else newFiles
          }
    } yield ()
}
