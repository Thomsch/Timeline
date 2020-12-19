package timeline

package object mock {

  // Mocks the changes in the version control system. The mocked information can be retrieved from Git commands in a
  // real implementation.
  object vcs {

    import timeline.IO._

    // Changes for each version of our mock git repo
    val version1: Folder = // Initial commit with a file
      Folder("SRC",
        Set.empty,
        Set(File("app"))
      )

    val version2: Folder = // Adding fooA and fooB
      Folder("SRC",
        Set(
          Folder("A", Set.empty, Set(File("fooA"))),
          Folder("B", Set.empty, Set(File("fooB")))
        )
      )

    val version3: Folder =
      Folder("SRC", // file 'bar' is now 'foo2'. New file in root.
        Set(
          Folder("B", Set.empty, Set(File("barB")))
        )
      )

    val version4: Folder =
      Folder("SRC", // Add subfolder folder 'B' into folder 'A'
        Set(
          Folder("A",
            Set(Folder("B", Set.empty, Set(File("fooAB"), File("barAB"))))
          ),
          Folder("B", Set.empty, Set(File("fooB")))
        )
      )

    val versionRetrieval: Map[String, FileSystem] = Map(
      "v1" -> version1, "v2" -> version2, "v3" -> version3, "v4" -> version4)

    def getLatestVersion: String = "v4"

    def versionRange(from: String, to: String): List[String] = {
      if (from != "v1" || to != "v4") List.empty else {
        List("v1", "v2", "v3", "v4")
      }
    }

    def getDiffs(version: Version): Option[FileSystem] = versionRetrieval.get(version.id)
  }

  // Mocks an analysis on a file, returning an integer as a result (e.g., number of methods for the file).
  object analyzer {
    def run(task: Task): Map[IO.Path, Int] = { // Parametrize
      task.artifacts.map(path => (path, staticAnalyzer(task.version.id, path))).toMap
    }

    val staticAnalyzer: Map[(String, String), Int] = Map(
      "v2" -> "/SRC/B/fooB" -> 3,
      "v3" -> "/SRC/B/barB" -> 5,
      "v4" -> "/SRC/A/B/fooAB" -> 1,
      "v4" -> "/SRC/A/B/barAB" -> 2,
      "v4" -> "/SRC/B/fooB" -> 7
    )
  }

}
