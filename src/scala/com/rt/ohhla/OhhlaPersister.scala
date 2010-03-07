package ohhla

import java.io._
import xml.{XML, PrettyPrinter}
import io.Source
import org.apache.commons.io.IOUtils
import java.net.URL

/**
 * Created by IntelliJ IDEA.
 * User: ck
 * Date: 09-Feb-2010
 * Time: 11:42:59
 * To change this template use File | Settings | File Templates.
 */

class OhhlaPersister{

  //def persistArtistAlbumFromFile(info: ArtistInfo)(folderGenerator : String => String) ={
  def persistArtistFiles(info: ArtistInfo):Unit ={
    val targetFolder = OhhlaConfig.rawTargetLocation+"/"+info.artist
    new File(targetFolder).mkdirs
    info.albums.foreach(entry => {
      persistAlbum(entry, targetFolder)
    })

    val st = new PrettyPrinter(120, 2).format(originalAlbumNamesXml(info));
    savePretty(targetFolder+"/titles.xml", st)
  }

  private def toFileName(st: String):String ={
    st.replace(" ", "_").replace("\\", "_").replace("/", "_").replace(".", "_").replace(":", "_").replace(";", "_").replace("*", "_S").replace("?", "_Q")
  }

  private def persistAlbum(albumInfo: AlbumInfo, targetArtistFolder: String):Unit = { // return xml
    //val modifiedFileName = toFileName(albumInfo.metaData.title)
    val targetFolder = targetArtistFolder+"/"+toFileName(albumInfo.metaData.title)
    new File(targetFolder).mkdirs;
    serializeAlbum(albumInfo, targetFolder)
    downloadTracks(albumInfo.tracks, targetFolder)
  }

  private def downloadTracks(tracks: List[AlbumTrack], targetFolder: String): Unit = {
    println("will download "+tracks+" to "+targetFolder)
    val iter = tracks.elements.counted
    iter.foreach(track => {
        val fileName = targetFolder+"/"+iter.count+".txt"
        println("starting download for "+OhhlaConfig.ohhlaUrl+"/"+track.url +" to " + fileName)
        if(!new File(fileName).exists){

          //IOUtils.copy(new URL(OhhlaConfig.ohhlaUrl+"/"+track.url).openStream, new FileWriter(fileName))
          val writer = new BufferedWriter(new FileWriter(fileName))
          Source.fromURL(OhhlaConfig.ohhlaUrl+"/"+track.url).getLines.foreach(line =>{
            writer.write(line)
          })
          writer.flush;writer.close();
        }else{
          println("skipping "+fileName+" as already exists")
        }
    })
  }

  private def serializeAlbum(albumInfo: AlbumInfo, rootFolder: String) = {
    //println("rootFolder: "+rootFolder)
    //println("mkdirs: "+new File(rootFolder).mkdirs());
    println("saving to "+rootFolder+"/md.xml")
    XML.save(rootFolder+"/md.xml", albumXml(albumInfo))
  }    

  private def albumXml(albumInfo: AlbumInfo) = {
   // val iter = albumInfo.tracks.elements.counted
    <album artist={albumInfo.metaData.artist}
           title={albumInfo.metaData.title}
           year={albumInfo.metaData.year}>
      {albumInfo.tracks.map(track => {
        <track url={track.url} num={track.number.toString} title={track.title}></track>
      })
      }
    </album>;
  }

  //TODO pass in toFileName function with extra param
  private def originalAlbumNamesXml(artistInfo: ArtistInfo) ={
    <artist name={artistInfo.artist}>
      {artistInfo.albums.map {albumElement} }
    </artist>;
  }

  private def albumElement(album: AlbumInfo)={
      <album filename={toFileName(album.metaData.title)} name={album.metaData.title}/>
  }


  private def savePretty(filename: String, xmlString: String) {
    var fos: FileOutputStream = null
    var w: Writer = null
    try {
      // using NIO classes of JDK 1.4
      import java.io.{FileOutputStream, Writer}
      import java.nio.channels.{Channels, FileChannel}

      fos = new FileOutputStream(filename)
      w = Channels.newWriter(fos.getChannel(), "UTF-8")
      w.write(xmlString)
    }catch {
      case ex: IOException => println("got exception "+ex)
    } finally {
      w.close()
      fos.close()
    }
  }
}