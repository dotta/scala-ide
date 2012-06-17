package scala.tools.eclipse.testframework

import scala.tools.eclipse.javaelements.ScalaSourceFile

import org.eclipse.core.runtime.IPath
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

object Assert {
  /** Assert that no errors are reported for the passed `unit`. */
  def assertNoErrors(unit: ScalaSourceFile): Unit = {
    val oProblems = Option(unit.getProblems())
    for (problems <- oProblems if problems.nonEmpty) {
      val errMsg = problems.mkString("-", "\n", ".")
      fail("Found unexpected problem(s):\n" + errMsg)
    }
  }

  def assertPathExists(path: IPath): Unit =
    assertTrue("`%s` does not exist in the file system.".format(path.toOSString), path.toFile.exists)
}