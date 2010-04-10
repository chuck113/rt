package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import java.lang.String
import com.rt.rhyme.RapSheetReader


object HierarchicalIndexerObj{
  val indexer:HierarchicalIndexer = new HierarchicalIndexer()

  def main(args:Array[String]):Unit={

    //val toIndex: List[String] = OhhlaConfig.allArtistFolderNames
    val toIndex = List("NOTORIOUS_BIG")

    //val toIndex = List("PUBLIC_ENEMY","NAS", "BEASTIE_BOYS", "JAYZ", "ICE_CUBE", "NOTORIOUS_BIG")
    indexer.serializeHierarcy(indexer.makeArtistsHierarchy(toIndex))

    //val toIndex:AlbumNode = buildArtistNodeForSong("NOTORIOUS_BIG", "READY_TO_DIE", 12)
    null
  }

  def buildArtistNodeForSong(artist:String, album:String, track:Int):AlbumNode={
    val albumFolder:String = OhhlaConfig.rawTargetLocation + "/" + artist +"/"+album
    indexer.makeAlbumNodeForOneSong(albumFolder, track)
  }
}