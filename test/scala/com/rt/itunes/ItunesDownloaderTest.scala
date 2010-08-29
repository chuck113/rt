package com.rt.itunes

import org.junit.{Before, Test, Assert}
import org.junit.Assert._
import java.lang.String
import org.hamcrest.core.IsNot
import com.rt.ohhla.OhhlaConfig
import com.rt.indexing.persistence.{Album, ArtistAlbums, AlbumTrack, AlbumMetaData}
import java.io.File
import util.Levenshtein

class ItunesDownloaderTest {

  var itunesSerializer: ItunesSerializer = _
  var itunesDownloader: ItunesDownloader = _

  @Before def setUp() {
    itunesSerializer = new ItunesSerializer
    itunesDownloader = new ItunesDownloader(itunesSerializer)
  }

  // integration test
  //@Test
  def shouldDownloadWuTang = {
    val searchString: String = "WUTANG CLAN ENTER_THE_WUTANG_36_CHAMBERS"

    val jsonForAlbum: String = itunesDownloader.jsonForAlbum(searchString, "US")
    Assert.assertTrue(jsonForAlbum.length > 37000)

    Assert.assertEquals(itunesSerializer.deserializeFromString(jsonForAlbum).trackNames.length, 12)
  }

  //@Test
  def shouldNotThrowErrorOn50Cent= {
    val query = "50 CENT THE MASSACRE"
    val download: Option[String] = itunesDownloader.download(query, "The Massacre")
  }

  @Test
  def shouldGetAFewArtists = {
    itunesDownloader.downloadForArtists(OhhlaConfig.rawTargetLocation)
    //itunesDownloader.downloadForArtists("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\methodman-test""")

    //   val artists: List[String] = List[String]("THE_GENIUSGZA", "NOTORIOUS_BIG", "GANGSTARR", "KRSONE", "MADVILLAIN")

    //    artists.foreach(artistName => {
    //      val artistAlbums: ArtistAlbums = ArtistAlbums.fromFolder(new File(OhhlaConfig.rawTargetLocation, artistName).getAbsolutePath)
    //      artistAlbums.albums.foreach(album => {
    //        val artist: String = artistAlbums.artist
    //        val albumName:String = album.metaData.title
    //        val query:String = artistName +" " + albumName
    //        val jsonForAlbum: String = itunesDownloader.jsonForAlbum(query, "US")
    //        //println("json for '" + artistName + " " + albumName + "' is \n" + jsonForAlbum)
    //        println("id for for '" + artistName + " " + albumName + "' is " + getAlbumId(jsonForAlbum, albumName))
    //      })
    //    })
  }



  //  @Test def shouldGetItunesInfoForAlbum() {
  //    val itunesDownloader: ItunesDownloader = new ItunesDownloader
  //    val itunesSerializer: ItunesSerializer = new ItunesSerializer
  //
  //    val artistFolder: String = new File(OhhlaConfig.testRoot, "wutang-itunes-test").listFiles()(0).getAbsolutePath
  //
  //    val artistAlbums: ArtistAlbums = ArtistAlbums.fromFolder(artistFolder)
  //    val artist: String = artistAlbums.artist
  //    val album: Album = artistAlbums.albums.first
  //    val albumMetaData: AlbumMetaData = album.metaData
  //    println("album: " + albumMetaData.title)
  //    val jsonForAlbum: String = itunesDownloader.jsonForAlbum(artist + " " + albumMetaData.title, "US")
  //    val albums: Albums = itunesSerializer.deserializeFromString(jsonForAlbum)
  //
  //    albums.results.foreach(album => {
  //      println("album: " + album.collectionName + ": " + Levenshtein.matches(album.collectionName, albumMetaData.title))
  //    })
  //
  //    val filteredAlbums = albums.results.filter(t => Levenshtein.matches(t.collectionName, albumMetaData.title) < 5).first
  //
  //    println(filteredAlbums)
  //  }
}