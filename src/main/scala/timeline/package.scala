import timeline.Artifact.AnyFile
import timeline.IO.{Folder, Path}

import scala.annotation.tailrec

package object timeline {

  /**
   * Surface language specification
   */

  object Artifact {

    sealed trait Artifact

    final case class Folder(name: String, artifact: Artifact) extends Artifact

    final case class File(name: String) extends Artifact

    final case class AnyFolder(artifact: Artifact) extends Artifact

    final case class AnyFile() extends Artifact

    // Two sugared artifact specification
    def allFiles(): Artifact = AnyFolder(AnyFile())

    def allClassesInFolder(name: String): Artifact = AnyFolder(Folder(name, AnyFile()))
  }

  // For now, folders contains String instead of instances of Folder to keep things simple
  final case class Repository(url: String, folders: List[String] = List.empty) {
    def in(sourceFolder: String): Repository = Repository(this.url, this.folders :+ sourceFolder)
  }

  sealed trait History

  case object Latest extends History

  final case class Range(from: String, to: String) extends History

  final case class Version(id: String) extends History {
    def to(version: String): History = Range(id, version)
  }

  /**
   * "Core language"
   */

  object IO {

    sealed trait FileSystem

    case class Folder(name: String, subfolders: Set[Folder] = Set.empty, files: Set[File] = Set.empty) extends FileSystem

    case class File(name: String) extends FileSystem

    type Path = String
  }

  type Error = String

  def repositoryExists(repository: Repository): Either[Error, Boolean] = {
    Either.cond(!repository.url.isBlank, true, "The repository is missing")
  }

  def retrieveVersions(history: History): Either[Error, List[Version]] = {
    history match {
      case Latest => Left("Latest version is not yet supported")
      case Range(from, to) => Right(timeline.mock.vcs.versionRange(from, to).map(id => Version(id)))
      case version@Version(_) => Right(List(version))
    }
  }

  def retrieveChanges(versions: List[Version]): Either[Error, Map[Version, IO.FileSystem]] = {
    Right(versions.map(
      version => timeline.mock.vcs.getDiffs(version) match {
        case Some(x) => version -> x
      }).toMap)
  }

  def resolveArtifacts(what: Artifact.Artifact, changes: Map[Version, IO.FileSystem]): Either[Error, Map[Version, Set[Path]]] = {
    Right(changes map { case (version, fileSystem) => version -> resolve(what, fileSystem) })
  }

  def resolve(what: Artifact.Artifact, filesystem: IO.FileSystem, path: Path = ""): Set[Path] = {
    (what, filesystem) match {
      case (Artifact.AnyFile(), IO.File(name)) => Set(s"$path/$name")

      case (Artifact.Folder(name, artifact), Folder(fsName, subfolders, files)) if (name == fsName) =>
        artifact match {
          case AnyFile() => files.flatMap(file => resolve(AnyFile(), file, path + '/' + fsName))
          case subfolder => subfolders.flatMap(folder => resolve(subfolder, folder, path + '/' + fsName))
        }

      case (a@Artifact.AnyFolder(b@Artifact.Folder(name, _)), f@Folder(fsName, subfolders, _)) =>
        name match {
          case _ if name == fsName => resolve(b, f, path) // consume any folder
          case _ => subfolders.flatMap(folder => resolve(a, folder, path + '/' + fsName)) // propagate any folder
        }

      case (a, b) => println(s"Case unsupported: ($a, $b)"); Set.empty
    }
  }

  /**
   * "Low level language"
   */

  case class Task(version: Version, artifacts: Set[Path])

  def dispatcher(resolvedChanges: Map[Version, Set[Path]]): Either[Error, Set[Task]] = {
    // This is a naive conversion to task, each file gets it's own task
    val result = for {
      (version, paths) <- resolvedChanges
      path <- paths
    } yield Task(version, Set(path))

    Right(result.toSet)
  }

  def analyze(task: timeline.Task): Map[Path, Int] = {
    timeline.mock.analyzer.run(task)
  }

  type Cache = Map[Version, Set[(Path, Int)]]

  def executeTasks(tasks: Set[Task]): Either[Error, Cache] = {
    // Group the result of each task by version
    Right(tasks.map(task => (task.version, analyze(task))).groupBy(_._1).map(e => e._1 -> e._2.flatMap(_._2)))
  }

  @tailrec
  def postProcess(versions: List[Version], cache: Cache, carry: Set[(Path, Int)] = Set.empty): Unit = {
    versions match {
      case head :: tail =>
        println(s"Version ${head.id}:")
        val option = cache.get(head)

        option match {
          case Some(data) =>
            val accResult = data ++ carry.filter(oldTuple => !data.map(tuple => tuple._1).contains(oldTuple._1))
            accResult.toList.sortWith((left, right) => left._1.compareTo(right._1) < 0).foreach(tuple => println(f"   ${tuple._1}%-15s -> ${tuple._2}"))
            postProcess(tail, cache, accResult)
          case None =>
            println("   No files matching the query")
            postProcess(tail, cache, carry)
        }
      case Nil =>
    }
  }
}
