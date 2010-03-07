package indexing


//TODO Genericise these methods
object MapUtils {
  def joinMaps(maps: Map[String, List[List[Int]]]*): Map[String, List[List[Int]]] = {
    maps.foldLeft(Map[String, List[List[Int]]]()) {
      (res, current) => {
        mergeListMaps(res, current)
      }
    }
  }

  def mergeListMaps(target: Map[String, List[List[Int]]], mapTwo: Map[String, List[List[Int]]]): Map[String, List[List[Int]]] = {
    var res = collection.mutable.Map[String, List[List[Int]]]() ++ target;
    for (el <- mapTwo.elements) {
      res(el._1) = res.getOrElse(el._1, List()) ++ el._2
    }
    Map.empty ++ res
  }


  def addEntry(target: Map[String, List[List[Int]]], strings: List[String], i: Int): Map[String, List[List[Int]]] = {
    val tempMap = collection.mutable.Map() ++ target

    for (st <- strings) {
      tempMap(st) = tempMap.getOrElse(st, List()) ::: List(List(i))
    }
    Map(tempMap.toSeq: _*)
  }
}