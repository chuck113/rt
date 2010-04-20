package com.rt.rhyme

import org.junit.Test
import junit.framework.Assert._
import java.lang.String


class RhymeZoneRhymeMapTest {
  val rhymeMap = new RhymeZoneRhymeMap()


  @Test def twoLineRhyme() {
    assertTrue(rhymeMap.doWordsRhyme("BUD", "FLOODS"))
    assertTrue(rhymeMap.doWordsRhyme("MISSIN'", "HISSING"))
    assertTrue(rhymeMap.doWordsRhyme("SLICK", "CHICKS"))
    assertTrue(rhymeMap.doWordsRhyme("BUD", "FLOODES"))
  }

  @Test def testRemovesDash() {
    val lines: List[String] = List("We got determination - bass and highs", "White Castle fries only come in one size")
    val rhymeFinder: RhymeFinder = new RhymeFinder(rhymeMap)
    val list: List[String] = rhymeFinder.findRhymesInLine(lines(0))
    null
  }

  @Test def testCorrectRhymes() {
    val lines: List[String] = List(
      "No respect in eighty-seven, eighty-eight you kneel",
      "Cause I produce and get loose, when it's time to perform",
      "Wax a sucker like Mop & Glow (that's word born)")
    val rhymeFinder: RhymeFinder = new RhymeFinder(rhymeMap)
    val pairsInLine: List[(String, String)] = rhymeFinder.findRhymePairsInLine(lines)
     assertTrue(pairsInLine.contains("PRODUCE" -> "LOOSE"))
  }
}