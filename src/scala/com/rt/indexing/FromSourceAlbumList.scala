package com.rt.indexing

import java.io.File
import java.lang.String
import collection.immutable.Map
import com.rt.util.{NameMapper, IO, MapUtils}
import com.rt.ohhla.{OhhlaPersister, OhhlaStreamBuilderImpl, AnonymousAlbumGrabber, OhhlaConfig}

/**
 * Reads the albums on the Source's greatest albums list and downloads all extra artists. Assumes
 * the YFA artists are all ready downloaded and downloads the anon ones.
 */
object FromSourceAlbumList {

  val grabber:AnonymousAlbumGrabber = new AnonymousAlbumGrabber(new OhhlaStreamBuilderImpl())
  val persister:OhhlaPersister = new OhhlaPersister()
  
  def extras():List[String]={
    List("Ice-T",
    "Immortal Technique",
    "Jeru the Damaja",
    "Ja Rule",
    "Jedi Mind Tricks",
    "The Juggaknots",
    "J-Zone",
    "Mr. Lif",
    "M.O.P",
    "Kanye West")
  }

  def main(args: Array[String]): Unit = {
    val file: File = new File("""C:\data\projects\rapAttack\rapAttack\etc\sourceGreatestAlbums.txt""")
    val artists: List[String] = IO.fileLines(file).map(f => f.substring(0, f.indexOf('-'))).removeDuplicates ++ extras()
    val artistsToFolderNames: Map[String, String] = MapUtils.toMap(artists, ((s: String) => NameMapper.nUnder(s)))
    //val files: Map[String, File] = OhhlaConfig.allTransformedArtistNamesToFiles()
    val files: Map[String, File] = OhhlaConfig.allArtistFoldersToFiles()

    val aliases: Map[String, String] = Map(
      "A_TRIBE_CALLED_QUEST" -> "ATCQ",
      "GANG_STARR" -> "GANGSTARR",
      "WU" -> "WUTANG_CLAN",
      "DRDRE" -> "DR_DRE",
      "ULTRAMAGNETIC_MCS_" -> "ULTRAMAGNETIC_MCS",
      "TUPAC" -> "2PAC",
      "TOO_SHORT" -> "TOO_HORT",
      "ROOTS" -> "THE_ROOTS",
      "OUTCAST" -> "OUTKAST",
      "KRS_ONE" -> "KRSONE",
      "JAZZY_JEFF_AND_THE_FRESH_PRINCE" -> "DJ_JAZZY_JEFF_AND_THE_FRESH_PRINCE",
      "BOOGIE_DOWN_PRODUCTIONS" -> "BDP",
      "RUN_DMC" -> "RUNDMC",
      "COMMON_SENSE" -> "COMMON",
      "JAY" -> "JAYZ"
      )

    val mulipleFolders: Map[String, List[String]] = Map(
      "SNOOP_DOGGY_DOG" -> List("SNOOP_DOGGY_DOGG", "SNOOP_DOGG"),
      "2PAC" -> List("2PAC", "TUPAC_SHAKUR")
      )

    // notable missing
    // Ice T, Immortal Technique, Mr Lif, Kanye West

    //Ice-T
    //Immortal Technique
    //Jeru the Damaja
    //Ja Rule
    //Jedi Mind Tricks
    //The Juggaknots
    //J-Zone
    //Mr. Lif
    //M.O.P
    //Kanye West

    // albums to check: WuTang, Gza

    var notFound = List[String]()

    artistsToFolderNames.foreach(entry => {
      val folderName = entry._1
      val artistName = entry._2
      val folder: File = getFolder(folderName, files, aliases)
      if (folder == null) {
        println("didn't find " + folderName + " for artist " + artistName)
        notFound = artistName :: notFound
      } else {
        println("found " + folderName + " in " + folder)
      }
    })

    val anonAliasMap:Map[String, String] = Map(
      "Genius/GZA" -> "GZA",
      "Heavy D & The Boyz" -> "Heavy D",
      "Ice T" -> "Ice-T",
      "Smif N Wessun" -> "Smif-N-Wessun",
      "Salt N Pepa" -> "Salt 'n' Pepa",
      "Eazy E" -> "Eazy-E",
      "Biz Markie" -> "BizMarkie",
      "Organized Confusion" -> "Organized Konfusion",
      "Diamond D" -> "Diamond (D)",
      "Stestasonic" -> "Stetsasonic",
      "Raekwon" -> "Raekwon the Chef",
      "Ol'Dirty Bastard" -> "Ol Dirty Bastard",
      "Bone Thugs and Harmony" -> "Bone Thugs-N-Harmony",
      "Schooly D" -> "Schoolly D",
      "Souls Of Mischief" -> "Souls of Mischief",
      "D.O.C." -> "D.O.C., The",
      "DJ Quick" -> "DJ Quik",
      "Spice 1" -> "Spice-1",
      "X" -> "X-Clan"
      )

    notFound.foreach(a => {
      //val urlForArtist: Option[String] = OhhlaConfig.urlForArtist(a)
      getArtistUrl(a, anonAliasMap) match {
        case None => println("didn't find artist for " + a)
        case Some(url) => {downloadArtistFromUrl(a, url)} //println(" -----  found "+a)
      }
    })
    null
  }

  private def downloadArtistFromUrl(artistName:String, url:String):Unit={
    grabber.getArtistAlbumsFromUrl(artistName, url).foreach(persister.persistArtistFiles(_))

//    val fromUrl: Option[ArtistAlbums] = grabber.getArtistAlbumsFromUrl(artistName, url)
//    fromUrl match{
//      case Some(urlToDownload) => persister.persistArtistFiles(fromUrl)
//    }
  }

  private def getArtistUrl(rawName:String, aliases:Map[String, String]):Option[String]={
   val name = aliases.getOrElse(rawName, rawName)
   OhhlaConfig.urlForArtist(name)
//   val urlForArtist: Option[String] = OhhlaConfig.urlForArtist(name)
//      urlForArtist match {
//        case None => println("no alias for: "+name);None
//        case Some(url) => {
//          //println(" -----  found through alias"+url);Some(url)
//          grabber.getArtistAlbumsFromUrl(name, url).map
//        }
//      }
  }

  private def getFolder(artist: String, ohhlaFiles: Map[String, File], aliases: Map[String, String]): File = {
    if (!ohhlaFiles.contains(artist)) {
      if (!aliases.contains(artist)) {
        null
      } else {
        ohhlaFiles(aliases(artist))
      }
    } else {
      //println(" ---------------------- found "+artist)
      ohhlaFiles(artist)
    }
  }

}