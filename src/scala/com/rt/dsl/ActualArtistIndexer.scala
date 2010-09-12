//package com.rt.indexing
//
//import java.io.File
//import com.rt.ohhla.OhhlaFiles
//import java.lang.String
//import collection.immutable.Map
//import persistence.{FileNameUtils, Album, ArtistAlbums}
//import com.rt.util.{Strings, IO, ScalaConversions}
//
//class ActualArtistIndexer{
//
//  def getArtistsFromFolders(folders: Map[String, File]):Map[String, File]={
//    folders.foldLeft(Map[String, File]()){(resMap, entry) => {
//      val artists: List[String] = getArtistAlbumsInArtistFolder(entry._2).removeDuplicates
//      ScalaConversions.addKeys(resMap, artists, entry._2)
//    }}
//  }
//
//  /**
//   * The folders albums are contained in are named after the artists on the ohhla site
//   * which groups related aritsts together, i.e. Qtip is in the tribe called quest folder.
//   * This method used the album meta data from each album to produce a map of the artists
//   * based on the artist in the meta data, which is the actual aritst (will be Qtip in the
//   * given example).
//   *
//   * returns all the found artists mapped to a pair of the artist folder and their albums
//   */
////  def artistAlbumsFromMetaData():Map[String, (String, List[Album])]={
////    val list: List[String] = OhhlaFiles.allArtistFolders
////    list.foldLeft(Map[String, (String, List[Album])]()){(map, folder)=>{
////      val albums:List[Album] = ArtistAlbums.fromFolderWithAlbumMetadata(folder).albums
////      val albumsArtists:Map[String, List[Album]] = getAlbumArtist(albums)
////      applyFolder(ScalaConversions.mergeListMaps(map, albumsArtists), folder)
////    }}
////  }
//
//  /**
//   * Returns the name of artists found form meta data mapped to the folders of the
//   * artists album
//   */
//  def artistAlbumFoldersFromMetaData():Map[String, List[String]]={
//    val list: List[String] = OhhlaFiles.allArtistFolders
//    list.foldLeft(Map[String, List[String]]()){(map, folder)=>{
//      val albums:List[Album] = ArtistAlbums.fromFolderWithAlbumMetadata(folder).albums
//      val albumsArtists:Map[String, List[String]] = getAlbumArtistMappedToFolders(albums)
//      ScalaConversions.mergeListMaps(map, albumsArtists)
//    }}
//  }
//
//  def applyFolder(in:Map[String, List[Album]], folder:String):Map[String, (String, List[Album])]={
//    in.foldLeft(Map[String, (String, List[Album])]()){(map, e)=> map(e._1) = folder -> e._2}
//  }
//
//  implicit def removePunctuation(str: String) = new StringMethods1(str)
//  implicit def replaceAmpersand(str: String) = new StringMethods2(str)
//
//  class StringMethods1(str:String) {
//    def removePunctuation():String= {
//      Strings.removePunctuation(str)
//    }
//  }
//
//  class StringMethods2(str:String) {
//    def replaceAmpersand():String= {
//      Strings.replaceAmpersand(str)
//    }
//  }
//
////  implicit def stringWrapper(s: String) =
////    new RandomAccessSeq[Char] {
////    def length = s.length
////    def apply(i: Int) = s.charAt(i)
////    }
//
//  def toFolderName(in:String):String={
//    in.replace(" ", "_")
//  }
//
//  def dePunctuate(in:String):String={
//    in.removePunctuation.toUpperCase.replaceAmpersand
//  }
//
//  def dePunctuateKeys[T](in:Map[String, T]):Map[String, T]={
//    mutateKeys(in, dePunctuate)
//  }
//
//  private def mutateKeys[T](in:Map[String, T], mutator:(String => String)):Map[String, T]={
//    in.foldLeft(Map[String, T]()){(result, e)=>{
//      result(mutator(e._1)) = e._2
//    }}
//    //map(e => function(e._1) -> e._2)
//  }
//
//  //.map(_.fileInfo.fileName)
//
//  def getAlbumArtistMappedToFolders(albums:List[Album]):Map[String, List[String]]={
//     albums.foldLeft(Map[String, List[String]]()){(res, album)=>{
//      res(album.metaData.artist) = res.getOrElse(album.metaData.artist, List[String]()) + album.fileInfo.fileName
//    }}
//  }
//
//  def getAlbumArtist(albums:List[Album]):Map[String, List[Album]]={
////    val map: Map[String, List[Album]] = ScalaConversions.toMap(albums._2, ((a: Album) => a.metaData.artist))
////    map.foldLeft(Map[String, (String, List[Album])]()){ (map, e)=>
////      map(e._1) = albums._1 -> e._2
////    }
//    albums.foldLeft(Map[String, List[Album]]()){(res, album)=>{
//      res(album.metaData.artist) = res.getOrElse(album.metaData.artist, List[Album]()) + album
//    }}
//  }
//
//  def getArtistAlbumsInArtistFolder2(artistFolder:File):List[String]={
//    val artistAlbums: ArtistAlbums = ArtistAlbums.fromFolderWithAlbumMetadata(artistFolder.getAbsolutePath)
//    artistAlbums.albums.map(a => {a.metaData.artist})
//  }
//
//  def getArtistAlbumsInArtistFolder(artistFolder:File):List[String]={
//    val artistAlbums: ArtistAlbums = ArtistAlbums.fromFolderWithAlbumMetadata(artistFolder.getAbsolutePath)
//    artistAlbums.albums.map(a => {a.metaData.artist})
//  }
//}