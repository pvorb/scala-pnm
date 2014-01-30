package de
package vorb
package pnm
package impl

import java.awt.image.BufferedImage
import java.io.{ File, FileInputStream, FileOutputStream, IOException, InputStream, OutputStream }
import java.util.Scanner
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import de.vorb.pnm.FileType

private[pnm] object PNMImpl extends PNM {
  def read(file: File): BufferedImage =
    read(new FileInputStream(file))

  def write(file: File, ft: FileType, img: BufferedImage): Unit =
    write(new FileOutputStream(file), ft, img)

  def read(in: InputStream): BufferedImage = fileType(in) match {
    case ft: FileType.ASCII =>
      val scanner = new Scanner(in, "ASCII")
      scanner.useDelimiter("""\s+#.+\s+|\s+""".r.pattern)
      val width = scanner.nextInt()
      val height = scanner.nextInt()

      val imgType = imageType(ft)

      val img = new BufferedImage(width, height, imgType)

      ft match {
        case FileType.BitmapASCII =>
          readImage(img) {
            if (scanner.nextInt() == 1) 0x000000
            else 0xFFFFFF
          }
        case FileType.GraymapASCII =>
          val max = scanner.nextInt
          readImage(img) {
            val gray = normalize(scanner.nextInt(), max)
            gray << 16 | gray << 8 | gray
          }
        case FileType.PixmapASCII =>
          val max = scanner.nextInt()

          if (max <= 255) {
            readImage(img){
              normalize(scanner.nextInt(), max) << 16 |
                normalize(scanner.nextInt(), max) << 8 |
                normalize(scanner.nextInt(), max)
            }
          } else {
            readImage(img) {
              ((normalize(scanner.nextInt(), max) << 8) & 0xFF0000) |
                (normalize(scanner.nextInt(), max) & 0x00FF00) |
                (normalize(scanner.nextInt(), max) >> 8 & 0x0000FF)
            }
          }
      }

      scanner.close()

      // result
      img
    case ft: FileType.Binary =>
      val width = readInt(in)
      val height = readInt(in)

      val imgType = imageType(ft)
      val img = new BufferedImage(width, height, imgType)

      ft match {
        case FileType.BitmapBinary =>
          readBitmapBinary(in, img)
        case FileType.GraymapBinary =>
          val max = readInt(in)
          readImage(img) {
            val gray = normalize(in.read(), max)
            gray << 16 | gray << 8 | gray
          }
        case FileType.PixmapBinary =>
          val max = readInt(in)
          readImage(img) {
            (normalize(in.read(), max) << 16) |
              (normalize(in.read(), max) << 8) |
              normalize(in.read(), max)
          }
      }

      in.close()

      // result
      img
  }

  def normalize(x: Int, max: Int): Int =
    (x.toDouble / max * 255).toInt

  def readImage(img: BufferedImage)(nextColor: => Int): Unit = {
    for {
      y <- 0 until img.getHeight
      x <- 0 until img.getWidth
      color = nextColor
    } {
      img.setRGB(x, y, color)
    }
  }

  def readInt(in: InputStream): Int = {
    val whitespace = Set[Int](' ', '\n', '\r', '\t')

    val number = new StringBuilder

    // skip whitespace
    var next = in.read()
    while (whitespace.contains(next)) {
      next = in.read()
    }

    // read width
    while (!whitespace.contains(next)) {
      number += next.toChar
      next = in.read()
    }

    number.toInt
  }

  def readBitmapBinary(in: InputStream, img: BufferedImage): Unit = {
    val width = img.getWidth
    val height = img.getHeight

    // running variables
    var i = 7
    var x = 0
    var y = 0
    var byte = in.read()

    while (byte > -1) {
      while (i >= 0) {
        if (((byte >> i) & 0x1) == 0)
          img.setRGB(x, y, 0xFFFFFF)
        else
          img.setRGB(x, y, 0x000000)

        i -= 1
        x += 1
        if (x == width) {
          // skip the rest of the line
          i = -1
          x = 0
          y += 1

          // all required bytes read, stop
          if (y == height) {
            return
          }
        }
      }

      i = 7
      byte = in.read()
    }
  }

  def fileType(in: InputStream): FileType = {
    (in.read(), in.read()) match {
      case ('P', '1') => FileType.BitmapASCII
      case ('P', '2') => FileType.GraymapASCII
      case ('P', '3') => FileType.PixmapASCII

      case ('P', '4') => FileType.BitmapBinary
      case ('P', '5') => FileType.GraymapBinary
      case ('P', '6') => FileType.PixmapBinary

      case (a, b) => throw new IOException(
        s"Invalid file format. Magic number: '${a.toChar}${b.toChar}'")
    }
  }

  def imageType(ft: FileType): Int = ft match {
    case FileType.BitmapASCII   => BufferedImage.TYPE_BYTE_BINARY
    case FileType.GraymapASCII  => BufferedImage.TYPE_BYTE_GRAY
    case FileType.PixmapASCII   => BufferedImage.TYPE_INT_RGB

    case FileType.BitmapBinary  => BufferedImage.TYPE_BYTE_BINARY
    case FileType.GraymapBinary => BufferedImage.TYPE_BYTE_GRAY
    case FileType.PixmapBinary  => BufferedImage.TYPE_INT_RGB
  }

  def write(out: OutputStream, ft: FileType, img: BufferedImage): Unit = {
    val bos = new BufferedOutputStream(out)
    val writer = new OutputStreamWriter(bos, "ASCII")

    // Write header
    writer.write('P')
    ft match {
      case FileType.BitmapASCII   => writer.write('1')
      case FileType.GraymapASCII  => writer.write('2')
      case FileType.PixmapASCII   => writer.write('3')
      case FileType.BitmapBinary  => writer.write('4')
      case FileType.GraymapBinary => writer.write('5')
      case FileType.PixmapBinary  => writer.write('6')
    }

    writer.write('\n')
    writer.write("# Image created with Scala PNM libary\n")
    writer.write("# https://github.com/pvorb/scala-pnm\n")
    writer.write("# (c) 2014 Paul Vorbach\n")
    writer.write(img.getWidth.toString)
    writer.write(' ')
    writer.write(img.getHeight.toString)
    writer.write('\n')

    val lineLength = new IntRef(0)

    ft match {
      case FileType.BitmapASCII =>
        writeImage(writer, img, lineLength, ascii = true) { rgb =>
          if (rgb == 0xFF000000)
            "1"
          else
            "0"
        }

      case FileType.GraymapASCII =>
        writer.write("255\n")
        writeImage(writer, img, lineLength, ascii = true) { rgb =>
          pad(rgb & 0x0000FF, 3)
        }

      case FileType.PixmapASCII =>
        writer.write("255\n")
        writeImage(writer, img, lineLength, ascii = true) { rgb =>
          val r = rgb >> 16 & 0xFF
          val g = rgb >> 8 & 0xFF
          val b = rgb & 0xFF

          pad(r, 3) + " " + pad(g, 3) + " " + pad(b, 3)
        }

      case FileType.BitmapBinary =>
        writer.flush()
        writeBitmapBinary(out: OutputStream, img: BufferedImage)

      case FileType.GraymapBinary =>
        writer.write("255\n")
        writeImage(writer, img, lineLength, ascii = false) { rgb =>
          (rgb & 0x0000FF).toChar.toString
        }

      case FileType.PixmapBinary =>
        writer.write("255\n")
        writeImage(writer, img, lineLength, ascii = false) { rgb =>
          new String(Array((rgb >> 16 & 0xFF).toChar, (rgb >> 8 & 0xFF).toChar,
            (rgb & 0xFF).toChar))
        }
    }
  }

  def writeImage(writer: Writer, img: BufferedImage, lineLength: IntRef,
    ascii: Boolean)(toString: Int => String): Unit = {
    for {
      y <- 0 until img.getHeight
      x <- 0 until img.getWidth
      color = toString(img.getRGB(x, y))
      colorLength = color.length
    } {
      if (ascii) {
        if (lineLength.x + colorLength > 68) {
          writer.write('\n')
          lineLength.x = 0
        }

        if (lineLength.x > 0) {
          writer.write(' ')
          lineLength.x += 1
        }

        lineLength.x += colorLength
      }

      writer.write(color)
    }

    if (ascii)
      writer.write('\n')

    writer.close()
  }

  def writeBitmapBinary(out: OutputStream, img: BufferedImage): Unit = {
    val width = img.getWidth
    val height = img.getHeight

    var x = 0
    var y = 0
    var byte = 0
    var i = 7

    while (y < height) {
      while (i >= 0) {
        if (img.getRGB(x, y) == 0xFF000000) {
          byte |= 1 << i
        }

        x += 1
        i -= 1
      }

      out.write(byte.toChar)

      byte = 0
      i = 7
      if (x == width) {
        x = 0
        y += 1
      }
    }

    out.close()
  }

  def pad(str: Any, length: Int): String = {
    val string = String.valueOf(str)
    val strLen = string.length
    if (strLen == 1)
      "  " + string
    else if (strLen == 2)
      " " + string
    else
      string
  }
}

private[impl] class IntRef(var x: Int)
