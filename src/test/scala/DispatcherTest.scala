import timeline.{Version, dispatcher}

object DispatcherTest extends App{
  val resolvedChanges = Map(Version("1") -> List("p1", "p2"), Version("2") -> List("p3"))
  dispatcher(resolvedChanges) match {
    case Right(result) => result.foreach(task => println(task))
  }
}
