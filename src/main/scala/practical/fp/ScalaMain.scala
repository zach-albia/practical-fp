package practical.fp

import zio.console._
import java.io.{ File => JFile }
import zio.{ Ref, UIO, ZIO }

import scala.collection.immutable.TreeSet

object ScalaMain extends zio.App {

  sealed trait FilePath
  final case class File(value: JFile)                    extends FilePath
  final case class Directory(file: Option[Array[JFile]]) extends FilePath
  final case object Other                                extends FilePath

  implicit val fileOrdering: Ordering[File] = Ordering.by(_.value.length())

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _      <- putStr("Enter a file path: ")
      path   <- getStrLn
      _      <- putStr("Enter number of top files by file size: ")
      number <- getStrLn.map(_.toInt)
      files  <- topNFiles(path, number)
      _      <- ZIO.sequence(files.map(file => putStrLn(file.value.getAbsolutePath)))
    } yield ()).foldM(
      e => putStrLn(e.getMessage) *> ZIO.succeed(0),
      (_: Any) => ZIO.succeed(0)
    )

  def parseFilePath(pathStr: String) = {
    val file = new JFile(pathStr)
    if (file.exists())
  }

  def topNFiles(pathStr: String, n: Int): UIO[TreeSet[File]] =
    for {
      set   <- Ref.make(TreeSet())
      filePath <- parseFilePath(pathStr)
      _     <- doTopNFiles(filePath, n, set)
      files <- set.get
    } yield files

  private def doTopNFiles(root: FilePath, n: Int, ref: Ref[TreeSet[File]]): UIO[Unit] = {
    val file = root.
    if (file.exists()) {
      if (file.isFile) updateAndBind(root, n, ref)
      else if (file.isDirectory) {
        val files = file.listFiles
        if (null == files) ZIO.unit
        else {
          ZIO
            .sequence(files.map(subPath => {
              val path = Path.fromJava(subPath.toPath)
              if (subPath.isFile) {
                updateAndBind(path, n, ref)
              } else if (file.isDirectory) {
                doTopNFiles(path, n, ref).fork.flatMap(_.join)
              } else {
                ZIO.unit
              }
            }))
        }.unit
      } else ZIO.unit
    } else ZIO.succeed(Nil)
  }

  private def updateAndBind(root: Path, n: Int, set: Ref[TreeSet[Path]]) =
    for {
      files <- set.update(_ + root)
      _     <- if (files.size > n) set.update(_.drop(1)) else ZIO.unit
    } yield ()
}
