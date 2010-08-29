package com.rt.rhyme

import java.lang.String
import collection.immutable.Map
import org.junit.{Test}
import org.junit.Assert._

class RhymeFinderExampleTests {
  val rhymeMap = RhymeZoneMapCache.getRhymeMap() //new RhymeZoneRhymeMap()
  val reader = new RhymeFinder(rhymeMap)

  //  @Test def failedRead() {
  //    val trackFile = """C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\PUBLIC_ENEMY\MUSE_SICK_N_HOUR_MESS_AGE\7.txt"""
  //    val rhymes: List[Rhyme] = RapSheetReader.findRhymes(trackFile, null);
  //    println("found " + rhymes.size + " rhymes")
  //    rhymes.foreach(println)
  //
  //    //val rhymeMap: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(trackFile, null)
  //    val list: List[Rhyme] = RapSheetReader.findRhymes(trackFile, null)
  //    println("found " + list.size + " rhymes in second")
  //    list.foreach(println)
  //  }

  //Jackin creeps, packin heat, these Harlem streets is for keeps / Much love to all my peeps who got covered in sheets



  //  @Test def findRhymesInLines2Epmd() {
  //    val lines = List[String]("No respect in eighty-seven, eighty-eight you kneel",
  //      "Cause I produce and get loose, when it's time to perform",
  //      "Wax a sucker like Mop & Glow (that's word born)")
  //
  //    reader.findRhymesInLines2(lines)
  //  }

  private def lines(lines: String*) = lines.toList

  private def parts(parts: String*) = parts.toList

  @Test def rakim() {
    testRhyme(
      lines("But don't be afraid in the dark, in a park",
        "Not a scream or a cry, or a bark, more like a spark"),
      parts("PARK", "DARK", "SPARK", "BARK"),
      parts("OR", "MORE")
      )

    //    val lines = List[String]("But don't be afraid in the dark, in a park",
    //      "Not a scream or a cry, or a bark, more like a spark")
    //
    //    val rhymes: List[Rhyme] = reader.findRhymesInLines(lines)
    //    assertRhymeListContnainsRhymeSetOld(rhymes, "PARK", "DARK", "SPARK", "BARK")
    //    rhymes.foreach(println)
  }

  private def assertRhymeListContnainsRhymeSetOld(rhymes: List[Rhyme], parts: String*) = {
    assertRhymeListContnainsRhymeSet(rhymes, parts.toList)
  }

  private def assertRhymeListContnainsRhymeSet(rhymes: List[Rhyme], expectedParts: List[String]) = {
    if (expectedParts.isEmpty){ assertTrue(rhymes.isEmpty)
    }else {
      null
    }
      // each rhyme in the list must have an equlivalent
      //assertTrue(rhymes.find(r => {r.parts.forall(p => parts.contains(p))}).isDefined)

    // foreach
  }

  private def printRhymes(found: List[Rhyme], required: List[List[String]]) = {
    println("FOUND")
    found.sort((a, b) => a.parts(0) < b.parts(0)).foreach(r => println(r.parts))
    println("REQUIRED")
    required.sort((a, b) => a(0)(0) < b(0)(0)).foreach(r => println(r))
  }

  private def testRhyme(lines: List[String], rhymesSetsToFind: List[String]*) = {
    val rhymes: List[Rhyme] = reader.findRhymesInLines(lines)

    // for each rhyme, it's parts must appear in one of the lists entries
    //val first: List[String] = rhymesSetsToFind.toList.first

    rhymesSetsToFind.foreach(rhymeSet => {
      val exists = rhymes.exists(rhyme => {
        rhymeSet.forall(part => rhyme.parts.contains(part))
      })
      assertTrue("Did not find expected rhyme "+rhymeSet+" in rhyme results "+rhymes.map(_.parts), exists)
    })

//    rhymes.foreach(println)
//    if (rhymes.size != rhymesSetsToFind.toList.size) {
//      printRhymes(rhymes, rhymesSetsToFind.toList)
//    }
//
    //assertEquals("rhymes were not of equal size", rhymes.size, rhymesSetsToFind.toList.size)
//    rhymesSetsToFind.foreach(setToFind => assertRhymeListContnainsRhymeSet(rhymes, setToFind))
  }

  @Test def empd4() {
    testRhyme(
      lines("No hopes folks, I quote note for note ",
        " You mind float on the rhyme on I wrote (what?)"),
      parts("NOTE", "QUOTE", "WROTE", "FLOAT")
      )
  }

  @Test def publicEnemy() {
    testRhyme(
      lines("I got mine, for I'm using my rhyme"),
      parts("RHYME", "I'M")
      )
  }

  @Test def big2() {
    testRhyme(
      lines("There's gonna be a lot of slow singin, and flower bringin",
            "if my burgular alarm starts ringin"),
      parts("SINGIN", "BRINGIN", "RINGIN")
      )
  }

  @Test def nas2() {
    testRhyme(
      lines("Street's disciple, I rock beats that's mega trifle"),
      parts("BEATS", "STREET'S")
      )
  }

  @Test def epmd3() {
    testRhyme(
      lines("Can't understand, why your body's gettin weaker",
        "Then you realize, it's the voice from the speaker"),
      parts("WEAKER", "SPEAKER")
      )
  }

  @Test def epmd2() {
    testRhyme(
      lines("They drove off quickly in the black Hummer",
        "Never trust no matter what the dance or song")
      )
  }

  @Test def empd() {
    testRhyme(
      lines("No respect in eighty-seven, eighty-eight you kneel",
        "Cause I produce and get loose, when it's time to perform",
        "Wax a sucker like Mop & Glow (that's word born)"),
      parts("LOOSE", "PRODUCE")
      )
  }

  @Test def big1() {
    testRhyme(lines(
      "You see its kinda like the crack did to Pookie, in New Jack",
      "Except when I cross over, there ain't no comin' back",
      "Should I die on the train track, like Remo in Beatstreet"),
      parts("NEW", "YOU"),
      parts("CRACK", "JACK", "BACK", "TRACK"))
  }

  @Test def big() {
    testRhyme(lines(
      "You see its kinda like the crack did to Pookie, in New Jack",
      "Except when I cross over, there ain't no comin' back",
      "Should I die on the train track, like Remo in Beatstreet",
      "People at the funeral frontin' like they miss me",
      "My baby momma kissed me but she glad I'm gone",
      "She knew me and her sista had somethin' goin' on"),
      parts("NEW", "YOU"),
      parts("SHE", "ME"),
      parts("GLAD", "HAD"),
      parts("ON", "GONE"),
      parts("TRACK", "CRACK", "JACK", "BACK"))
  }

  // "Not stories by Aesop, place your loot up, parties I shoot up",

  @Test def nas4() {
    testRhyme(
      lines("The buddha monk's in your trunk, turn the bass up",
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
        "Nas'll rock well, it ain't hard to tell"
        )
      , parts("BASS", "PLACE")
      , parts("SHOOT", "LOOT")
      , parts("FOOL", "SCHOOL")
      , parts("INHALE", "BRAILLE")
      , parts("FEEL", "SHAQUILLE")
      , parts("TELL", "L", "WELL")
      , parts("PLANNIN", "SCANNIN")
      , parts("ILL", "PILL", "SKILL")
      , parts("SLAM", "JAM")
      , parts("YOU", "JEW")
      , parts("FROZE", "NOSE")
      , parts("YOU'RE", "YOUR")
      , parts("TELL", "WELL")
      )
  }


  @Test def nas3() {
    testRhyme(
      lines("It ain't hard to tell, I excel, then prevail",
        "The mic is contacted, I attract clientele",
        "My mic check is life or death, breathin a sniper's breath"
        ), parts("TELL", "EXCEL", "CLIENTELE")
      , parts("DEATH", "BREATH")
      )
  }

  @Test def nas() {
    val rhymes: List[Rhyme] = reader.findRhymesInLines(lyrics)
    rhymes.foreach(println)

    //    val lines = List("No respect in eighty-seven, eighty-eight you kneel",
    //      "Cause I produce and get loose, when it's time to perform",
    //      "Wax a sucker like Mop & Glow (that's word born)")
    //    val rhymes: List[Rhyme] = reader.findRhymesInLines(lines)
    //
    //    rhymes.foreach(println)
    //    assertRhymeListContnainsRhymeSetOld(rhymes, "LOOSE", "PRODUCE")

  }

  //  @Test def failingOne() {
  //    val rhymes: List[Rhyme] = reader.findRhymesInLines(lines)
  //    //val rhymes: Map[String, List[RhymeLines]] = RapSheetReader.findRhymesOld(lyrics(), null)
  //    rhymes.foreach(rhyme => {
  //      println(rhyme)
  //    })
  //
  //    //    val lines1: List[String] = rhymes("frozen")(0).lines
  //    //    Assert.assertEquals("This rhythmatic explosion, is what your frame of mind has chosen", lines1(0))
  //    //    Assert.assertEquals("I'll leave your brain stimulated, niggaz is frozen", lines1(1))
  //    //    println()
  //    //
  //    //    val trackFile = """C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\Nas\Illmatic\10.txt"""
  //    //    val rhymes2 = RapSheetReader.findRhymesOld(trackFile, null);
  //    //    rhymes2.foreach(rhyme =>{
  //    //      println(rhyme._1+" = "+rhyme._2)
  //    //    })
  //    //
  //    //    println("frozen lines are: "+rhymes2("frozen"))
  //    //    val list: List[String] = rhymes2("frozen")(0).lines
  //    //    Assert.assertEquals("This rhythmatic explosion, is what your frame of mind has chosen", list(0))
  //    //    Assert.assertEquals("I'll leave your brain stimulated, niggaz is frozen", list(1))
  //  }



  def lyrics(): List[String] = {
    List[String](
      "It ain't hard to tell, I excel, then prevail",
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