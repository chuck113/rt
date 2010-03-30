package com.rt.ohhla

import com.rt.indexing.persistence.ArtistAlbums


abstract class AbstractGrabber{

  def getArtistAlbumsFromUrl(artist: String, artistUrl: String): Option[ArtistAlbums]
}