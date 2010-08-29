//package com.rt.dsl

//import _root_.rt.dsl.AlbumSpec._
//import _root_.rt.dsl.{Album, ArtistAlbums}


//object SourceAlbumsDsl extends AbstractDslFile{
//   override def load():List[ArtistAlbums]={
//    artists(
//      artist("A Tribe Called Quest", "ATCQ", //TODO need alias here
//        Album("Peoples Instinctive Travels and", 5),
//        Album("Midnight Marauders", 5)),
//      artist("De La Soul",
//        Album("3 Feet High and Rising", 5)
//      ),
//      artistAllAlbums("Eric B. and Rakim", 5)
//    )
//  }
//}