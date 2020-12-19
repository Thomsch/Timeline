import timeline._
import timeline.Artifact._

object Timeline extends App {

  val where = Repository("https://github.com/user/example") // Repository specification
  val when = Version("v1").to("v4") // Various ways to select versions or branches (e.g., Branch('main'))
  val what = AnyFolder(Folder("B", AnyFile())) // Match any file in folder 'B' anywhere in the repository

  val results = for {
    _ <- repositoryExists(where) // Checks repository exists online
    versions <- retrieveVersions(when) // Retrieve a sequential list of versions from v1 to v4 -> v1, v2, v3, v4
    changes <- retrieveChanges(versions) // Retrieve source changes for each version
    resolvedChanges <- resolveArtifacts(what, changes) // Find and match artifacts in the changes
    tasks <- dispatcher(resolvedChanges) // Create analysis tasks that can be executed independently
    cache <- executeTasks(tasks) // Run the tasks and accumulate results in a cache
  } yield (versions, cache)

  results match {
    // If there is an error, print it.
    case Left(value) => println(s"Error: $value")

    // Print the analysis result for each file matching the artifact specification (what) for selected versions.
    case Right((versions, cache)) => postProcess(versions, cache)
    /* Will output:
    Version v1:
      No files matching the query
    Version v2:
      /SRC/B/fooB     -> 3
    Version v3:
      /SRC/B/barB     -> 5
      /SRC/B/fooB     -> 3
    Version v4:
      /SRC/A/B/barAB  -> 2
      /SRC/A/B/fooAB  -> 1
      /SRC/B/barB     -> 5
      /SRC/B/fooB     -> 7
    */
  }
}
