package com.rt.ohhla

import java.io.InputStream
import java.lang.String
import com.rt.util.IO


abstract class OhhlaStreamBuilder{
  def fromUrlPrefix(prefix:String):InputStream

  /**
   * gives a stream to the page used as an index for a folder
   */
  def fromUrlFolder(folderUrl:String):InputStream
}

class OhhlaStreamBuilderImpl extends OhhlaStreamBuilder{
  def fromUrlPrefix(urlPrefix: String):InputStream = {
    IO.streamFromUrl(OhhlaConfig.ohhlaUrl+urlPrefix)
  }

  def fromUrlFolder(folderUrl: String):InputStream = {
    fromUrlPrefix(folderUrl)
  }
}