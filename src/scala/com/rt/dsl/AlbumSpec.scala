package rt.dsl

/**
 * DSL attempt charged with allowing a user so specify a list
 * of albums
 */
object AlbumSpec {
  def buildIndexFrom(containingFolder: String, albums: List[ArtistAlbums]): Unit = {
      
  }

  def artists(artists: ArtistAlbums*):List[ArtistAlbums]={
    artists.toList
  }

  def artist(artist: String, albums: List[Album]): ArtistAlbums = {
    return new ArtistAlbums(artist, albums)
  }

  def artist(artist: String, alias:String, albums: List[Album]): ArtistAlbums = {
    return new ArtistAlbums(artist, albums, alias)
  }

  def artistAllAlbums(artist: String, batchRating:Int): ArtistAlbums = {
    return new ArtistAlbums(artist, batchRating)
  }

  def artist(artist: String, alias: String, albums: Album*): ArtistAlbums = {
    return new ArtistAlbums(artist, albums.toList, alias)
  }

  def artist(artist: String, albums: Album*): ArtistAlbums = {
    return new ArtistAlbums(artist, albums.toList)
  }
}


case class ArtistAlbums(artist: String, albums: List[Album], batchRating:Int, alias:String){
  def this(artist: String, batchRating:Int) = this(artist, List[Album](), batchRating, null)
  def this(artist: String, batchRating:Int, alias:String) = this(artist, List[Album](), batchRating, alias)
  def this(artist: String, albums: List[Album]) = this(artist, albums, -1, null)
  def this(artist: String, albums: List[Album], alias:String) = this(artist, albums, -1, alias)

  def isArtistBatch():Boolean={
    batchRating > -1
  }

  //def withAlias(alias:String):ArtistAlbums
}
case class Album(title: String, pop:Int)