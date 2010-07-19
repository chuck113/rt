package com.rt.rhyme

import java.lang.String


object RhymeZoneMapCache{
  val rhymeMap = new RhymeZoneRhymeMap()

  def getRhymeMap():RhymeMap={
    return rhymeMap
  }
}