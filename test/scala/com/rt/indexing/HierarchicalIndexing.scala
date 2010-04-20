package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import org.junit.{Before, Test}


class HierarchicalIndexingTest {
  private var indexer: HierarchicalIndexer = null

  @Before def setUp() {
    indexer = new HierarchicalIndexer()
  }

  @Test def indexOneSong {
    val toIndex:AlbumNode = buildArtistNodeForSong("EPMD", "UNFINISHED_BUSINESS", 1)
    null
  }

  def buildArtistNodeForSong(artist: String, album: String, track: Int): AlbumNode = {
    val albumFolder: String = OhhlaConfig.rawTargetLocation + "/" + artist + "/" + album
    indexer.makeAlbumNodeForOneSong(albumFolder, track)
  }
}