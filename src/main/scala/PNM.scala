package de
package vorb
package pnm

import java.io.File
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.io.IOException
import java.io.IOException

import impl.PNMImpl

trait PNM {
  /**
   * Reads a PNM file.
   */
  @throws(classOf[IOException])
  def read(file: File): BufferedImage

  /**
   * Writes a PNM file with the given file type.
   */
  @throws(classOf[IOException])
  def write(file: File, ft: FileType, img: BufferedImage): Unit

  /**
   * Reads a PNM input stream.
   */
  @throws(classOf[IOException])
  def read(in: InputStream): BufferedImage

  /**
   * Writes an image with the given file type to the output stream.
   */
  @throws(classOf[IOException])
  def write(out: OutputStream, ft: FileType, img: BufferedImage): Unit
}

object PNM extends PNM {
  def read(file: File): BufferedImage =
    PNMImpl.read(file)

  def write(file: File, ft: FileType, img: BufferedImage): Unit =
    PNMImpl.write(file, ft, img)

  def read(in: InputStream): BufferedImage =
    PNMImpl.read(in)

  def write(out: OutputStream, ft: FileType, img: BufferedImage): Unit =
    PNMImpl.write(out, ft, img)
}
