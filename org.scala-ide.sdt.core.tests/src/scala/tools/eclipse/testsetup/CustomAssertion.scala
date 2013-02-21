package scala.tools.eclipse.testsetup

import scala.tools.eclipse.ScalaProject
import org.eclipse.jdt.core.ICompilationUnit
import scala.tools.eclipse.javaelements.ScalaSourceFile
import org.eclipse.jdt.core.compiler.IProblem
import org.junit.ComparisonFailure
import org.junit.Assert
import scala.collection.GenTraversable

trait CustomAssertion extends TestProjectSetup {

  /** Assert that no errors are reported for the passed `unit`. */
  def assertNoErrors(unit: ScalaSourceFile) {
    Option(unit.getProblems()) foreach { problems =>
      if (problems.nonEmpty)
        assertStringifiedArrayContentEquals("Found unexpected problem(s)", Array.empty[IProblem], problems)
    }
  }

  /** Assert that `expectedErrors` are reported in the passed `unit`. */
  def assertFoundErrors(unit: ScalaSourceFile)(expectedErrors: IProblem*) {
    assert(expectedErrors.nonEmpty, "Use `assertNoErrors` if you want to assert tha non errors should be found.")

    val problems = Option(unit.getProblems()) getOrElse Array.empty[IProblem]
    assertStringifiedArrayContentEquals("Found errors don't match", expectedErrors.toArray, problems)
  }

  /** Asserts that the passed `expected` and `actual` arrays contain the same elements, in the same order.
    *
    * @note We use the elements' `toString` method to check equality here (not a great idea in general, but it
    *       gets the job done here)
    */
  def assertStringifiedArrayContentEquals[T](msg: String, expected: Array[T], actual: Array[T]) = {
    val expectedStringified = expected.mkString("(", ",", ")")
    val actualStringified = actual.mkString("(", ",", ")")
    if (expectedStringified != actualStringified) throw new ComparisonFailure(msg, expectedStringified, actualStringified)
  }
}
