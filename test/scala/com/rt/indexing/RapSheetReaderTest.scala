package com.rt.indexing

import java.lang.String
import collection.immutable.Map
import org.junit.{Test}
import org.junit.Assert._
import com.rt.rhyme.{Rhyme, RapSheetReader}

class RapSheetReaderTest {
  @Test def failedRead() {
    val trackFile = """C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\PUBLIC_ENEMY\MUSE_SICK_N_HOUR_MESS_AGE\7.txt"""
    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(trackFile, null);
    println("found " + rhymes.size + " rhymes")
    rhymes.foreach(println)

    //val rhymeMap: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(trackFile, null)
    val list: List[Rhyme] = RapSheetReader.findRhymes(trackFile, null)
    println("found " + list.size + " rhymes in second")
    //list.foreach(println)
  }

  @Test def rakim() {
    val lines = List[String]("But don't be afraid in the dark, in a park",
      "Not a scream or a cry, or a bark, more like a spark")

    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
//    assertRhymeListContnainsRhymeSet(rhymes, "PARK", "DARK", "SPARK", "BARK")
    rhymes.foreach(println)
  }

  private def assertRhymeListContnainsRhymeSet(rhymes:List[Rhyme], parts:String*)={
    assertTrue(rhymes.find(r =>{r.parts.sameElements(parts.toList)}).isDefined)
  }

  @Test def empd4() {
    val lines = List[String]("No hopes folks, I quote note for note ",
      " You mind float on the rhyme on I wrote (what?)")

    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
    assertRhymeListContnainsRhymeSet(rhymes, "NOTE", "QUOTE", "WROTE", "FLOAT")
    rhymes.foreach(println)

  }

  @Test def publicEnemy() {
    val lines = List[String]("I got mine, for I'm using my rhyme")

    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
    //assertTrue(rhymes.isEmpty)
    rhymes.foreach(println)
    assertRhymeListContnainsRhymeSet(rhymes, "RHYME", "I'M")
  }


  @Test def nas2() {
    val lines = List[String]("Street's disciple, I rock beats that's mega trifle")

    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
    rhymes.foreach(println)
    assertRhymeListContnainsRhymeSet(rhymes, "BEATS", "STREET'S")
  }

  @Test def epmd3() {
    val lines = List[String]("Can't understand, why your body's gettin weaker",
      "Then you realize, it's the voice from the speaker")
    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
    rhymes.foreach(println)
    assertRhymeListContnainsRhymeSet(rhymes, "WEAKER", "SPEAKER")

  }

  @Test def epmd2() {
    val lines = List[String]("They drove off quickly in the black Hummer",
      "Never trust no matter what the dance or song")
    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)
    rhymes.foreach(println)
    assertTrue(rhymes.isEmpty)

  }

  @Test def correctRhymes() {
    val lines = List("No respect in eighty-seven, eighty-eight you kneel",
      "Cause I produce and get loose, when it's time to perform",
      "Wax a sucker like Mop & Glow (that's word born)")
    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lines, null)

    rhymes.foreach(println)
    assertRhymeListContnainsRhymeSet(rhymes, "LOOSE", "PRODUCE")

  }

  @Test def failingOne() {
    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(lyrics(), null)
    //val rhymes: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(lyrics(), null)
    rhymes.foreach(rhyme => {
      println(rhyme)
    })

    //    val lines1: List[String] = rhymes("frozen")(0).lines
    //    Assert.assertEquals("This rhythmatic explosion, is what your frame of mind has chosen", lines1(0))
    //    Assert.assertEquals("I'll leave your brain stimulated, niggaz is frozen", lines1(1))
    //    println()
    //
    //    val trackFile = """C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\Nas\Illmatic\10.txt"""
    //    val rhymes2 = RapSheetReader.findRhymesOld(trackFile, null);
    //    rhymes2.foreach(rhyme =>{
    //      println(rhyme._1+" = "+rhyme._2)
    //    })
    //
    //    println("frozen lines are: "+rhymes2("frozen"))
    //    val list: List[String] = rhymes2("frozen")(0).lines
    //    Assert.assertEquals("This rhythmatic explosion, is what your frame of mind has chosen", list(0))
    //    Assert.assertEquals("I'll leave your brain stimulated, niggaz is frozen", list(1))
  }



  def lyrics(): List[String] = {
    List[String]("It ain't hard to tell, I excel, then prevail",
      "The mic is contacted, I attract clientele",
      "My mic check is life or death, breathin a sniper's breath",
      "I exhale the yellow smoke of buddha through righteous steps",
      "Deep like The Shinin', sparkle like a diamond",
      "Sneak a uzi on the island in my army jacket linin",
      "Hit the Earth like a comet, invasion",
      "Nas is like the Afrocentric Asian, half-man, half-amazin",
      "Cause in my physical, I can express through song",
      "Delete stress like Motrin, then extend strong",
      "I drank Moet with Medusa, give her shotguns in hell",
      "From the spliff that I lift and inhale, it ain't hard to tell",
      "",
      "The buddha monk's in your trunk, turn the bass up",
      "Not stories by Aesop, place your loot up, parties I shoot up",
      "Nas, I analyze, drop a jew-el, inhale from the L",
      "School a fool well, you feel it like braille",
      "It ain't hard to tell, I kick a skill like Shaquille holds a pill",
      "Vocabulary spills I'm +Ill+",
      "plus +Matic+, I freak beats slam it like Iron Shiek",
      "Jam like a tech with correct techniques",
      "So analyze me, surprise me, but can't magmatize me",
      "Scannin while you're plannin ways to sabotage me",
      "I leave em froze like her-on in your nose",
      "Nas'll rock well, it ain't hard to tell",
      "",
      "This rhythmatic explosion, is what your frame of mind has chosen",
      "I'll leave your brain stimulated, niggaz is frozen",
      "Speak with criminal slang, begin like a violin",
      "End like Leviathan, it's deep well let me try again",
      "Wisdom be leakin out my grapefruit troop",
      "I dominate break loops, givin mics men-e-straul cycles",
      "Street's disciple, I rock beats that's mega trifle",
      "And groovy but smoother than moves by Villanova",
      "You're still a soldier, I'm like Sly Stone in Cobra",
      "Packin like a rasta in the weed spot",
      "Vocals'll squeeze glocks, MC's eavesdrop",
      "Though they need not to sneak",
      "My poetry's deep, I never fell",
      "Nas's raps should be locked in a cell",
      "It ain't hard to tell")
  }
}