package scala.tools.eclipse.pc

import scala.tools.eclipse.EclipseUserSimulator
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.util.EclipseUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import scala.tools.eclipse.javaelements.ScalaCompilationUnit
import scala.tools.eclipse.testsetup.TestProjectSetup
import scala.tools.eclipse.testsetup.SDTTestUtils
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert
import scala.tools.eclipse.testsetup.CustomAssertion
import scala.tools.eclipse.javaelements.ScalaSourceFile
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.compiler.IProblem
import org.mockito.Mockito._

@RunWith(classOf[JUnit4])
class ContinuationsPluginTest {

  private final val TestProjectName = "continuations-plugin"

  private val simulator = new EclipseUserSimulator

  private var projectSetup: TestProjectSetup with CustomAssertion = _

  def project: ScalaProject = projectSetup.project

  @Before
  def createProject() {
    val scalaProject = simulator.createProjectInWorkspace(TestProjectName, withSourceRoot = true)
    projectSetup = new TestProjectSetup(TestProjectName) with CustomAssertion {
      override lazy val project = scalaProject
    }
    project.resetPresentationCompiler()
  }

  @After
  def deleteProject() {
    SDTTestUtils.deleteProjects(project)
  }

  private def openAndWaitUntilTypechecked(source: ScalaSourceFile) {
    val sourcePath = source.getPath()
    val projectSrcPath = project.underlying.getFullPath() append "src"
    val path = sourcePath.makeRelativeTo(projectSrcPath)
    projectSetup.open(path.toOSString())
    projectSetup.waitUntilTypechecked(source)
  }

  @Test
  def presentation_compiler_report_errors_when_continuations_plugin_is_not_enabled() {
    val source = projectSetup.createSourceFile("nok", "Continuations.scala") {
      """
        |import scala.util.continuations.reset
        |import scala.util.continuations.shift
        |
        |object Continuations extends App {
        |  val a: Int = reset {
        |    shift { k: (Int => Int) =>
        |      k(k(k(7)))
        |    } + 1
        |  }
        |  println(a * 2) // result 20
        |}
      """
    }

    openAndWaitUntilTypechecked(source)

    projectSetup.assertFoundErrors(source) {
      "Pb(0) this code must be compiled with the Scala continuations plugin enabled"
    }
  }

  @Test
  def presentation_compiler_does_not_report_errors_when_continuations_plugin_is_enabled(): Unit = withContinuationPluginEnabled {
    projectSetup
    val source = projectSetup.createSourceFile("ok", "Continuations.scala") {
      """
        |import scala.util.continuations.reset
        |import scala.util.continuations.shift
        |
        |object Continuations extends App {
        |  val a: Int = reset {
        |    shift { k: (Int => Int) =>
        |      k(k(k(7)))
        |    } + 1
        |  }
        |  println(a * 2) // result 20
        |}
      """
    }

    openAndWaitUntilTypechecked(source)

    projectSetup.assertNoErrors(source)
  }

  private def withContinuationPluginEnabled(body: => Unit) {
    val value = project.storage.getString("P")
    try {
      project.storage.setValue("P", "continuations:enable")
      body
    }
    finally {
      project.storage.setValue("P", value)
    }
  }

  implicit def string2problem(problemMessage: String): IProblem = {
    val problem = mock(classOf[IProblem])
    when(problem.toString).thenReturn(problemMessage)
    problem
  }
}