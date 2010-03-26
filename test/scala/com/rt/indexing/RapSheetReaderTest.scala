package com.rt.indexing

import org.junit.Test
import java.lang.String
import collection.immutable.Map


class RapSheetReaderTest   {

  @Test def failingOne(){
    val reader:RapSheetReader = new RapSheetReader(lyrics(), null)
    val rhymes: Map[String, List[RhymeLines]] = reader.findRhymes()
    rhymes.foreach(rhyme =>{
      println(rhyme._1+" = "+rhyme._2)
    })

    println()

    val trackFile = """C:\data\projects\rapAttack\rapAttack\rhyme-0.9\olhha\Nas\Illmatic\10.txt"""
    val rhymes2 = RapSheetReader.findRhymes(trackFile, null);
    rhymes2.foreach(rhyme =>{
      println(rhyme._1+" = "+rhyme._2)
    })
  }

  def lyrics():List[String]={
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