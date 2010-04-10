package com.rt.ohhla

import com.rt.indexing.persistence.ArtistAlbums
import io.Source
import org.slf4j.{LoggerFactory, Logger}



class Grabber(val streamBuilder: OhhlaStreamBuilder) {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[Grabber])

  private val anonymousGrabber: AnonymousAlbumGrabber = new AnonymousAlbumGrabber(streamBuilder)
  private val yfaGrabber: ArtistPageGrabber = new ArtistPageGrabber(streamBuilder)

  //<a href="YFA_nas.html#firm">Firm, The</a>
  //<a href="anonymous/fixxers/">Fixxers, The</a>
  def getArtistAlbums(artist: String): Option[ArtistAlbums] = {
    OhhlaConfig.urlForArtist(artist).flatMap(artistAlbumsFromUrl(artist, _))
  }

  private def artistAlbumsFromUrl(artist: String, artistUrl: String): Option[ArtistAlbums] = {
    LOG.info("using url " + artistUrl)
    if (artistUrl.startsWith("YFA")) {
      yfaGrabber.getArtistAlbumsFromUrl(artist, artistUrl)
    } else {
      anonymousGrabber.getArtistAlbumsFromUrl(artist, artistUrl)
    }
  }

  //  def urlForArtist(artistName: String): Option[String] = {
  //    def res = OhhlaConfig.allOhhlaIndexes.filter(_.contains(">" + artistName + "<"));
  //    res.length match {
  //      case 0 => None
  //      case 1 => Some(ParsingUtils.betweenFirstAndLastQuotes(res.head))
  //    }
  //  }
}