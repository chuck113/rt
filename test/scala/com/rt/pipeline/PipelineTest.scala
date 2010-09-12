package com.rt.pipeline

import java.lang.String
import org.junit.{Assert, Test}
import com.rt.indexing.{RhymeLeaf, SongNode, ArtistNode, PipeLine}

class PipelineTest{
  
  //implicit?def?int2MyInt(?i:Int?)?=?new?MyInteger(i)
  implicit def artistHelper (h:ArtistNode) = new ArtistHelper(h)

  @Test def twoLineRhyme() {
    val artists: Map[String, ArtistNode] = new PipeLine().process("""C:\data\projects\rapAttack\rapAttack\olhha-testdata\atcq-test""")

    Assert.assertFalse(artists.values.head.allRhymes.exists(_.parts.length < 2))
  }

}

class ArtistHelper(artistNode:ArtistNode){
  def allSongs():List[SongNode]= artistNode.children.head.children
  def allRhymes():List[RhymeLeaf]= artistNode.children.head.children.head.rhymes
}
