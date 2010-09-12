package com.rt.indexing.persistence

import java.io.StringWriter
import org.junit.{Assert, Before, Test}
import com.rt.ohhla.persistence
import persistence.{AlbumMetaData, AlbumTrack}

class AlbumMetaDataTest{

  //metaData: AlbumMetaData

  @Before def create()={

  }


  @Test def testPersistence()={
    val artist="ck"
    val title="theTitle"
    val year:Int = 2000

    val track1 = new AlbumTrack("track1", 1, "http://url.com")
    val track2 = new AlbumTrack("track2", 2, "http://url.com")

    val metaData:AlbumMetaData = new AlbumMetaData(artist, title, year, List[AlbumTrack](track1, track2))
    val writer = new StringWriter();
    metaData.write(writer);
    println(writer.toString)
    Assert.assertEquals(
      """{"artist":"ck","title":"theTitle","tracks":[{"number":1,"title":"track1","url":"http:\/\/url.com"},{"number":2,"title":"track2","url":"http:\/\/url.com"}],"year":2000}""".replace("\n", "").replace(" ", ""),
      writer.toString.replace("\n", "").replace(" ", ""));
    
    val res = AlbumMetaData.fromString(metaData.toJson)
    println(res.toString)
    Assert.assertEquals(metaData.title, res.title)
    Assert.assertEquals(metaData.artist, res.artist)
    Assert.assertEquals(metaData.tracks.size, res.tracks.size)
  }
}
