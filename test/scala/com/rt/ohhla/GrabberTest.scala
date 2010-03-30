package com.rt.ohhla

import org.junit.{Before, Test}
import com.rt.indexing.persistence.ArtistAlbums

class GrabberTest{
  var grabber:Grabber = null
  var builder:OhhlaStreamBuilder = null

  @Before def setup(){
    builder = new OhhlaStreamBuilderTestImpl()
    grabber = new Grabber(builder);
  }

  @Test def testGrab(){
    val albums:ArtistAlbums = grabber.getArtistAlbums("Cypress Hill").get
    println(albums)
  }
}