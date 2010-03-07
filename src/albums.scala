import rt.dsl.AlbumSpec._

buildIndexFrom("""C:\data\projects\rhyme-0.9\olhha""",
  artist("Beastie Boys",
    albums("Time to Get Ill", "Ill Communication","Paul's Botique", "Hello Nasty")
  )
)