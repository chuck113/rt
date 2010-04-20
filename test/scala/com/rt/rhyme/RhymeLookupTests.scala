package com.rt.rhyme

import org.junit.Test
import junit.framework.Assert._

class RhymeLookupTests {
  val rhymeMap = new CmuDictRhymeMap()
  val reader = new RhymeFinder(rhymeMap)


  @Test def twoLineRhyme() {
    val lines = List("So analyze me, surprise me, but can't magmatize me")
    val res = reader.findMultiPartRhymes(lines)
    println(res)
    assertTrue(res(0).parts.size == 3)
  }

}