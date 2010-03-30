package com.rt.indexing

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import com.rt.util.MapUtils

/**
 *
 */
class MapUtilsTest extends JUnitSuite  {

  //  def addEntry(target: Map[String, List[List[Int]]], strings: List[String], i:Int):Map[String, List[List[Int]]] ={
  @Test def testAddEntry(){
    val strings = List("one", "three")
    val target: Map[String, List[List[Int]]] = Map( "one" -> List(List(5, 5)), "two" -> List(List(3, 3)))

    val merged = MapUtils.addEntry(target, strings, 100)
    assert(merged.size == 3)
    assert(merged("one").size == 2)
    assert(merged("one").contains(List(100)))
    assert(merged("one").contains(List(5, 5)))
    println("merged is "+merged)
  }

  @Test def testMergeListMaps(){
    val target: Map[String, List[List[Int]]] = Map( "one" -> List(List(1, 2)))
    val toMerge: Map[String, List[List[Int]]] = Map( "one" -> List(List(5, 5)), "two" -> List(List(3, 3)))

    val merged = MapUtils.mergeListMaps(target, toMerge)
    assert(merged.size == 2)
    assert(merged("one").size == 2)
    assert(merged("two").size == 1)
  }

  @Test def testJoinMaps(){
    val one: Map[String, List[List[Int]]] = Map( "one" -> List(List(1, 2)))
    val two: Map[String, List[List[Int]]] = Map( "one" -> List(List(5, 5)), "two" -> List(List(3, 3)))
    val three: Map[String, List[List[Int]]] = Map( "one" -> List(List(2, 5)), "three" -> List(List(3, 3)))
    val merged = MapUtils.joinMaps(one, two, three)
    assert(merged.size == 3)
    assert(merged("one").size == 3)
    assert(merged("two").size == 1)
    assert(merged("three").size == 1)
  }
}