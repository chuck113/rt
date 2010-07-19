package com.rt.rhyme

import collection.mutable.ListBuffer
import org.junit.Test
import junit.framework.Assert
import java.lang.String

class RhymeFinderTest {

  val rhymeMap = RhymeZoneMapCache.getRhymeMap() //new RhymeZoneRhymeMap()
  val reader = new RhymeFinder(rhymeMap)

  @Test def twoLineRhyme() {
    val lines = List("Foot on the pedal - never ever false metal", "Engine running hotter than a boiling kettle")
    val res = reader.findRhymesInLines(lines)
    println("res is " + res)
    Assert.assertTrue(res.size == 2)
  }

  @Test def oneLineRhyme() {
    val line = "My mic check is life or death, breathin a sniper's breath"
    val res = reader.findRhymesInLines(List(line))
    println("res is " + res)
    Assert.assertTrue(res.size > 0)
  }


  @Test def noRhymes(){
    val lines = List("They drove off quickly in the black Hummer",
      "Never trust no matter what the dance or song")

    Assert.assertTrue(reader.findRhymesInLines(lines).size == 0)
  }

//  @Test def testIsRhymeContainedIn() {
//    val r1 = Rhyme(List("a", "b", "cc"), List("line1", "line2"))
//    val r2 = Rhyme(List("a", "b", "bb", "cc"), List("line1", "line2"))
//    val r3 = Rhyme(List("a", "b", "X"), List("line1", "line2"))
//    Assert.assertTrue(reader.isRhymeContainedIn(r2, r1))
//    Assert.assertFalse(reader.isRhymeContainedIn(r3, r1))
//  }
//
//  @Test def testIsRhymeContainedInAnElement() {
//    val r1 = Rhyme(List("a", "b", "cc"), List("line1", "line2"))
//    val r2 = Rhyme(List("a", "b", "bb", "cc"), List("line1", "line2"))
//    val r3 = Rhyme(List("a", "b", "X"), List("line1", "line2"))
//    val r4 = Rhyme(List("a", "b", "X", "y"), List("line1", "line2"))
//    Assert.assertTrue(reader.isRhymeContainedInAnElement(List(r2, r3), r1))
//    //Assert.assertFalse(reader.isRhymeContainedInAnElement(List(r3, r4), r2))
//  }
//
//  //replaceIntersectingRhyme
//
//  @Test def replaceIntersectingRhyme() {
//    val r1 = Rhyme(List("a", "b", "cc"), List("line1", "line2"))
//    val r2 = Rhyme(List("a", "b"), List("line1", "line2"))
//    val r3 = Rhyme(List("a", "b", "X"), List("line1", "line2"))
//    val r4 = Rhyme(List("a", "b", "X", "y"), List("line1", "line2"))
//    Assert.assertEquals(List(r1, r3), reader.replaceIntersectingRhyme(List(r2, r3), r1))
//  }


  @Test def multiPartRhyme() {
    // todo, switch in' for ing
    val lines = List(
      //"Cuz I came back to attack others in spite-",
      //"Strike like lightnin', It's quite frightenin'!",
      "But don't be afraid in the dark, in a park,",
      "Not a scream or a cry, or a bark, more like a spark;"
      //"Ya tremble like a  alcoholic, muscles tighten up,"
      )

    val res = reader.findRhymesInLines(lines)
    println("res is " + res)
    Assert.assertTrue(res.size == 2)
    Assert.assertTrue(res(1).parts.removeDuplicates.size == 4)
  }


  @Test def testPairs(){
    val res:Map[(String, String), List[String]] = reader.findRhymingWordsMap(List(
      "But don't be afraid in the dark, in a park,",
      "Not a scream or a cry, or a bark, morexx like a spark;",
      "Ya tremble like a  alcoholic, muscles tighten up,"
      ))

    println(res)
    res.foreach(println)
  }

  @Test def testRhymeSet(){
    val s = new RhymePartSetHolder(rhymeMap)
    s.addPart("back")
    s.addPart("attack")
    s.addPart("more")
    s.addPart("or")
    s.addPart("attack")
    val list: List[List[String]] = s.allRhymePartSets()
    Assert.assertEquals(2, list.size)
    Assert.assertEquals(2, list(0).size)
    Assert.assertEquals(2, list(1).size)

    Assert.assertTrue(s.containsEntryFor("back"))
    Assert.assertTrue(s.containsEntryFor("or"))
    Assert.assertFalse(s.containsEntryFor("xx"))

    println("list = "+list)
  }
  
  @Test def testRhymeList(){
    val r1 = Rhyme(List("a", "b", "cc"), List("line1", "line2"))
    val r2 = Rhyme(List("a", "b"), List("line1", "line2"))
    val r3 = Rhyme(List("a", "b", "cc", "X"), List("line1", "line2"))
    val r4 = Rhyme(List("a", "b", "X", "y"), List("line1", "line2"))
    val r5 = Rhyme(List("x", "x"), List("line1", "line2"))

    val s = new RhymeList();
    s.addRhyme(r1)
    //s.addInternal(r1)
    Assert.assertEquals(List(r1), s.subsetOf(r3)) // contains a subset, delete it and insert r3
    Assert.assertEquals(List(), s.subsetOf(r5)) // not a sub set, can ignore ignore

    Assert.assertEquals(List(r1), s.superSetOf(r2)) // contians a superset, don't add it
    Assert.assertEquals(List(), s.superSetOf(r3))  // not a super set, can ignore
  }

  @Test def multiPartRhymeWith2RhymeSets() {
    // todo, switch in' for ing
    val lines = List(
      "Cuz I came back to attack others in spite-",
      "Strike like lightnin', It's quite frightenin'!",
      "But don't be afraid in the dark, in a park,",
      "Not a scream or a cry, or a bark, more like a spark;",
      "Ya tremble like a  alcoholic, muscles tighten up,"
      )

    val res:List[Rhyme] = reader.findRhymesInLines(lines)
    println("res is " + res)
    //Assert.assertTrue(res.size == 5)
    Assert.assertTrue(res.exists(_.parts.size == 4))
    
   // Assert.assertTrue(res.exists(_ == Rhyme(List("MORE", "OR"),List("Not a scream or a cry, or a bark, more like a spark;"))))
    Assert.assertTrue(res.exists(r =>
      r.parts.contains("MORE") &&  r.parts.contains("OR") && r.lines.size == 1 && 
      r.lines(0) == "Not a scream or a cry, or a bark, more like a spark;"
    ));
    Assert.assertTrue(res.exists(r =>
      r.parts.size == 4 &&
      r.parts.contains("SPARK") &&
      r.parts.contains("BARK") &&
      r.parts.contains("PARK") &&
      r.parts.contains("DARK") &&
      r.lines.size == 2 &&
      r.lines.contains("Not a scream or a cry, or a bark, more like a spark;")&&
      r.lines.contains("But don't be afraid in the dark, in a park,")
    ));

    //Assert.assertTrue(res.exists(_ == Rhyme(List("SPARK", "BARK", "PARK", "DARK"),List("Not a scream or a cry, or a bark, more like a spark;", "But don't be afraid in the dark, in a park,"))))
  }

}