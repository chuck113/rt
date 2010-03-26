package com.rt.indexing.persistence

import org.junit.Test
import junit.framework.Assert
import java.io.StringWriter


class ArtistAlbumsTest{
   @Test def testPersistence()={
    val artist="ck"

    val metaData:ArtistAlbums = new ArtistAlbums(artist, List[Album](
      new Album(new AlbumMetaData("artist1", "title 1", 2001, List[AlbumTrack](new AlbumTrack("track1", 1, "http://test.com")))),
      new Album(new AlbumMetaData("artist2", "title2", 1990, List[AlbumTrack](new AlbumTrack("track1", 1, "http://test.com"), new AlbumTrack("track2", 2, "http://test.com"))))))
    val writer = new StringWriter();
    metaData.write(writer);
    println(writer.toString.replace("\n", "").replace(" ", ""))
    Assert.assertEquals(
      """{"albums":{"title1":"title_1","title2":"title2"},"artist":"ck"}""".replace("\n", "").replace(" ", ""),
      writer.toString.replace("\n", "").replace(" ", ""));

    val res = ArtistAlbums.fromString(metaData.toJson)
    println(res.toString)
  }
}