package scala.tools.eclipse.testframework

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.core.CompilationUnit
import org.junit.Assert.assertTrue
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.core.resources.IMarker
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IncrementalProjectBuilder
import scala.tools.eclipse.testsetup.SDTTestUtils
import scala.tools.eclipse.testsetup.FileUtils

object JavaProjectTest {
  def apply(javaProject: IJavaProject): JavaProjectTest = new JavaProjectTest(javaProject)
}

class JavaProjectTest protected (val javaProject: IJavaProject) {

  /** Return the handle */
  private def project: IProject = javaProject.getProject

  def file(path: String): IFile = project.getFile(path)
  
  /** The package root corresponding to /src inside the project. */
  val srcPackageRoot: IPackageFragmentRoot = {
    val packageRoot = javaProject.findPackageFragmentRoot(srcFolder)
    assertTrue("No package root for source folder `%s`".format(srcFolder), packageRoot.exists)
    assertTrue(packageRoot.getPath.lastSegment == "src")
    packageRoot.open(null)
    packageRoot
  }

  /**
   * Return the compilation unit corresponding to the given path, relative to the src folder.
   *  for example: "scala/collection/Map.scala"
   */
  def compilationUnit(path: String): CompilationUnit = {
    val segments = path.split("/")
    val pkg = segments.init.mkString(".")
    val pkgFragment = srcPackageRoot.getPackageFragment(pkg)
    val unit = pkgFragment.getCompilationUnit(segments.last)
    unit.asInstanceOf[CompilationUnit]
  }

  /** Return a sequence of compilation units corresponding to the given paths. */
  def compilationUnits(paths: String*): Seq[CompilationUnit] =
    paths.map(compilationUnit)

  def getErrorMessages(units: ICompilationUnit*): List[String] =
    SDTTestUtils.getErrorMessages(units: _*)

  def getProblemMarkers(units: ICompilationUnit*): List[IMarker] =
    SDTTestUtils.getProblemMarkers(units: _*)

  /** Return the Java problem markers corresponding to the given compilation unit. */
  def getProblemMarkers(unit: ICompilationUnit): List[IMarker] =
    SDTTestUtils.getProblemMarkers(unit)

  /** Find all offsets of the passed {{{marker}}} for a given compilation unit. */
  def findMarker(marker: String) = SDTTestUtils.findMarker(marker)

  /** Delete this {{{project}}} from the file system.*/
  def delete(): Unit = project.getProject.delete( /*deleteContent*/ true, /*force*/ true, new NullProgressMonitor)

  def srcFolder: IPath = javaProject.getPath.append("src")

  def addFile(folder: IPath, sourceName: String, content: Array[Byte]): Unit = {
    require(folder.toFile.isDirectory, "`%s` is not a directory".format(folder))
    val sourcePath = folder.append(sourceName)
    SDTTestUtils.addFileToProject(project, sourcePath.toPortableString, content)
  }

  def addFileinSrcRoot(sourceName: String, content: Array[Byte]): Unit =
    addFile(new Path(srcPackageRoot.getPath.lastSegment), sourceName, content)

  def copyAllInSrcFolder(from: IPath): Unit = {
    for (file <- from.toFile.listFiles) {
      val filePath = file.getAbsolutePath
      val relativeFilePath = new Path(filePath).makeRelativeTo(from)
      SDTTestUtils.addFileToProject(project, new Path("src").append(relativeFilePath).toPortableString, FileUtils.read(file))
    }
  }
}