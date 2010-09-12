package com.rt.itunes

import org.apache.commons.io.IOUtils
import java.net.{URLEncoder, URI}
import java.lang.String
import collection.JavaConversions._
import com.rt.ohhla.OhhlaFiles
import com.rt.indexing.persistence.ArtistAlbums
import java.io.{FileOutputStream, File, InputStream}
import util.Levenshtein
import com.rt.util.NameMapper

class ItunesDownloader(itunesSerializer: ItunesSerializer) {
  def downloadForArtists(artistRootFile: String) = {

    OhhlaFiles.allArtistFolderNames(artistRootFile).sortWith((a, b) => a < b).foreach(aritstFolderName => {
      val artistFolder: File = new File(artistRootFile, aritstFolderName)
      val artistAlbums: ArtistAlbums = ArtistAlbums.fromFolder(artistFolder.getAbsolutePath)
      artistAlbums.albums.foreach(album => {
        val artist: String = artistAlbums.artist
        val albumName: String = album.metaData.title
        val query: String = artist + " " + albumName

        val target: File = new File(artistFolder.getAbsolutePath + File.separator + album.fileInfo.fileName, "itunes.json")
        if (!target.exists) {
          download(query, albumName) match{
            case None => None
            case Some(albumJson) => {
              IOUtils.write(albumJson, new FileOutputStream(target))
            }
          }
        }
      })
    })
  }

  def download(query: String, albumName:String):Option[String] = {
    val albumJson: String = jsonForAlbum(query, "US")
    val albums: Albums = itunesSerializer.deserializeFromString(albumJson)
    val albumId: Int = getAlbumId(albumJson, albumName)

    if (albums.trackNames.size == 0) println("  No albums found for " + query)
    if (albumId < 0) println("  No albums found in results " + query)

    albumId match{
      case -1 => None
      case _ => println("got result for " + query); Some(albumJson)
    }
  }

  private def getAlbumId(jsonForAlbum: String, albumName: String): Int = {
    val itunesSerializer: ItunesSerializer = new ItunesSerializer
    val albums: Albums = itunesSerializer.deserializeFromString(jsonForAlbum)
    //    albums.results.foreach(a => {
    //      println("album: " + a.collectionName + ": " + Levenshtein.matches(a.collectionName, albumName))
    //    })
   //println(jsonForAlbum)
    
    albums.results.filter(a => "song" == a.kind).filter(a => Levenshtein.matches(a.collectionName, albumName) < 5).map(_.artistId).firstOption.getOrElse(-1)
  }

  def jsonForAlbum(searchString: String, country: String): String = {
    val result = IOUtils.toString(jsonStreamForAlbum(searchString, country))

    result
            .replace("\"trackNumber\":null", "\"trackNumber\":-1")
            .replace("\"artistId\":null", "\"artistId\":-1")
            .replace("\"collectionId\":null", "\"collectionId\":-1")
  }

  private def jsonListForAlbum(searchString: String, country: String): List[String] = {
    IOUtils.readLines(jsonStreamForAlbum(searchString, country)).toList.asInstanceOf[List[String]]
  }

  private def jsonStreamForAlbum(searchString: String, country: String): InputStream = {
    //"http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStoreServices.woa/wa/wsSearch?term=wutang%20enter%20the%20wu%20tang%2036%20chambers%20clan&country=US&limit=200"
    val urlTemplate: String = "http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStoreServices.woa/wa/wsSearch?term=%s&country=%s&limit=200"
    val url = String.format(urlTemplate, encodeForUrl(NameMapper.nSpace(searchString)), country)
    println("url: " + url)
    return URI.create(url).toURL.openStream
  }

  private def encodeForUrl(st: String): String = {
    return st.replace(" ", "%20").replace("_", "%20")
  }
}