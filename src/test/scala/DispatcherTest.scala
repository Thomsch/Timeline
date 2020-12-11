import timeline.{Version, dispatcher}

object DispatcherTest extends App{
  val resolvedChanges = Map(Version("1") -> Set("p1", "p2"), Version("2") -> Set("p3"))
  dispatcher(resolvedChanges) match {
    case Right(result) => result.foreach(task => println(task))
  }
}
