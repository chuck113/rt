package com.rt.util


object MapUtils {

  def addKeys[K,V](map: Map[K, V], keys:List[K], newValue:V): Map[K, V]={
    var res = collection.mutable.Map[K, V]() ++ map;
    for(key <- keys){
      res(key) = newValue
    }
    Map.empty ++ res
  }

  /** builds a map up from a list, applying a function to obtain keys for each value in the list */
  def toMap[K,V](list:List[V], mapFunction:(V => K)):Map[K,V]={
    list.foldLeft(Map[K, V]()) {(map,value)  => {
      map(mapFunction(value)) = value
      }
    }
  }

  def toMapReversed[K,V](list:List[K], mapFunction:(K => V)):Map[K,V]={
    list.foldLeft(Map[K, V]()) {(map,value)  => {
      map(value) = mapFunction(value)
      }
    }
  }

  def join[T](list:List[List[T]]):List[T]={
    list.foldLeft(List[T]()){(result, list)=>{
      result ::: list
    }}
  }

  def joinMaps[K,V](maps: Map[K, List[V]]*): Map[K, List[V]] = {
    maps.foldLeft(Map[K, List[V]]()) {
      (res, current) => {
        mergeListMaps(res, current)
      }
    }
  }

  def mergeListMaps[K,V](target: Map[K, List[V]], mapTwo: Map[K, List[V]]): Map[K, List[V]] = {
    var res = collection.mutable.Map[K, List[V]]() ++ target;
    for (el <- mapTwo.elements) {
      res(el._1) = res.getOrElse(el._1, List[V]()) ++ el._2
    }
    Map.empty ++ res
  }

  def addEntry[K,V](target: Map[K, List[List[V]]], strings: List[K], i:V): Map[K, List[List[V]]] = {
    val tempMap = collection.mutable.Map() ++ target

    for (st <- strings) {
      tempMap(st) = tempMap.getOrElse(st, List()) ::: List(List(i))
    }
    Map(tempMap.toSeq: _*)
  }

  def toJavaMap[K,V](scalaMap: Map[K,V]):java.util.Map[K,V] ={
    val jMap = new java.util.HashMap[K,V]()
    for (el <- scalaMap.elements) {
      jMap.put(el._1, el._2)
    }
    jMap
  }

  def toJavaMapOfLists[K,V](scalaMap: Map[K,List[V]]):java.util.Map[K,java.util.List[V]] ={
    val jMap = new java.util.HashMap[K,java.util.List[V]]()
    for (el <- scalaMap.elements) {
      jMap.put(el._1, toJavaList(el._2))
    }
    jMap
  }

  def toJavaList[T](scalaList: List[T]):java.util.List[T]={
    java.util.Arrays.asList(scalaList.toArray: _*)
  }

  def toJavaSet[T](scalaSet: Set[T]):java.util.Set[T]={
    //val list: java.util.List[T] = java.util.Arrays.asList(scalaSet.toArray: _*)
    new java.util.HashSet[T](toJavaList(scalaSet.toList))
  }

  def toScalaList[T](javaList: java.util.List[T])={
    List(javaList.toArray: _*)
  }

  def toScalaList[T](javaList: java.util.List[T], clazz:Class[T])={
    List(javaList.toArray: _*)
  }


  def toScalaMap[K,V](jMap:java.util.Map[K,V]):Map[K,V]={
    val res = collection.mutable.Map[K, V]()
    val entryIter = jMap.entrySet.iterator
    while(entryIter.hasNext) {
      val next = entryIter.next
      res(next.getKey) = next.getValue
    }

    Map(res.toSeq: _*)
  }
}