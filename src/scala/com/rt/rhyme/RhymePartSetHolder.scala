package com.rt.rhyme

/**
 * Map-like Data structure for stroring groups of parts based on weather they rhyme.
 * When a string is inserted it is inserted into an entry with all other words it
 * rhymes with
 */

class RhymePartSetHolder(val rhymeMap: RhymeMap){
  private var entries: List[Entry] = List[Entry]()

  case class Entry(var parts: List[String]) {
    def addPart(part: String): Unit = {
      if (!parts.contains(part)) {
        parts ::= part
      }
    }
  }

  def addParts(parts: List[String]): Unit = {
    parts.foreach(addPart)
  }

  def addPart(part: String): Unit = {
    getEntryForPart(part) match {
      case Some(e) => {e.addPart(part)}
      case None => {entries = Entry(List(part)) :: entries}
    }
  }

  /**
   * removes the entry that contains the given part
   */
  def removeEntry(part: String): Option[List[String]] = {
    val removed: List[Entry] = entries.remove(e => e.parts.contains(part))
    removed.size match {
      case (0) => None
      case (1) => Some(removed(0).parts)
      case _ => {
        println("WARN removed " + removed.size + " entries for " + part + ", combining lists")
        Some(removed.foldLeft(List[String]()) {
          (res, entry) => {
            entry.parts ::: res
          }
        })
      }
    }
  }

  def allRhymePartSets(): List[List[String]] = {
    entries.map(_.parts)
  }

  def allRhymeParts(): List[String] = {
    entries.foldLeft(List[String]()) {
      (list, entry) => {
        list ::: entry.parts
      }
    }
  }

  private def getEntryForPart(part: String): Option[Entry] = {
    entries.foreach(e => {
      e.parts.foreach(p => {
        if (rhymeMap.doWordsRhyme(part, p)) {
          return Some(e)
        }
      })
    })
    None
  }

  private def contains(entry: Entry, part: String): Boolean = {
    entry.parts.exists(p => {rhymeMap.doWordsRhyme(part, p)})
  }

  private def containsForAny(entry: Entry, parts: List[String]): Boolean = {
    parts.exists(part => {contains(entry, part)})
  }

  def containsEntryForEither(pair: (String, String)): Boolean = {
    containsEntryFor(pair._1) || containsEntryFor(pair._2)
  }

  def containsEntryFor(part: String): Boolean = {
    entries.exists(e => {contains(e, part)})
  }

  /**
   * Removes entries which don't contain any of the given parts,
   * and returns a list of the parts removed.
   */
  def removeEntriesWhichDontMatchAny(parts: List[String]): List[List[String]] = {
    // find entries which don't mach any given parts
    def toRemoveFctn = {(e: Entry) => !containsForAny(e, parts)}
    val toBeRemoved: List[List[String]] = entries.filter(toRemoveFctn).map(e => e.parts)
    entries = entries.remove(toRemoveFctn)
    toBeRemoved
  }
}