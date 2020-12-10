import timeline._

object Timeline extends App {
  import timeline.Artifact._

  val where = Repository("https://github.com/thomsch/timeline-example")
  val when = Version("aef023").to("4bbb34")
  val what = AnyFolder(Folder("B", AnyFile()))

  val results = for {
    _ <- repositoryExists(where) // Checks repository exists online
    versions <- retrieveVersions(when) // Retrieve individual versions
    changes <- retrieveChanges(versions) // Retrieve changes for each version
    resolvedChanges <- resolveArtifacts(what, changes) // Resolve for the artifacts of each version
    tasks <- dispatcher(resolvedChanges) // Create individual analysis tasks
    cache <- executeTasks(tasks)
  } yield cache

  results match {
    case Left(value) => println(s"Error: $value")
    case Right(cache) => {
      println(cache.get(Version("v1"), "/ROOT/A/B/foo2"))
      println(cache.get(Version("v2"), "/ROOT/A/B/foo2"))
    }
  }
}

