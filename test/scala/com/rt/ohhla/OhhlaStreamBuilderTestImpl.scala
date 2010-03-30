package com.rt.ohhla

import java.io.InputStream
import com.rt.util.IO


class OhhlaStreamBuilderTestImpl extends OhhlaStreamBuilder {
  def fromUrlPrefix(prefix: String): InputStream = {
    IO.streamFromClasspath(prefix)
  }

  def fromUrlFolder(folderUrl: String): InputStream = {
    fromUrlPrefix(folderUrl + "/index.html")
  }
}