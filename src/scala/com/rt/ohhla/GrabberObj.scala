package com.rt.ohhla



object GrabberObj{
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

    //val artistName = "Beastie Boys"
    //val artistName = "Run-D.M.C."
    //val artistName = "A Tribe Called Quest"
    //val artistName = "KRS-One"
    //val artistName = "Method Man"
    //val artistName = "MF Doom"
    //val artistName = "Nas"
    //val artistName = "Jay-Z"
    //val artistName = "Ice Cube"
    //val artistName = "Cypress Hill"
    //val artistName = "Public Enemy"
    //val artistName = "Eric B. & Rakim".replace("&", "&amp;")
    //persistDirect("Eric B. & Rakim".replace("&", "&amp;"), "YFA_rakim.html")
    //val artistName = "De La Soul"


    val artistName = "ice t"
    persistArtists(artistName)

    //BROKEN
    // val artistName = "Mos Def"
    //val artistName = "Public Enemy"

    //val fileName = """C:\data\projects\scala-play\rapAttack\resources\olhha-beastie-boys.html"""
    //val htmlString = IO.fileAsString(fileName)
    //persistArtists("MF Doom", "Method Man", "KRS-One")



    val yfaArtists = findAllArtistsWithYFA()
        println("total: "+yfaArtists.size)
        yfaArtists.foreach(entry => {
          println(entry._2)
          persistArtists(entry._2)
        })

    null
  }

  def findAllArtistsWithYFA(): Map[String, String] = {
    val pairs = OhhlaFiles.allOhhlaIndexes.filter(_.contains("YFA_")).map(ParsingUtils.hrefLinkAndtitle)
    pairs.foldLeft(Map[String, String]()) {(map, pair) => {map(pair._1) = pair._2}};
  }

  private def persistDirect(artist: String, artistUrl: String) = {
    val persister = new OhhlaPersister()
    val yfaGrabber: ArtistPageGrabber = new ArtistPageGrabber(new OhhlaStreamBuilderImpl())
    yfaGrabber.getArtistAlbumsFromUrl(artist, artistUrl).foreach(persister.persistArtistFiles(_))
  }

  private def persistArtists(artistNames: String*) = {
    val grabber = new Grabber(new OhhlaStreamBuilderImpl())
    val persister = new OhhlaPersister()

    for (artistName <- artistNames.elements) {
      grabber.getArtistAlbums(artistName).foreach(persister.persistArtistFiles(_))
    }
  }
}