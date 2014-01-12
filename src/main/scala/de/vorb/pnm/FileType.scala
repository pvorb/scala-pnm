package de.vorb.pnm

sealed trait FileType

object FileType {
  sealed trait ASCII extends FileType
  sealed trait Binary extends FileType

  case object BitmapASCII extends ASCII
  case object GraymapASCII extends ASCII
  case object PixmapASCII extends ASCII

  case object BitmapBinary extends Binary
  case object GraymapBinary extends Binary
  case object PixmapBinary extends Binary
}
