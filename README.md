scala-pnm
=========

Scala library for reading and writing Portable Anymap images on the JVM.

It correctly reads and writes

  * Portable Bitmap binary and ASCII,
  * Portable Graymap binary and ASCII,
  * Portable Pixmap binary and ASCII.

Pixmaps with 48 bits per pixel are converted to 24bpp, since
`java.awt.BufferedImage` doesn't support 48bpp images.


Installation
------------

SBT:

~~~ scala
libraryDependencies += "de.vorb" %% "scala-pnm" % "0.0.+"
~~~


Usage
-----

~~~ scala
import java.awt.image.BufferedImage
import java.io.File

import de.vorb.pnm.{ PNM, FileType }

val img: BufferedImage = PNM.read(new File("path/to/image.pnm"))
PNM.write(new File("path/to/image2.pnm"), FileType.BitmapASCII, img)
~~~


API
---

~~~ scala
package de.vorb.pnm

trait PNM {
  /**
   * Reads a PNM file.
   */
  def read(file: File): BufferedImage

  /**
   * Writes a PNM file with the given file type.
   */
  def write(file: File, ft: FileType, img: BufferedImage): Unit

  /**
   * Reads a PNM input stream.
   */
  def read(in: InputStream): BufferedImage

  /**
   * Writes an image with the given file type to the output stream.
   */
  def write(out: OutputStream, ft: FileType, img: BufferedImage): Unit
}

object PNM extends PNM {
  // Implementation ...
}

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
~~~


License
-------

(MIT license)

Copyright © 2014 Paul Vorbach

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the “Software”), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
