sealed trait Artifact
// What if we just provide Artifact and the users are free to define their language hierarchy on the fly?
// They would just need to implement what they need and that's it.

object Artifact {
  def allClasses(): Artifact = AnyFolder(AnyFile(AnyClass()))

  def allClassesInFolder(name: String): Artifact = AnyFolder(Folder(name, AnyFile(AnyClass())))

  def allClassesInRecursiveFolder(folder: String): Artifact = AnyFolder(Folder(folder, AnyFolder(AnyFile(AnyClass()))))
}

final case class AnyFolder(artifact: Artifact) extends Artifact
final case class Folder(name:String, artifact: Artifact) extends Artifact
final case class AnyFile(artifact: Artifact) extends Artifact
final case class AnyClass() extends Artifact // This case class could accept an artifact to enable better portability

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

object Timeline extends App {
  println("Welcome to Timeline")

  val where = Repository("https://github.com/danilofes/refactoring-toy-example").in("src")
  val when = Version("aef023").to("4bbb34")
  val latest = Latest // latest commit in the repository

  val allClasses = Artifact.allClasses()
  val allClassesInFolder = Artifact.allClassesInFolder("foo")
  val allClassesInRecursiveFolder = Artifact.allClassesInRecursiveFolder("foo")
}
