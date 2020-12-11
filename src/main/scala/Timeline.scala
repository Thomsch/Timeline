import timeline._

object Timeline extends App {
  import timeline.Artifact._

  val where = Repository("https://github.com/thomsch/timeline-example")
  val when = Version("v1").to("v4")
  val what = AnyFolder(Folder("B", AnyFile()))

  val results = for {
    _ <- repositoryExists(where) // Checks repository exists online
    versions <- retrieveVersions(when) // Retrieve individual versions
    changes <- retrieveChanges(versions) // Retrieve changes for each version
    resolvedChanges <- resolveArtifacts(what, changes) // Find and match artifacts in the filesystem
    tasks <- dispatcher(resolvedChanges) // Create individual analysis tasks
    cache <- executeTasks(tasks)
  } yield (versions, cache)

  results match {
    case Left(value) => println(s"Error: $value")
    case Right((versions, cache)) => printResults(versions, cache)
  }
}

