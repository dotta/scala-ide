package scala.tools.eclipse.javaelements

import org.junit.Before
import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.EclipseUserSimulator
import scala.tools.eclipse.util.EclipseUtils
import org.junit.After
import scala.tools.nsc.interactive.Response
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import scala.tools.eclipse.testsetup.TestProjectSetup
import scala.tools.eclipse.testsetup.SDTTestUtils
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnit4])
class SymbolNameTest {
  private final val TestProjectName = "symbol-name"

  private val simulator = new EclipseUserSimulator
  private var projectSetup: TestProjectSetup = _

  def project: ScalaProject = projectSetup.project

  @Before
  def createProject() {
    val scalaProject = simulator.createProjectInWorkspace(TestProjectName, withSourceRoot = true)
    projectSetup = new TestProjectSetup(TestProjectName) {
      override lazy val project = scalaProject
    }
  }

  @After
  def deleteProject() {
    SDTTestUtils.deleteProjects(project)
  }
  
  @Test
  def fullClassName() {
    val pkgName = "foo1.foo2"
    val typeName = "Foo"

    val content = """
    package %s
    class %s
    """.format(pkgName, typeName)
    
    val scu = simulator.createScalaCompilationUnit(pkgName, typeName, content)
    
    val fullTypeName = pkgName + "." + typeName
    
    val element = project.javaProject.findType(fullTypeName)
    val offset = element.getSourceRange.getOffset
    val length = element.getSourceRange.getLength
    
    project.withSourceFile(scu) { (src, compiler) =>
      val pos = compiler.rangePos(src, offset, offset, offset + length)
      val response = new Response[compiler.Tree]
      compiler.askTypeAt(pos, response)
      val typed = response.get
      val sym = typed.left.get.symbol
      val result = compiler.fullClassName(sym)
      assertEquals(fullTypeName, result)
      ()
    }(())
  }
}