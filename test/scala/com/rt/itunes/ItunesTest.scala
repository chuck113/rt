package com.rt.itunes

import org.junit.{Before, Test, Assert}
import util.Levenshtein
import collection.immutable.List

class ItunesTest {

  var itunesSerializer: ItunesSerializer = _
  var itunesDownloader: ItunesDownloader = _

  @Before def setUp() {
    itunesSerializer = new ItunesSerializer
    itunesDownloader = new ItunesDownloader(itunesSerializer)
  }


  @Test def shouldIndexWuTang= {

    val itunesSerializer: ItunesSerializer = new ItunesSerializer()
    val albums: Albums = itunesSerializer.deserialize("itunes/json/wutang-36chambers.json")
    
    Assert.assertNotNull(albums)
    Assert.assertEquals(27, albums.results.length);
    Assert.assertEquals("Enter the Wu-Tang (36 Chambers)", albums.albumName())
    Assert.assertEquals("Bring da Ruckus", albums.trackNames.first)
  }

  @Test def shouldIndex50Cent={
    val itunesSerializer: ItunesSerializer = new ItunesSerializer()
    val albums: Albums = itunesSerializer.deserialize("itunes/json/50cent-themassacre.json")

    val filter: List[Track] = albums.results.filter(a => "song" == a.kind)
    val list: List[Track] = filter.filter(a => Levenshtein.matches(a.collectionName, "the massacre") < 5)
    val i: Int = list.map(_.artistId).firstOption.getOrElse(-1)

  }
}