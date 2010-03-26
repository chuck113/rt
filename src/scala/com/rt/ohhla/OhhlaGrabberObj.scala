package com.rt.ohhla

import java.io.{BufferedReader, FileReader}
import com.rt.indexing.persistence.ArtistAlbums
import org.apache.commons.io.IOUtils

object OhhlaGrabberObj {
  def main(args: Array[String]): Unit = {

    val artists = List[String](
        "Beastie Boys"
//      "Run-D.M.C.",
//      "Jay-Z",
//      "A Tribe Called Quest",
//      "KRS-One",
//      "Method Man",
//      "MF Doom",
//      "Nas",
//      "Jay-Z"
      )

    val artistName = "Beastie Boys"
    //val artistName = "Run-D.M.C."    
    //val artistName = "A Tribe Called Quest"
    //val artistName = "KRS-One"
    //val artistName = "Method Man"
    //val artistName = "MF Doom"
    //val artistName = "Nas"
    //val artistName = "Jay-Z"
    //val artistName = "Ice Cube"
    persistArtists(artistName)

    //BROKEN
    // val artistName = "Mos Def"
    //val artistName = "Public Enemy"
    
    //val fileName = """C:\data\projects\scala-play\rapAttack\resources\olhha-beastie-boys.html"""
    //val htmlString = grabber.htmlAsString(fileName)
    //persistArtists("MF Doom", "Method Man", "KRS-One")



    val yfaArtists = findAllArtistsWithYFA()
    println("total: "+yfaArtists.size)
    yfaArtists.foreach(entry => {
      println(entry._2)
      persistArtists(entry._2)
    })
  }

  def getArtistAndLink(yfaLine:String):(String, String) ={
    val firstQuote = yfaLine.indexOf("\"")+1
    val secondQuote = yfaLine.indexOf("\"", firstQuote+1)
    val firstCloseAngleBracket = yfaLine.indexOf(">", secondQuote)+1
    val secondOpenAngleBracket = yfaLine.indexOf("<", firstCloseAngleBracket+1)
    yfaLine.substring(firstQuote, secondQuote) -> yfaLine.substring(firstCloseAngleBracket, secondOpenAngleBracket)
  }

  def findAllArtistsWithYFA():Map[String, String] ={
    val cpResources = OhhlaConfig.ohhlaLocalSiteAll.map(l => fileContent(OhhlaConfig.ohhlaLocalSiteRoot + "/" + l))
    //println("cpResources = " + cpResources)
    val allLines = cpResources.foldLeft(List[String]()) {(list, lines) => {list ::: lines}}
    val pairs = allLines.filter(_.contains("YFA_")).map(getArtistAndLink)
    pairs.foldLeft(Map[String, String]()){(map, pair) => {map(pair._1) = pair._2}};
  }
   //tmp
   private def fileContent(classpathFile: String): List[String] = {
    //println("classpathFile is " + classpathFile)
    //println("classpathFile resource is " + getClass().getClassLoader().getResourceAsStream(classpathFile))
    //val in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(classpathFile))
    val javaList = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(classpathFile));
    List.fromArray((javaList.toArray)).asInstanceOf[List[String]]
  }

  private def persistArtists(artistNames: String*)={
    val grabber = new OhhlaGrabber()
    val persister = new OhhlaPersister()

    for (artistName <- artistNames.elements){
//      val htmlString = grabber.artistPageContents(artistName)
//      val info: ArtistAlbums = grabber.artistAlbums(htmlString, artistName)
//      persister.persistArtistFiles(info)

      grabber.artistPageContents(artistName) match {
        case Some(htmlString) =>{
            val info: ArtistAlbums = grabber.artistAlbums(htmlString, artistName)
            persister.persistArtistFiles(info)
        }
        case None => println("no albums for "+artistName)
      }
    }
  }


  // Source.fromFile fails with bufferUnderrun
  private def htmlAsString(fileName: String): String = {
    val reader = new BufferedReader(new FileReader(fileName));
    val b = new StringBuilder();
    var line: String = "";
    while (line != null) {
      line = reader.readLine()
      if (line != null) {
        b.append(line + "\n")
      }
    }
    b.toString
  }
}