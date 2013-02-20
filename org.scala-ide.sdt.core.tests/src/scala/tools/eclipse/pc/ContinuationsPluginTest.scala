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

  private def sourcecode(pkg: IPackageFragment)(code: String): ScalaSourceFile = {
    val testCode = code.stripMargin
    val emptyPkg = simulator.createPackage("")
    simulator.createCompilationUnit(emptyPkg, "A.scala", testCode).asInstanceOf[ScalaSourceFile]
  }

  @Test
  def foo() {
    val emptyPkg = simulator.createPackage("")
    val source = sourcecode(emptyPkg) {
      """
        |import scala.util.continuations.reset
        |import scala.util.continuations.shift
        |
        |object Continuations extends App {
        |  reset {
        |    shift { k: (Int => Int) =>
        |      k(k(k(7)))
        |    } + 1
        |  } * 2 // result 20 
        |}
      """
    }
    
    val path = source.getPath().makeRelativeTo(project.sourceFolders.head)
    val raw = path.toOSString()
    projectSetup.open(path.toOSString())
    projectSetup.waitUntilTypechecked(source)

    projectSetup.assertNoErrors(source)
  }
}	