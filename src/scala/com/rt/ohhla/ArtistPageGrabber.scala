package com.rt.ohhla


import io.Source
import org.apache.commons.io.IOUtils
import scala.util.matching.Regex.MatchIterator
import com.rt.indexing.persistence.{ArtistAlbums, AlbumMetaData, Album, AlbumTrack}
import java.net.URLDecoder
import util.IO
import org.slf4j.{Logger, LoggerFactory}
import java.io.InputStream
import com.rt.util.IO

class ArtistPageGrabber(val streamBuilder:OhhlaStreamBuilder) extends AbstractGrabber{

  private val LOG:Logger = LoggerFactory.getLogger(classOf[ArtistPageGrabber])

  override def getArtistAlbumsFromUrl(artist: String, artistUrl: String): Option[ArtistAlbums] = {
    try{
      val inputStream: InputStream = streamBuilder.fromUrlPrefix(artistUrl)
      val htmlString:String = IO.fileAsString(inputStream)
      artistAlbums(htmlString, artist)
    }catch {
      case e:Exception => println("could not get html at "+artistUrl+" due to "+e.getMessage);None
    }
  }

  private def artistAlbums(htmlString: String, artist: String): Option[ArtistAlbums] = {
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
      Some(fromProperPage(matchIter, artist))
    } else {
      LOG.warn("NO ALBUMS FOUND FOR ARTIST "+artist)
      None
    }
  }

  // a proper page is something like the beastie boy page
  private def fromProperPage(matchIter: MatchIterator, artist: String): ArtistAlbums = {
    //TODO fold left instead of matchIter.map to avoid nulls at end
    val albums = matchIter.map[Album](albumHtml => {
      val headerOpt = getAlbumHeader(albumHtml)
      headerOpt match {
        case None => LOG.warn("didn't find album header in album html: \n"+albumHtml); null
        case _ => {
          val tracksStart = """<tr>"""
          val tracksEnd = """</tr>"""
          val tracksRegex = tracksStart + "[\\s\\S]+?" + tracksEnd
          val tracksIter = tracksRegex.r.findAllIn(albumHtml)

          if (tracksIter.hasNext) {
            val metaOpt = makeAlbumMetaData(headerOpt.get, buildTracks(tracksIter))
            if(metaOpt.isDefined){
              new Album(metaOpt.get)
            }else{
              null
            }
          } else {
            null
          }
        }
      }
    })
    new ArtistAlbums(artist, albums.filter(_ != null).toList)
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
      case None => {LOG.warn("got no track number for " + trackHtml); None}
      case _ => {
        val c = trackNoOpt.get.substring(trackNoOpt.get.indexOf('>')+1, trackNoOpt.get.lastIndexOf('<'))
        Some(removeNonNumberChars(c))
      }
    }
  }

  private def removeNonNumberChars(st: String): Int = {
    val res:String = st.toArray.elements.foldLeft(new StringBuilder()) {
      (b, c) => {
        try {
         new Integer(c+"")
          b.append(c)
        } catch {
          case ex: NumberFormatException => b.append("")
        }
      }
    }.toString

    if(res.length == 0)99
    else res.toInt
  }

  private def getTitleAndLink(trackHtml: String): Option[AlbumTrack] = {
    println("Track html is "+trackHtml)

    val trackStart = "<a href=\""
    val trackEnd = "</a>"
    val trackRegex = trackStart + "[\\s\\S]+?" + trackEnd
    val trackOpt = trackRegex.r.findFirstIn(trackHtml)
    trackOpt match {
      case None => None
      case _ => {
        val trackString = trackOpt.get
        //println("trackString: " + trackString)
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
      case None => LOG.warn("no match found using regex '"+albumHeaderRegex+"'");None
      case _ => {
        val header = headerMatch.get.substring(albumHeaderStart.length, (headerMatch.get.length - albumHeaderEnd.length))
        (!header.contains("Remix") && !header.contains("Miscellaneous")) match{
          case true => Some(header)
          case false => None
        }
      }
    }
  }

  //TODO use some library to do this properly for all html
  private def stripAmps(html:String):String = {
    html.replace("&amp;", "&").replace("&amp", "&")
  }

  // regex on http://daily-scala.blogspot.com/2009/09/matching-regular-expressions.html
  private def makeAlbumMetaData(htmlTitleEncoded: String, tracks:List[AlbumTrack]): Option[AlbumMetaData] = {    
    val htmlTitle = stripAmps(htmlTitleEncoded)
    println("html is '" + htmlTitle + "'")
    val Name = """(.+?) - (.+?) [\\(\\)](.+?)[\\)\\)]""".r
    htmlTitle.trim match {
      case Name(a, t, y) => Some(new AlbumMetaData(a, t, removeNonNumberChars(y).toInt, tracks))
      case _ => LOG.warn("could not get album meta data from string '" + htmlTitle + "'"); None
    }
  }

//  def mapOnAlbumName(albums: List[Album]): Map[String, Album] = {
//    albums.foldLeft(Map[String, Album]()) {
//      (map, a) => {
//        map(a.metaData.title) = a
//      }
//    }
//  }

//  def regexTest2() = {
//    def wu = "Wu-Tang Clan - Enter the Wu-Tang: 36 Chambers (1993)"
//    println(makeAlbumMetaData(wu))
//  }
//
//
//  def regexTest() = {
//    val albumHeaderStart = """start etc"""
//    val albumHeaderEnd = """ending"""
//    val albumHeaderRegex = albumHeaderStart + "([\\s\\S]+?)" + albumHeaderEnd
//    println(albumHeaderRegex.r.findFirstIn("start etc this please ending").get)
//  }
}