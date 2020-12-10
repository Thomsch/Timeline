package timeline

package object mock {

  object vcs {
    import timeline.IO._

    // Changes for each version of our mock git repo
    val version1 = // Initial commit with one subfolder and a file
      Folder("ROOT",
        Set(Folder("A")),
        Set(File("readme.md"))
      )

    val version2 =  // New root folder 'B' with files 'foo' and 'bar'
      Folder("ROOT",
        Set(
          Folder("A"),
          Folder("B", Set.empty,Set(File("foo"), File("bar"))
          )),
        Set(File("readme.md"))
      )

    val version3 = Folder("ROOT", // file 'bar' is now 'foo2'. New file in root.
      Set(
        Folder("A"),
        Folder("B", Set.empty, Set(File("foo"), File("foo2")))),
      Set(File("readme.md"), File(".gitignore")))

    val version4 = Folder("ROOT", // Move folder 'B' into folder 'A'
      Set(
        Folder("A",
          Set(Folder("B", Set.empty, Set(File("foo"), File("foo2")))))),
      Set(File("readme.md"), File(".gitignore")))
  }


  object analyzer {
    def run(task: Task): Map[Path, Int] = { // Parametrize
      task.artifacts.map(path => (path, staticAnalyzer(task.version.id, path))).toMap
    }

    val staticAnalyzer: Map[(String, String), Int] = Map(
      "1" -> "path" -> 3
    )
  }

}
