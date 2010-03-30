package com.rt.ohhla

import org.apache.commons.io.IOUtils
import java.io.{InputStream, Reader}
import com.rt.indexing.{SongMetaData}
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.String
import com.rt.indexing.persistence.{Album, AlbumTrack, ArtistAlbums, AlbumMetaData}
import com.rt.util.{IO, MapUtils}


class AnonymousAlbumGrabber(val streamBuilder: OhhlaStreamBuilder) extends AbstractGrabber {
  private val parentDirectoryTitle = "Parent Directory"
  private val LOG: Logger = LoggerFactory.getLogger(classOf[AnonymousAlbumGrabber])

  class TrackNumberGenerator {
    var number: Int = 0

    def next(): Int = {
      number += 1
      number
    }
  }

  object SongAtribute extends Enumeration {
    type SongAtribute = Value
    val Artist = Value("artist")
    val Album = Value("album")
    val Title = Value("song")
  }
  import SongAtribute._

  override def getArtistAlbumsFromUrl(artist: String, url: String): Option[ArtistAlbums] = {
    getAlbumUrlsFromArtistIndex(url) match {
      case None => LOG.warn("no album urls from artist url: '" + url + "'"); None
      case Some(urls: List[String]) => Some(new ArtistAlbums(artist, newAlbumsList(urls))) //getAlbumMetaDataFromUrl(albumUrl)
    }
    //    getArtistFolderUrlFromArtistName(artist, OhhlaConfig.allOhhlaIndexes) match{
    //      case None => LOG.warn("no artist folder for artist: '"+artist+"'");None
    //      case Some(url)=> println("url is "+url);{
    //        getAlbumUrlsFromArtistIndex(url) match{
    //          case None => LOG.warn("no album urls from artist url: '"+url+"'");None
    //          case Some(urls: List[String]) => Some(new ArtistAlbums(artist, newAlbumsList(urls))) //getAlbumMetaDataFromUrl(albumUrl)
    //        }
    //      }
    //    }
  }

  def newAlbumsList(urls: List[String]): List[Album] = {
    val albumMetaData: List[AlbumMetaData] = urls.map(url => getAlbumMetaDataFromUrl(url).getOrElse(null)).filter(_ != null)
    albumMetaData.map(m => new Album(m))
  }


  def getAlbumMetaDataFromUrl(albumUrl: String): Option[AlbumMetaData] = {
    LOG.info("creating album meta data from url " + albumUrl)
    getAlbumUrlsFromArtistIndex(albumUrl) match {
      case None => None
      case Some(urls: List[String]) => {
        val streams: List[(String, InputStream)] = urls.map(url => {url -> streamBuilder.fromUrlPrefix(url)}) //IO.streamFromUrl(url)})
        val numberGenerator: TrackNumberGenerator = new TrackNumberGenerator()
        Some(getAlbumMetaDataFromSongData(streams.zipWithIndex.map(v => getSongDataFromFile(v._1._1, v._1._2, v._2 + 1))))
      }
    }
  }

  //val b = sum(1, _: Int, 3)

  def getArtistFolderUrlFromArtistName(artistName: String, in: InputStream): Option[String] = {
    getArtistFolderUrlFromArtistName(artistName, IO.readLines(in))
  }

  def getArtistFolderUrlFromArtistName(artistName: String, lines: List[String]): Option[String] = {
    val filterd: List[String] = lines.filter(_.contains(artistName))

    filterd.size match {
      case 0 => None
      case 1 => Some(ParsingUtils.hrefLinkAndtitle(filterd(0))._1)
      case _ => {
        LOG.info("got more than one match for " + artistName + ", using first");
        Some(ParsingUtils.hrefLinkAndtitle(filterd(0))._1)
      }
    }
  }


  def getAlbumMetaDataFromSongData(songData: List[(AlbumTrack, SongMetaData)]): AlbumMetaData = {
    val albumTitle = ParsingUtils.mostCommonValueInList(songData.map(_._2.album))
    val artist = ParsingUtils.mostCommonValueInList(songData.map(_._2.artist))

    AlbumMetaData(artist, albumTitle, -1, songData.map(_._1))
  }

  //  def getAlbumMetaDataFromFoundSongs(songs: List[SongMetaData]):Option[AlbumMetaData]={
  //    val albumTitle = ParsingUtils.mostCommonValueInList(songs.map(_.album))
  //    val artist = ParsingUtils.mostCommonValueInList(songs.map(_.artist))
  //
  //    Some(AlbumMetaData(artist, albumTitle, -1, getAlbumTracksFromSongs(songs)))
  //  }

  //  def getAlbumTracksFromSongs(songs: List[SongMetaData]):List[AlbumTrack]={
  //    songs.map(song => AlbumTrack(song.title, -1, null))
  //  }

  //  def getAlbumTrackFromFile(in:InputStream, url:String):Option[AlbumTrack]={
  //    val lines:List[String] = IO.readLines(in)
  //    //lines.foreach(line => println("= l "+line+", contains artist: "+contains2(line, "artist")))
  //    Some(AlbumTrack(
  //      getTitle(lines).getOrElse(null),
  //      -1, // can't get year
  //      url
  //      ))
  //  }

  def getSongDataFromFile(url: String, in: InputStream, trackNo: Int): (AlbumTrack, SongMetaData) = {
    val lines: List[String] = IO.readLines(in)
    //lines.foreach(line => println("= l "+line+", contains artist: "+contains2(line, "artist")))
    (AlbumTrack(
      getTitle(lines).getOrElse(null),
      trackNo,
      url
      ) -> SongMetaData(
      getTitle(lines).getOrElse(null),
      getArtist(lines).getOrElse(null),
      -1, // can't get year
      getAlbum(lines).getOrElse(null),
      trackNo
      ))
  }

  def getArtist(lines: List[String]) = getSongAttribute(Artist, lines)

  def getAlbum(lines: List[String]) = getSongAttribute(Album, lines)

  def getTitle(lines: List[String]) = getSongAttribute(Title, lines)

  def getSongAttribute(att: SongAtribute, lines: List[String]): Option[String] = {
    val line: Int = lines.findIndexOf(i => contains2(i, att.toString))
    line match {
      case -1 => LOG.warn("did not find attribute " + att.toString + " in any lines"); None
      case _ => {
        getTextAfterColon(lines(line)) match {
          case None => LOG.warn("didn't find text after colon in line " + lines(line)); None
          case (x) => Some(x.get.trim)
        }
      }
    }
  }

  def contains2(line: String, toFind: String): Boolean = {
    (line.contains(toFind) || line.contains(toFind.capitalize)) && line.contains(":")
  }

  def getTextAfterColon(in: String): Option[String] = {
    in.indexOf(':') match {
      case -1 => LOG.warn("didn't find colon in line " + in); None
      case (x) => Some(in.substring(x + 1, in.length).trim)
    }
  }


  def getAlbumUrlsFromArtistIndex(artistIndexRelativeUrl: String): Option[List[String]] = {
    val in: InputStream = this.streamBuilder.fromUrlFolder(artistIndexRelativeUrl)
    val lines: List[String] = IO.readLines(in)
    println("lines = " + lines)
    val filterd: List[String] = lines.filter(_.contains("href")).filter(!_.contains(parentDirectoryTitle))
    println("filterd = " + filterd)
    Some(filterd.map(ParsingUtils.betweenFirstAndLastQuotes).map(removeLastSlash).map(artistIndexRelativeUrl + "/" + _))
  }

  private def removeLastSlash(st: String): String = {
    if (st != null && st.length > 0 && st.charAt(st.length - 1) == '/') {
      st.substring(0, st.length - 1)
    } else {
      st
    }
  }

}