package com.rt.ohhla

import java.lang.String
import org.junit.{Before, Test}
import org.junit.Assert._
import com.rt.indexing.SongMetaData
import java.io.InputStream
import com.rt.indexing.persistence.{ArtistAlbums, AlbumTrack, AlbumMetaData}

class AnonymousAlbumGrabbingTest  {

  var grabber:AnonymousAlbumGrabber = null

  @Before def setUp()={
    val downloader:OhhlaStreamBuilder = new OhhlaStreamBuilderTestImpl()
    grabber = new AnonymousAlbumGrabber(downloader)
  }

  @Test def findUrlFromArtistName()={
    val artistName:String = "Public Enemy"
    val ohhlaPageFile:String = "/ohhla-site/all_four.html"

    val in:InputStream = getClass.getResource(ohhlaPageFile).openStream;

    val artistUrl: String = grabber.getArtistFolderUrlFromArtistName(artistName, in).get
    assertEquals("anonymous/pb_enemy/", artistUrl)
  }

  @Test def findAlbumUrlsFromArtistUrl()={
    val artistFolder:String = "anonymous/pb_enemy"
    val urls:List[String]  = grabber.getAlbumUrlsFromArtistIndex(artistFolder).get
    assertEquals(2, urls.size)
    println("urls are "+urls)
    assertTrue(urls.contains(artistFolder+"/apoc_91"))
  }

  @Test def testGetAlbumMetaDataFromFoundSoungs(){
    val songMetaData:List[SongMetaData] = List(
      SongMetaData("title1", "artist1", -1, "album1", -1),
      SongMetaData("title2", "artist1", -1, "album1", -1),
      SongMetaData("title3", "artist1", -1, "album1(1)", -1),
      SongMetaData("title4", "artist1-with extra text", -1, "album1", -1)
     )

    val tracks:List[AlbumTrack] = List(
      AlbumTrack("title1", -1, null),
      AlbumTrack("title2", -1, null),
      AlbumTrack("title3", -1, null),
      AlbumTrack("title4", -1, null)
   )


    val album: AlbumMetaData = grabber.getAlbumMetaDataFromSongData(tracks.zip(songMetaData))

    assertEquals(AlbumMetaData("artist1", "album1", -1, tracks), album)
  }

  @Test def testMostCommonValueInList(){
    val l:List[String] = List("a", "c", "b" ,"a")
    assertEquals("a", ParsingUtils.mostCommonValueInList(l))
  }

  @Test def getArtistAlbumsFromArtistName(){
    val artist:String = "Public Enemy"
    val url:String = OhhlaFiles.urlForArtist(artist).get
    val artistAlbums: ArtistAlbums = grabber.getArtistAlbumsFromUrl(url, artist).get
    assertEquals(2, artistAlbums.albums.size)
    assertEquals(artist, artistAlbums.artist)

    println(artistAlbums.albums(0))
  }

  @Test def getSongMetadataFromFile(){
    val songFile:String = "/anonymous/pb_enemy/apoc_91/shutdown.pbe.txt"
    val in:InputStream = getClass.getResource(songFile).openStream
    
    val metadata:SongMetaData = grabber.getSongDataFromFile("fake/url", in, 1)._2
    assertEquals("Public Enemy", metadata.artist)
    assertEquals("Apocalypse 91", metadata.album)
    assertEquals("Shut Em Down", metadata.title)

  }
}