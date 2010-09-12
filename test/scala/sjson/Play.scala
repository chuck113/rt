package sjson

import json.{Serializer}
import org.junit.{Assert, Test}
import scala.reflect._
import com.rt.indexing.persistence._

@BeanInfo
case class TestClass(p1: String) {
  private def this() = this (null)
}

class Play {
  private[this] val serializer = Serializer.SJSON
  implicit def ignoreProps = List[String]("class")

  @Test def testPersistence() = {
    import dispatch.json._

    val artist = "ck"
    val title = "theTitle"
    val year: Int = 2000

    val track1 = new AlbumTrack("track1", 1, "http://url.com")
    val track2 = new AlbumTrack("track2", 2, "http://url.com")

    val metaData: AlbumMetaData = new AlbumMetaData(artist, title, year, List[AlbumTrack](track1, track2))

    val out: Array[Byte] = serializer.out(metaData)
    println("out is " + new String(out))
    val m: AlbumMetaData = serializer.in[AlbumMetaData](out).asInstanceOf[AlbumMetaData]
    println("out is " + m)


    val json = metaData.toJson()
    println("json = " + json)
    val d: AlbumMetaData = AlbumMetaData.fromString(json)
    Assert.assertEquals(d, metaData)

    val aa:ArtistAlbums = new ArtistAlbums(artist, List[Album](
         new Album(new AlbumMetaData("artist1", "title1", 2001, List[AlbumTrack](new AlbumTrack("track1", 1, "http://test.com")))),
         new Album(new AlbumMetaData("artist2", "title2", 1990, List[AlbumTrack](new AlbumTrack("track1", 1, "http://test.com"), new AlbumTrack("track2", 2, "http://test.com"))))))

   println("res: "+ ArtistAlbums.fromJson(aa.toJson))

    //val jsBean = new Object with JsBean with DefaultConstructor

//    val albums:Map[String, String] = Map[String, String]("one" -> "one1", "two" -> "two2");
//   // val aaMap = Map("artist" -> "title", "albums" -> albums)
//    val aaJson:ArtistAlbumsJson = ArtistAlbumsJson("title", albums)
//    println("aaJson: "+aaJson)
//    val jsonObj:String = jsBean.toJSON(aaJson)
//    println("jsonObj: "+jsonObj)
//
//    val res:ArtistAlbumsJson = jsBean.fromJSON(Js(jsonObj), Some(classOf[ArtistAlbumsJson])).asInstanceOf[ArtistAlbumsJson]
//    println("res: "+res)



//    val aaMapOut: Array[Byte] = serializer.out(aaMap)
//    println("aaMapOut is " + new String(aaMapOut))
//    val fromString = JsValue.fromString(new String(aaMapOut))
//    println("from string: "+fromString)

    // jsBook.self.asInstanceOf[Map[JsString, JsValue]].get(JsString("ISBN")).get.self should equal ("012-456372")
//    val map:Map[JsString, JsValue] = fromString.self.asInstanceOf[Map[JsString, JsValue]]
//    map.get(JsString("artist")).get.self
//    println("from string: "+map+", artists = "+map.get(JsString("artist")).get.self)
//    println("albums = "+map.get(JsString("albums")).get.self.asInstanceOf[Map[JsString, JsValue]])
//
//    val convertedMap:Map[JsString, JsString] = map.get(JsString("albums")).get.self.asInstanceOf[Map[JsString, JsString]]
//    val resMap:Map[String, String] = convertedMap.foldLeft(Map[String, String]()){
//      (map, entry)=>{
//        println("entry 2: "+entry._2.toString+", length "+entry._2.toString.length+", type "+jsBean.fromJSON(entry._2, ))
//        map(entry._1.self) = entry._2.toString
//        //map(" ") = " "
//      }
//    }

    //println("convertedMap "+resMap)

    //println("artist albums "+aa.toJson)
    
    //val m:Map = serializer.in[AnyRef](aaMapOut).asInstanceOf[Map]
    //println("out is " + m)

    //val res = jsBean.fromJSON(Js(jsonMap), Some(classOf[Map]))
    //println("res = "+res)

    //    val tracks:List[AlbumTrack] = m.tracks.foldLeft(List[AlbumTrack]()){(list, track) => {
    //      list + AlbumTrack(track("title"), track("number"), track("url"))
    //    }}
    //    println("result is " + tracks)
    //Assert.assertEquals(m, metaData)
  }

//  def toJson(aa:ArtistAlbums):String={
//    StringBuilder b = new StringBuilder("{ \"artist\" = \""+aa.artist+"\" {\n");
//    aa.albums.foreach(album =>
//      b.append("  {" \""+album.fileInfo.albumName+"\" : \""+album.fileInfo.fileName+"\" }")
//    )
//  }
}