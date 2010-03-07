package ohhla

import io.Source
import org.apache.commons.io.IOUtils
import scala.util.matching.Regex.MatchIterator

case class AlbumMetaData(artist: String, title: String, year: String)
case class AlbumTrack(title: String, number: Int, url: String)
case class AlbumInfo(metaData: AlbumMetaData, tracks: List[AlbumTrack]);
case class ArtistInfo(artist: String, albums: List[AlbumInfo]);

class OhhlaGrabber {
  //val olhhaUrl = "http://ohhla.com"

  //val rawTargetLocation = """C:\data\projects\rhyme-0.9\olhha"""

  //  private def source(): Source = {
  //    return Source.fromFile("""C:\data\projects\scala-play\rapAttack\resources\olhha-index.html""");
  //  }

  //def downloadAlbum



  def artistAlbums(htmlString: String, artist: String): ArtistInfo = {
    val tableStart = """<table border=1 cellspacing=2 bordercolor=#000000 cellpadding=1 width=100%>"""
    val tableEnd = """</table>"""

    //val source = Source.fromFile("""C:\data\projects\scala-play\rapAttack\resources\olhha-beastie-boys.html""");
    //(new FileInputStream(fileName))

    //println(b.toString())

    //    <table border=1 cellspacing=2 bordercolor=#000000 cellpadding=1 width=100%>
    //    <TH COLSPAN=3 ALIGN=CENTER>Beastie Boys - Pollywog Stew EP (1982) <a
    //            href="http://www.amazon.com/gp/product/B000002UST?ie=UTF8&tag=theorihiphopl-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=B000002UST">BUY
    //        NOW!</a><img src="http://www.assoc-amazon.com/e/ir?t=theorihiphopl-20&l=as2&o=1&a=B000002UST" width="1"
    //                     height="1" border="0" alt="" style="border:none !important; margin:0px !important;"/>
    //    </TH>
    //    <tr>
    //        <td width="50" align=left valign=top><u>Track</u></td>
    //        <td width="350" align=left valign=top><u>Lyrics</u></td>
    //    </tr>

    val regex: String = tableStart + "[\\s\\S]+?" + tableEnd
    val matchIter = regex.r.findAllIn(htmlString)
    if (matchIter.hasNext) {
      //println("found albums: "+matchIter.toList+" from "+htmlString)           
      fromProperPage(matchIter, artist)
    } else {
      println("NO ALBUMS FOUND FOR ARTIST "+artist)
      //TODO deal with broken lyrics like public enemy 
      new ArtistInfo(artist, List[AlbumInfo]())
    }
  }

  // a proper page is something like the beastie boy page
  private def fromProperPage(matchIter: MatchIterator, artist: String): ArtistInfo = {
    val albums = matchIter.map[AlbumInfo](albumHtml => {
      val headerOpt = getAlbumHeader(albumHtml)
      headerOpt match {
        case None => println("WARN - didn't find album header in album html: (not included)"); null
        case _ => {
          val tracksStart = """<tr>"""
          val tracksEnd = """</tr>"""
          val tracksRegex = tracksStart + "[\\s\\S]+?" + tracksEnd
          val tracksIter = tracksRegex.r.findAllIn(albumHtml)
          val metaOpt = makeAlbumMetaData(headerOpt.get)
          if (tracksIter.hasNext && metaOpt.isDefined) {
            //val tracks = buildTracks(tracksIter)
            new AlbumInfo(metaOpt.get, buildTracks(tracksIter))
          } else {
            null
          }
        }
      }
    })
    new ArtistInfo(artist, albums.filter(_ != null).toList)
  }

  private def buildTracks(tracksIter: MatchIterator): List[AlbumTrack] = {
    tracksIter.foldLeft(List[AlbumTrack]()) {
      (list, track) => {
        val trackInfo = getTitleAndLink(track)
        trackInfo match {
          case None => list
          case _ => trackInfo.get :: list
        }
      }
    }
  }

  private def findTrackNumber(trackHtml: String): Option[Int] = {
    val trackNoStart = "<td"
    val trackNoEnd = "</td>"
    val trackNoRegex = trackNoStart + "[\\s\\S]+?" + trackNoEnd
    val trackNoOpt = trackNoRegex.r.findFirstIn(trackHtml)
    trackNoOpt match{
      case None => {println("got no track number for " + trackHtml); None}
      case _ => {
        println("got trackNoOpt "+trackNoOpt.get)
        val c = trackNoOpt.get.substring(trackNoOpt.get.indexOf('>')+1, trackNoOpt.get.lastIndexOf('<'))
        println("no is "+c)
        Some(removeNonNumberChars(c))
      }
    }
  }

  private def removeNonNumberChars(st: String): Int = {
    st.toArray.elements.foldLeft(new StringBuilder()) {
      (b, c) => {
        try {
         new Integer(c+"")
          b.append(c)
        } catch {
          case ex: NumberFormatException => b.append("")
        }
      }
    }.toString.toInt
  }

  private def getTitleAndLink(trackHtml: String): Option[AlbumTrack] = {
    println("Track html is "+trackHtml)

    val trackStart = "<a href=\""
    val trackEnd = "</a>"
    val trackRegex = trackStart + "[\\s\\S]+?" + trackEnd
    val trackOpt = trackRegex.r.findFirstIn(trackHtml)
    trackOpt match {
      case None => {println("got none for " + trackHtml); None}
      case _ => {
        val trackString = trackOpt.get
        println("trackString: " + trackString)
        val number: Int = findTrackNumber(trackHtml).get
        val lastQuote = trackString.lastIndexOf("\"");
        val firstQuote = trackString.indexOf("\"");
        val title = trackString.substring(lastQuote + 2, trackString.length - trackEnd.length)
        val link = trackString.substring(firstQuote + 1, lastQuote)
        Some(new AlbumTrack(title, number, link))
      }
    }
  }

  private def getTrackNumber(trackHtml: String): Option[Int] = {
    val No = """<td align=left valign=top>([0-9]+?)</td>""".r
    trackHtml.trim match {
      case No(n) => Some(n.toInt)
    }
  }

  private def getAlbumHeader(albumHtml: String): Option[String] = {
    val albumHeaderStart = """<TH COLSPAN=3 ALIGN=CENTER>"""
    val albumHeaderEnd = """<"""
    val albumHeaderRegex = albumHeaderStart + "[\\s\\S]+?" + albumHeaderEnd

    val headerMatch = albumHeaderRegex.r.findFirstIn(albumHtml)
    headerMatch match {
      case None => None
      case _ => {
        val header = headerMatch.get.substring(albumHeaderStart.length, (headerMatch.get.length - albumHeaderEnd.length))
        if (!header.contains("Remix") && !header.contains("Miscellaneous")) {
          Some(header)
        } else {
          None
        }
      }
    }
  }

  //  private def getTrackNumber(trackHtml: String): Option[Int] = {
  //    val No = """<td align=left valign=top>([0-9]+?)</td>""".r
  //    trackHtml.trim match {
  //      case No(n) => Some(n.toInt)
  //    }
  //  }

  private def betweenFirstAndLastQuotes(line: String): String = {
    val first = line.indexOf("\"")
    val last = line.lastIndexOf("\"")
    line.substring(first + 1, last);
  }

  private def fileContent(classpathFile: String): List[String] = {
    println("classpathFile is " + classpathFile)
    println("classpathFile resource is " + getClass().getClassLoader().getResourceAsStream(classpathFile))
    //val in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(classpathFile))
    val javaList = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(classpathFile));
    List.fromArray((javaList.toArray)).asInstanceOf[List[String]]
  }

  def artistPageContents(artistName: String): String = {
    println("artist url is " + urlForArtist(artistName))
    Source.fromURL("http://ohhla.com/" + urlForArtist(artistName)).getLines.toList.foldLeft("")(_ + _)
  }

  private def urlForArtist(artistName: String): String = {
    val cpResources = OhhlaConfig.ohhlaLocalSiteAll.map(l => fileContent(OhhlaConfig.ohhlaLocalSiteRoot + "/" + l))
    println("cpResources = " + cpResources)
    val allLines = cpResources.foldLeft(List[String]()) {(list, lines) => {list ::: lines}}
    def res = allLines.filter(_.contains(">" + artistName + "<"));
    println("found: " + res)
    res.length match {
      case 0 => null
      case 1 => betweenFirstAndLastQuotes(res.head)
    }
  }

  // regex on http://daily-scala.blogspot.com/2009/09/matching-regular-expressions.html
  private def makeAlbumMetaData(htmlTitle: String): Option[AlbumMetaData] = {
    println("html is '" + htmlTitle + "'")
    val Name = """(.+?) - (.+?) [\\(\\)](.+?)[\\)\\)]""".r
    htmlTitle.trim match {
      case Name(a, t, y) => Some(new AlbumMetaData(a, t, y))
      case _ => println("could not get album meta data from string '" + htmlTitle + "'"); None
    }
  }

  def mapOnAlbumName(albums: List[AlbumInfo]): Map[String, AlbumInfo] = {
    albums.foldLeft(Map[String, AlbumInfo]()) {
      (map, a) => {
        map(a.metaData.title) = a
      }
    }
  }

  def regexTest2() = {
    def wu = "Wu-Tang Clan - Enter the Wu-Tang: 36 Chambers (1993)"
    println(makeAlbumMetaData(wu))
  }


  def regexTest() = {
    val albumHeaderStart = """start etc"""
    val albumHeaderEnd = """ending"""
    val albumHeaderRegex = albumHeaderStart + "([\\s\\S]+?)" + albumHeaderEnd
    println(albumHeaderRegex.r.findFirstIn("start etc this please ending").get)
  }
}