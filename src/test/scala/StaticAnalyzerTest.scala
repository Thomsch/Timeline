import timeline.{Task, Version}
import timeline.mock.analyzer._

object StaticAnalyzerTest extends App {
  println(staticAnalyzer("1", "path"))
  println(run(Task(Version("1"), Set("path"))))

  println(staticAnalyzer.getOrElse(("1", "Missing file"), 2))
}
