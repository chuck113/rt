package com.rt.indexing.persistence

import xml.{Elem, PrettyPrinter}
import java.io.{FileWriter, Writer, FileOutputStream}


class XmlPersister{

  def writeXmlToFile(file:String, xml:Elem):Unit ={
    writeXml(new FileWriter(file), xml)
  }

  def writeXml(out:Writer, xml:Elem):Unit ={
    val st = new PrettyPrinter(120, 2).format(xml);
    out.write(st)
    out.flush()
    out.close()
  }
}