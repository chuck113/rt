package com.rt.indexing.persistence

import java.io._
import reflect.BeanInfo
import sjson.json.{Serializer, JSONTypeHint}
import org.apache.commons.io.IOUtils
import annotation.target.field
// how can we use traits here? Don't want AlbumMetaData to know about xml so it should be wrapped in a trait
// that knows how to persist it.

/**
 * Data about an album including the tracks
 */
@BeanInfo
case class AlbumMetaData(
        artist: String,
        title: String,
        year: Int,
        @(JSONTypeHint @field)(value = classOf[AlbumTrack]) tracks: List[AlbumTrack]){
  
  private def this() = this(null, null, -1, null)

  private[this] val serializer = Serializer.SJSON
  private val jsonPersister:JsonPersister = new JsonPersister();

  def toJson():String = {
    new String(serializer.out(this))
  }
  def writeToFolder(folder:String):Unit ={
    jsonPersister.writeToFile(folder+"/"+Constants.albumMetaDataFileName, toJson())
  }

  def write(out:Writer):Unit ={
    jsonPersister.write(out, toJson())
  }

  override def toString():String ={
    "AlbumMetaData ["+artist+", "+title+", "+year+", "+tracks+"]"
  }
}

object AlbumMetaData{

  def fromString(json:String):AlbumMetaData ={
    Serializer.SJSON.in[AlbumMetaData](json).asInstanceOf[AlbumMetaData]  
  }

  def fromFolder(folder:String):AlbumMetaData ={
     fromString(IOUtils.toString(new FileInputStream(folder+"/"+Constants.albumMetaDataFileName)))
  }
}