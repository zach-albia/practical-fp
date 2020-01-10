package practical.fp

import java.io.File

import zio.console._
import zio.{ Ref, UIO, ZIO }

import scala.collection.immutable.TreeSet

object ScalaMain extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _      <- putStr("Enter a file path: ")
      path   <- getStrLn
      _      <- putStr("Enter number of top files by file size: ")
      number <- getStrLn.map(_.toInt)
      files  <- topNFiles(path, number)
      _      <- putStrLn(files.map(_.getAbsolutePath).mkString("\n"))
    } yield ()).foldM(
      e => putStrLn(e.getMessage) *> ZIO.succeed(0),
      (_: Any) => ZIO.succeed(1)
    )

  def topNFiles(str: String, n: Int): UIO[TreeSet[File]] =
    for {
      set   <- Ref.make(TreeSet()(Ordering.fromLessThan[File](_.length < _.length)))
      root  <- ZIO.effectTotal(new File(str))
      _     <- doTopNFiles(root, n, set)
      files <- set.get
    } yield files

  private def doTopNFiles(root: File, n: Int, ref: Ref[TreeSet[File]]): UIO[Unit] =
    if (root.exists) {
      if (root.isFile) updateAndBind(root, n, ref)
      else {
        ZIO.sequence(root.listFiles.toList.map(subPath => {
          if (subPath.isFile) updateAndBind(subPath, n, ref)
          else doTopNFiles(subPath, n, ref)
        })).unit
      }
    } else ZIO.succeed(Nil)

  private def updateAndBind(root: File, n: Int, set: Ref[TreeSet[File]]) =
    for {
      files <- set.update(_ + root)
      _     <- if (files.size > n) set.update(_.drop(1)) else ZIO.unit
    } yield ()
}
