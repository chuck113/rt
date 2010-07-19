package com.rt.rhyme

import org.junit.Test
import junit.framework.Assert._

class RhymeLookupTests {
  val rhymeMap = RhymeZoneMapCache.getRhymeMap() 
  val reader = new RhymeFinder(rhymeMap)

  //FIXME
  /*@Test()*/ def twoLineRhyme() {
    val lines = List("So analyze me, surprise me, but can't magmatize me")
    val res = reader.findRhymesInLines(lines)
    println(res)
    assertTrue(res(0).parts.size == 3)
  }
}