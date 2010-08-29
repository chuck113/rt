package com.rt.tools

import com.rt.ohhla.OhhlaConfig


object PrintEveryArtistObj{
   def main(args: Array[String]):Unit={
    PrintEveryArtistObj(OhhlaConfig.rawTargetLocation)
  }

  private def PrintEveryArtistObj(rootFolder:String)={
    OhhlaConfig.allArtistFolderNames.foreach(a => {println ("1:"+a)})
  }
}