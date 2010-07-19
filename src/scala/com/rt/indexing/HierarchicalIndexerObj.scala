package com.rt.indexing

import com.rt.ohhla.OhhlaConfig
import java.lang.String
import com.rt.rhyme.RapSheetReader
import collection.immutable.Map
import java.io.{PrintWriter, BufferedWriter, FileWriter, FileOutputStream}
import collection.mutable.{MultiMap, ListBuffer, Set => MutableSet}

object HierarchicalIndexerObj{
  val indexer:HierarchicalIndexer = new HierarchicalIndexer()

  def main(args:Array[String]):Unit={

    //val toIndex: List[String] = OhhlaConfig.allArtistFolderNames
    val toIndex = List("NOTORIOUS_BIG")
    //val toIndex = List("EPMD")
    //val toIndex = List("BEASTIE_BOYS")
    //val toIndex = List("PUBLIC_ENEMY","NAS", "BEASTIE_BOYS", "JAYZ", "ICE_CUBE", "NOTORIOUS_BIG", "COMMON", "ATCQ",
    //val toIndex = List("PUBLIC_ENEMY","NAS", "BEASTIE_BOYS", "JAYZ", "ICE_CUBE", "NOTORIOUS_BIG")
     // "CYPRESS_HILL", "EMINEM", "DR_DRE", "ERIC_B_AND_RAKIM", "GANGSTARR", "KRSONE", "MADVILLAIN")
    //println("building hierarchy from "+toIndex.size+" folders")

    //persistIndex(indexer.makeArtistsHierarchy(toIndex))

    val node:AlbumNode = indexer.makeAlbumNodeForOneSong("C:\\data\\projects\\rapAttack\\rapAttack\\olhha\\NOTORIOUS_BIG\\READY_TO_DIE", 5);
    println(node)

    //val hierarchy: Map[String, ArtistNode] = indexer.makeArtistHierarchyWithAllWords(toIndex).index


    //
    //HibernateMain.main leaf: When Will They Shoot?: [Burnin our black skin, Buy my neighborhood - then push the crack in]
    //HibernateMain.main leaf: What Can I Do?: [Infest my hood with crack, cuz I'm the mack, Take a nation of millions to hold me back]
    //HibernateMain.main leaf: When Will They Shoot?: [Burnin our black skin, Buy my neighborhood - then push the crack in]
    //
//    val oneSong: AlbumNode = indexer.makeAlbumNodeForOneSong("""C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\ICE_CUBE\THE_PREDATOR""", 2)
//    val crackRhymes:List[RhymeLeaf] = oneSong.children.head.rhymes.filter(r => r.word == "CRACK")
//    println(crackRhymes)

    //bestRhymes(rhymeList(hierarchy.values.toList))
    //persistAllText(hierarchy.values.toList)



    //orderOnSuitability(hierarchy.values.toList).foreach(r => println(r.suitability+":"+r.rating+", "+r.lines))

    //TODO remove rhyme words with too many entries, have max at 40
    //TODO remove duplicated rhymes, they come from the same song on different albums
    //TODO consider building a rhyme group index 

    null
  }

  private def persistIndex(hierarchy:Map[String, ArtistNode])={
    println("serializing hieararchy...")
    indexer.serializeHierarcy(hierarchy)
    println("serialized hiearchy, calculating all ryhme parts...")
    val parts: List[String] = getAllRhymeParts(hierarchy.values.toList).sort((a, b) => (b.length > a.length))
    println("serialized hiearchy all rhyme parts, found "+parts.size+" rhymes...")
    indexer.serializeRhymedWords(parts)
  }

  def bestRhymes(rhymes:List[RhymeLeaf]):List[RhymeLeaf]={
    val sorted = rhymes.sort((a,b) => a.rating > b.rating)
    sorted.foreach(println)
    sorted
  }

  def orderOnSuitability(artistNodes:List[ArtistNode]):List[RhymeLeaf]={
    //var list = rhymeList(artistNodes).sort((a,b) => a.suitability > b.suitability)
    //list.foreach(r => {println(r.suitability+", "+r.lines)})

    val group: MultiMap[Int, RhymeLeaf] = groupBySuitabiliy(artistNodes)
    val orderedSuitabiliyt:List[(Int, MutableSet[RhymeLeaf])] = group.elements.toList.sort((a,b) => a._1 > b._1)
    orderedSuitabiliyt.foldLeft(List[RhymeLeaf]()){(list, e) =>
      list ++ bestRhymes(e._2.toList)
    }
  }

  private def groupBySuitabiliy(artistNodes:List[ArtistNode]):MultiMap[Int, RhymeLeaf]={
    val m = new scala.collection.mutable.HashMap[Int, MutableSet[RhymeLeaf]] with scala.collection.mutable.MultiMap[Int, RhymeLeaf]
    rhymeList(artistNodes).foreach(r => m.add(r.suitability, r))
    m
  }

  def rhymeStats(artistNodes:List[ArtistNode])={
    var rhymeCount:Int = 0
    var zeroPointRhymes:Int = 0;
    artistNodes.foreach(artist =>{
           artist.children.foreach(album =>{
             album.children.foreach(song =>{
               song.rhymes.foreach(rhyme =>{
                rhymeCount += 1
                 if(rhyme.rating == 0)zeroPointRhymes += 1
               })
             })
           })
        })

    println(rhymeCount+" ryhmes, "+zeroPointRhymes+" zero point rhymes, percent = "+((zeroPointRhymes.toDouble / rhymeCount.toDouble) * 100.0))
  }

  def rhymeList(artistNodes:List[ArtistNode]):List[RhymeLeaf]={
    val rhymeBuffer:ListBuffer[RhymeLeaf] = new ListBuffer[RhymeLeaf]();
    artistNodes.foreach(artist =>{
           artist.children.foreach(album =>{
             album.children.foreach(song =>{
               song.rhymes.foreach(rhyme =>{
                  rhymeBuffer.append(rhyme)
//                 rhyme.lines.foreach(line =>{
//                   out.println(line)
//                 })
                 //listBuffer.appendAll(rhyme.parts)
               })
             })
           })
        })
    rhymeBuffer.toList
  }

  def createArtistIndex(artistFolders:List[String]):Map[String, ArtistNode]={
    indexer.makeArtistsHierarchy(artistFolders)
  }

//  private def persistAllText(artistNodes:List[ArtistNode])=List[String]{
//   val file = System.getProperty("java.io.tmpdir")+"/allLines.txt"
//    //val out:PrintWriter = new PrintWriter(new FileWriter(file));
//    artistNodes.foreach(artist =>{
//      print(".")
//       artist.children.foreach(album =>{
//         album.children.foreach(song =>{
//           song.rhymes.foreach(rhyme =>{
//             rhyme.lines.foreach(line =>{
//               //out.println(line)
//             })
//             //listBuffer.appendAll(rhyme.parts)
//           })
//         })
//       })
//    })
//    //out.flush();out.close()
//  }

  private def getAllRhymeParts(artistNodes:List[ArtistNode]):List[String]={
    indexer.getAllRhymeParts(artistNodes)
  }
}