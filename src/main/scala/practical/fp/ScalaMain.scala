package practical.fp

import practical.fp.TopNFilesFP.topNFiles
import zio.console._
import zio.{ App, ZEnv, ZIO }

object ScalaMain extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      _      <- putStr("Enter a file path: ")
      path   <- getStrLn
      _      <- putStr("Enter number of top files by file size: ")
      ln     <- getStrLn
      number <- ZIO.effect(ln.toInt).refineToOrDie[NumberFormatException]
      files  <- topNFiles(path, number)
      _      <- ZIO.foreach_(files)(file => putStrLn(file.getAbsolutePath))
    } yield ()).foldM(
      e => putStrLn(e.getMessage) *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )
}
