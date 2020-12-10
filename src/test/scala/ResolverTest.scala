import timeline.Artifact.AnyFolder
import timeline._
import timeline.mock.vcs._

object ResolverTest extends App {
  val v = IO.Folder("ROOT",
    Set(
      IO.Folder("A",
        Set(IO.Folder("B",
          Set.empty,
          Set(IO.File("foo"), IO.File("foo2"))))),
      IO.Folder("B",
        Set.empty,
        Set(IO.File("trap")))),
    Set(IO.File("readme.md"), IO.File(".gitignore")))

  println(resolve(Artifact.Folder("ROOT", Artifact.AnyFile()), version1))
  println("===============")
  println(resolve(Artifact.AnyFolder(Artifact.Folder("B", Artifact.AnyFile())), version2))
  println("---------------")
  println(resolve(Artifact.AnyFolder(Artifact.Folder("B", Artifact.AnyFile())), version4))
  println("---------------")
  println(resolve(AnyFolder(Artifact.Folder("B", Artifact.AnyFile())), v))
  println("===============")
  println(resolve(AnyFolder(Artifact.Folder("A", Artifact.Folder("B", Artifact.AnyFile()))), v))
  println("---------------")
  println(resolve(Artifact.Folder("ROOT", Artifact.AnyFolder(Artifact.Folder("B", Artifact.AnyFile()))), version4))
}
