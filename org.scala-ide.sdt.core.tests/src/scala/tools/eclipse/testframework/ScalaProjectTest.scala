package scala.tools.eclipse.testframework

import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.javaelements.ScalaCompilationUnit
import scala.tools.eclipse.javaelements.ScalaSourceFile
import scala.tools.nsc.interactive.Response

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IProblemRequestor
import org.eclipse.jdt.core.WorkingCopyOwner
import org.mockito.Mockito.mock
import org.mockito.Mockito.when

object ScalaProjectTest {
  def apply(scalaProject: ScalaProject)(implicit context: TestContext): ScalaProjectTest = new ScalaProjectTest(scalaProject)
  def apply(name: String)(implicit context: TestContext): ScalaProjectTest = {
    val project = context.workspace.createProject(name, withSourceRoot = true)
    this(project)
  }
}

class ScalaProjectTest private (scalaProject: ScalaProject) extends JavaProjectTest(scalaProject.javaProject) {

  /** Return a sequence of Scala compilation units corresponding to the given paths. */
  def scalaCompilationUnits(paths: String*): Seq[ScalaSourceFile] =
    paths.map(scalaCompilationUnit)

  /**
   * Return the Scala compilation unit corresponding to the given path, relative to the src folder.
   *  for example: "scala/collection/Map.scala".
   */
  def scalaCompilationUnit(path: String): ScalaSourceFile =
    compilationUnit(path).asInstanceOf[ScalaSourceFile]

  /**
   * Emulate the opening of a scala source file (i.e., it tries to reproduce the steps performed
   * by JDT when opening a file in an editor) in read-only mode.
   *
   * Note that to open a working copy in write-mode you need to call {{{unit.becomeWorkingCopy}}},
   * which is not done here.
   *
   * @param srcPath the path to the scala source file
   */
  def openReadOnly(srcPath: String): ScalaSourceFile = {
    val unit = scalaCompilationUnit(srcPath)
    openWorkingCopyFor(unit)
    reload(unit)
    unit
  }

  /** Open a working copy for the passed {{{unit}}}. (read-mode)*/
  private def openWorkingCopyFor(unit: ScalaSourceFile): Unit = {
    val requestor = mock(classOf[IProblemRequestor])
    // the requestor must be active, or unit.getWorkingCopy won't trigger the Scala
    // structure builder
    when(requestor.isActive()).thenReturn(true)

    val owner = new WorkingCopyOwner() {
      override def getProblemRequestor(unit: org.eclipse.jdt.core.ICompilationUnit): IProblemRequestor = requestor
    }

    // this will trigger the Scala structure builder
    unit.getWorkingCopy(owner, new NullProgressMonitor)
  }

  /**
   * Ask the presentation compiler to reload (i.e., parsing the {{{unit}}} and then execute a
   * background compilation) the passed {{{unit}}}.
   */
  def reload(unit: ScalaCompilationUnit): Unit = {
    // first, 'open' the file by telling the compiler to load it
    scalaProject.withSourceFile(unit) { (src, compiler) =>
      val dummy = new compiler.Response[Unit]
      compiler.askReload(List(src), dummy)
      dummy.get
    }()
  }

  /** Wait until the presentation compiler has entirely typechecked the passed {{{unit}}}. */
  def waitUntilTypechecked(unit: ScalaCompilationUnit): Unit = {
    // give a chance to the background compiler to report the error
    scalaProject.withSourceFile(unit) { (source, compiler) =>
      import scala.tools.nsc.interactive.Response
      val res = new Response[compiler.Tree]
      compiler.askLoadedTyped(source, res)
      res.get // wait until unit is typechecked
    }()
  }
}