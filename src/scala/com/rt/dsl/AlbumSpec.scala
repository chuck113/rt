package rt.dsl

/**
 * DSL attempt charged with allowing a user so specify a list
 * of albums
 */
object AlbumSpec {
  def buildIndexFrom(containingFolder: String, albums: List[ArtistAlbums]): Unit = {
      
  }

  def artist(artist: String, albumNames: List[String]): ArtistAlbums = {
    return ArtistAlbums(artist, albumNames)
  }

  def albums(albums: String*): List[String] = {
    var result = List[String]()
    for (arg <- albums.elements) arg :: result
    result
  }
}
//
case class ArtistAlbums(artist: String, albumNames: List[String])