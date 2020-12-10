import timeline.Artifact.{AnyFile, AnyFolder}
import timeline.IO.{File, Folder}

package object timeline {

  object Artifact {
    sealed trait Artifact
    // What if we just provide Artifact and the users are free to define their language hierarchy on the fly?
    // They would just need to implement what they need and that's it.

    final case class Folder(name: String, artifact: Artifact) extends Artifact

    final case class File(name: String) extends Artifact

    final case class AnyFolder(artifact: Artifact) extends Artifact

    final case class AnyFile() extends Artifact

    def allFiles(): Artifact = AnyFolder(AnyFile())

    def allClassesInFolder(name: String): Artifact = AnyFolder(Folder(name, AnyFile()))

    def allClassesInRecursiveFolder(folder: String): Artifact = AnyFolder(Folder(folder, AnyFolder(AnyFile())))
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


  object IO {
    sealed trait FileSystem

    case class Folder(name: String, subfolders: Set[Folder] = Set.empty, files: Set[File] = Set.empty) extends FileSystem
    case class File(name: String) extends FileSystem
  }

  type Error = String

  type VersionedArtifacts = Map[Version, List[Artifact.Artifact]]

  type Path = String

  def repositoryExists(repository: Repository): Either[Error, Unit] = {
    Either.cond(repository.url.isBlank, println("Repository checked!"), "The repository is missing")
  }

  def retrieveVersions(history: History): Either[Error, List[Version]] = {
    history match {
      case Latest => ??? // Obtain latest from VCS
      case Range(from, to) => ??? // Obtain list of version from VCS
      case version@Version(_) => Right(List(version))
    }
  }

  def retrieveChanges(versions: List[Version]): Either[Error, VersionedArtifacts] = ???

  def resolveArtifacts(what: Artifact.Artifact, artifacts: VersionedArtifacts): Either[Error, Map[Version, List[Path]]] = ???

  def resolve(what: Artifact.Artifact, filesystem: IO.FileSystem, path:Path = ""): Set[Path] = {
    (what, filesystem) match {
      case (Artifact.AnyFile(), IO.File(name)) => Set(s"$path/$name")

      case (Artifact.Folder(name, artifact), Folder(fsName, subfolders, files)) if(name == fsName) =>
        artifact match {
          case AnyFile() => files.flatMap(file => resolve(AnyFile(), file, path + '/' + fsName))
          case subfolder => subfolders.flatMap(folder => resolve(subfolder, folder, path + '/' + fsName))
        }

      case (a@Artifact.AnyFolder(b@Artifact.Folder(name, _)), f@Folder(fsName, subfolders, _)) =>
        name match {
          case consumeAnyFolder if name == fsName => resolve(b, f, path)
          case propagateAnyFolder => subfolders.flatMap(folder => resolve(a, folder, path + '/' + fsName))
        }

      case (a, b) => println(s"Case unsupported: ($a, $b)"); Set.empty
    }
  }

  case class Task(version: Version, artifacts:Set[Path])

  def dispatcher(resolvedChanges: Map[Version, List[Path]]): Either[Error, Set[Task]] = {
    // This is a naive conversion to task, each file gets it's own task

    val result = for {
      (version, paths) <- resolvedChanges
      path <- paths
    } yield Task(version, Set(path))

    Right(result.toSet)
  }

  case class Cache() {
    def get(version: Version, artifact: Path): Int = {
      ???
    }
  }

  def analyze(task: timeline.Task, cache: Cache): Cache = {
    val result = timeline.mock.analyzer.run(task)
//    Cache(result, cache)
    Cache()
  }

  def executeTasks(tasks: Set[Task]): Either[Error, Cache] = {
    var cache = Cache()
    for(task <- tasks){
      cache = analyze(task, cache)
    }
    Right(cache)
  }
}
