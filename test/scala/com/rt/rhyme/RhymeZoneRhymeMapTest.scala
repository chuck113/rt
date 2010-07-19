package com.rt.rhyme

import org.junit.Test
import junit.framework.Assert._
import java.lang.String


class RhymeZoneRhymeMapTest {
  val rhymeMap = RhymeZoneMapCache.getRhymeMap()

  @Test def testAliases() {
    assertTrue(rhymeMap.doWordsRhyme("FUCKIN'", "MUCKING"))
    assertTrue(rhymeMap.doWordsRhyme("FUCK", "MUCK"))
    assertTrue(rhymeMap.doWordsRhyme("FLYIN'", "CRYIN'"))
    assertTrue(rhymeMap.doWordsRhyme("MISSIN", "HISSING"))
  }

  @Test def testRemovesDash() {
    val lines: List[String] = List("We got determination - bass and highs", "White Castle fries only come in one size")
    val rhymeFinder: RhymeFinder = new RhymeFinder(rhymeMap)
    val list: List[Rhyme] = rhymeFinder.findRhymesInLines(lines)
    null
  }
}