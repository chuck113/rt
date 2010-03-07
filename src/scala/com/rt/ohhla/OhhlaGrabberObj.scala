package ohhla

import java.io.{BufferedReader, FileReader}

object OhhlaGrabberObj {
  def main(args: Array[String]): Unit = {

    val artistName = "Beastie Boys"
    //val artistName = "Run-D.M.C."    
    //val artistName = "A Tribe Called Quest"
    //val artistName = "KRS-One"
    //val artistName = "Method Man"
    //val artistName = "MF Doom"
    //val artistName = "Nas"
    //val artistName = "Jay-Z"


    //BROKEN
    // val artistName = "Mos Def"
    //val artistName = "Public Enemy"
    
    //val fileName = """C:\data\projects\scala-play\rapAttack\resources\olhha-beastie-boys.html"""
    //val htmlString = grabber.htmlAsString(fileName)
    //persistArtists("MF Doom", "Method Man", "KRS-One")
    persistArtists(artistName)

  }

  private def persistArtists(artistNames: String*)={
    val grabber = new OhhlaGrabber()
    val persister = new OhhlaPersister()

    for (artistName <- artistNames.elements){
      val htmlString = grabber.artistPageContents(artistName) //htmlAsString(fileName)
      val info: ArtistInfo = grabber.artistAlbums(htmlString, artistName)
      println("artist albums are: "+info)
      persister.persistArtistFiles(info)
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