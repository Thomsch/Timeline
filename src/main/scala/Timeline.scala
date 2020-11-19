sealed trait Artifact

final case class Folder(name: String, parent: Folder) extends Artifact

final case class File(name: String, parent: Folder) extends Artifact

final case class Class(name: String) extends Artifact

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
  val latest = Latest

  val oneClassFQ = Folder("Foo").folder("bar").class
  val everyFileInFolder = ???
  val everyFileInFolderRecursive = ???

  println(where)
  println(when)
}
