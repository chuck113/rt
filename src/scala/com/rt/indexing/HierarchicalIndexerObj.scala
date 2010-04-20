package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import java.lang.String
import com.rt.rhyme.RapSheetReader
import collection.mutable.ListBuffer
import collection.immutable.Map
import java.io.{PrintWriter, BufferedWriter, FileWriter, FileOutputStream}

object HierarchicalIndexerObj{
  val indexer:HierarchicalIndexer = new HierarchicalIndexer()

  def main(args:Array[String]):Unit={

    //val toIndex: List[String] = OhhlaConfig.allArtistFolderNames
    //val toIndex = List("EPMD")
    //val toIndex = List("BEASTIE_BOYS")
    val toIndex = List("PUBLIC_ENEMY","NAS", "BEASTIE_BOYS", "JAYZ", "ICE_CUBE", "NOTORIOUS_BIG", "COMMON", "ATCQ",
      "CYPRESS_HILL", "EMINEM", "DR_DRE", "ERIC_B_AND_RAKIM", "GANGSTARR", "KRSONE", "MADVILLAIN")
    println("building hierarchy from "+toIndex.size+" folders")
    val hierarchy: Map[String, ArtistNode] = indexer.makeArtistsHierarchy(toIndex)
    println("built hierarchy for "+hierarchy.keySet.size+" artists")
    
    persistAllText(hierarchy.values.toList)


//    prtinln("serializing hieararchy...")
//    indexer.serializeHierarcy(hierarchy)
//    println("serialized hiearchy, calculating all ryhme parts...")
//    val parts: List[String] = getAllRhymeParts(hierarchy.values.toList).sort((a, b) => (b.length > a.length))
//    println("serialized hiearchy all rhyme parts, found "+parts.size+" rhymes...")
//    indexer.serializeRhymedWords(parts)

    //TODO remove rhyme words with too many entries, have max at 40
    //TODO remove duplicated rhymes, they come from the same song on different albums
    //TODO consider building a rhyme group index 

    null
  }

  def createArtistIndex(artistFolders:List[String]):Map[String, ArtistNode]={
    indexer.makeArtistsHierarchy(artistFolders)
  }

  private def persistAllText(artistNodes:List[ArtistNode])={
   val file = System.getProperty("java.io.tmpdir")+"/allLines.txt"
    val out:PrintWriter = new PrintWriter(new FileWriter(file));
    artistNodes.foreach(artist =>{
      print(".")
       artist.children.foreach(album =>{
         album.children.foreach(song =>{
           song.rhymes.foreach(rhyme =>{
             rhyme.lines.foreach(line =>{
               out.println(line)
             })
             //listBuffer.appendAll(rhyme.parts)
           })
         })
       })
    })
    out.flush();out.close()
  }

  private def getAllRhymeParts(artistNodes:List[ArtistNode]):List[String]={
    val listBuffer:ListBuffer[String] = new ListBuffer[String]()
    artistNodes.foreach(artist =>{
      print(".")
       artist.children.foreach(album =>{
         album.children.foreach(song =>{
           song.rhymes.foreach(rhyme =>{
             listBuffer.appendAll(rhyme.parts)
           })
         })
       })
    })

    listBuffer.toList.removeDuplicates
  }
}