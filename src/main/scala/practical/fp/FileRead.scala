package practical.fp

import java.io.{ File => JFile }

sealed trait FileRead extends Serializable with Product

object FileRead {
  final case class File(file: JFile)                extends FileRead
  final case class Dir(files: Option[Array[JFile]]) extends FileRead
  final case object NoRead                          extends FileRead
}
