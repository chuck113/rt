//package com.rt.dsl
//
//import _root_.rt.dsl.AlbumSpec._
//import _root_.rt.dsl.{Album, ArtistAlbums}
//import java.lang.String
//import com.rt.ohhla.OhhlaConfig
//import com.rt.util.{NameMapper}
//import com.rt.indexing.persistence.{ArtistAlbums => AAlbums}
//import com.rt.indexing.{ArtistNode, HierarchicalIndexer}
//import collection.immutable.{Map1, Map}
//import scala.None
//
//abstract class AbstractDslFile{
//
//  private val indexer:HierarchicalIndexer = new HierarchicalIndexer()
//
//  def main(array:Array[String])={
//    val list: List[ArtistAlbums] = load()
//    println(list)
//    buildIndex(list)
//  }
//
//  def load():List[ArtistAlbums]
//
//  private def filterExistingFolders(artist:String, folders:List[String]):(List[String], List[String])={
//    folders.partition(OhhlaConfig.albumFolderExists(artist, _))
//  }
//
//  private def warnAboutAlbumsNotFound(artist:String, albumsNotFound:List[String])={
//    albumsNotFound.foreach(a => println("didn't find album for artist folder '"+artist+"' at folder "+a))
//  }
//
//  private def folderName(artist:ArtistAlbums):Option[String]={
//    val neutralizedName:String = NameMapper.nUnder(artist.artist)
//    //OhhlaConfig.artistFolder(neutralizedName).getOrElse(OhhlaConfig.artistFolder(NameMapper.neutralizeWithUnderscores(artist.alias)))
//
//    //TODO wierdness here, we get the full artist folder but only retrn the name of the folder, not the new path
//    OhhlaConfig.artistFolder(neutralizedName) match{
//      case Some(x) => Some(neutralizedName)
//      case None =>{
//        val neutralizedAlias: String = NameMapper.nUnder(artist.alias)
//        OhhlaConfig.artistFolder(neutralizedAlias) match{
//          case Some(x) => Some(neutralizedAlias)
//          case None => None
//        }
//      }
//    }
//  }
//
//  private def getArtistAlbumList(folderName:String, albums:List[Album]):List[String]={
//     val albumFolders:List[String] = albums.map(a => NameMapper.nUnder(a.title))
//     val existingNonExisting:(List[String], List[String]) = filterExistingFolders(folderName, albumFolders)
//     warnAboutAlbumsNotFound(folderName, existingNonExisting._2)
//     existingNonExisting._1
//  }
//
//  def buildIndex(artists: List[ArtistAlbums]):Unit={
//    println("artists: "+artists)
//    artists.foreach(artist =>{
//      //val neutralizedName:String = NameMapper.nUnder(artist.artist)
//      folderName(artist)match{
//        case Some(artistFolder)=> {
//           println("artist: "+artist.artist + " ("+artistFolder+")")
//            if(artist.isArtistBatch){
//              println("serializing albums for artist "+artist.artist)
//              val artistHierarchy: Map[String, ArtistNode] = indexer.makeArtistHierarchy(artistFolder)
//              indexer.serializeHierarcy(artistHierarchy)
//            }else{
//              val albumList:List[String] = getArtistAlbumList(artistFolder, artist.albums)
//              println("serializing albums: "+albumList)
//              val albumsHierarchy: ArtistNode = indexer.makeArtistAlbumsHierarchy(artistFolder, albumList)
//              indexer.serializeHierarcy(new Map1(artistFolder, albumsHierarchy))
//            }
//        }
//        case None => {
//          println("no artist for "+artist.artist+", checked folder "+NameMapper.nUnder(artist.artist))
//        }
//      }
//    })
//  }
//}