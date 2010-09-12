package com.rt.tools

import com.rt.ohhla.OhhlaFiles


object PrintEveryArtistObj{
   def main(args: Array[String]):Unit={
    PrintEveryArtistObj(OhhlaFiles.root)
  }

  private def PrintEveryArtistObj(rootFolder:String)={
    OhhlaFiles.allArtistFolderNames.foreach(a => {println ("1:"+a)})
  }
}