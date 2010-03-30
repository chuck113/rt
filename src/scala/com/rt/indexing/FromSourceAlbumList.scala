package com.rt.indexing

import java.io.File
import com.rt.ohhla.OhhlaConfig
import java.lang.String
import collection.immutable.Map
import persistence.{FileNameUtils, Album, ArtistAlbums}
import com.rt.util.{Strings, IO, MapUtils}

object FromSourceAlbumList{

  def main(args: Array[String]) = {
    val file:File = new File("""C:\data\projects\rapAttack\rapAttack\etc\sourceGreatestAlbums.txt""")
    val artists:List[String] = IO.fileLines(file).map(f => f.substring(0, f.indexOf('-'))).removeDuplicates
    val files: Map[String, File] = OhhlaConfig.allTransformedArtistNamesToFiles()

    val aliases:Map[String, String] = Map(
      "A Tribe Called Quest" -> "ATCQ",
      "Eric B. and Rakim" -> "Eric B. & Rakim",
      "Common Sense" -> "Common",
      "Gang Starr" -> "GangStarr"
      )
     null
    //val indexer:ActualArtistIndexer = new ActualArtistIndexer()
    //val fromMetaData: Map[String, List[Album]] = indexer.artistAlbumsFromMetaData()
    //indexer.dePunctuateKeys(fromMetaData).foreach(println)

//    val fromFolders: Map[String, File] = getArtistsFromFolders(files)
//    println(fromFolders.keys.toList)
//     artists.foreach(a => {
//      if(fromFolders.contains(a)){
//
//      } else{
//        println("no file for artist "+a)
//      }
//    })
  }


}