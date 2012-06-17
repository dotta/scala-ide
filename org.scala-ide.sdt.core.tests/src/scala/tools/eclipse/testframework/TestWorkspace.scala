package scala.tools.eclipse.testframework

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.testsetup.FileUtils
import scala.tools.eclipse.util.EclipseUtils
import scala.tools.eclipse.util.OSGiUtils

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IWorkspaceDescription
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

abstract class TestWorkspace {
  protected val workspaceName = "test-workspace"

  protected def testBundleName: String

  def workspaceLocation: IPath = {
    val bundle = Platform.getBundle(testBundleName)
    OSGiUtils.pathInBundle(bundle, File.separatorChar + workspaceName).get
  }

  def workspace: IWorkspace = ResourcesPlugin.getWorkspace

  private def workspaceDescription: IWorkspaceDescription = workspace.getDescription

  def file(relativePath: IPath): IPath = {
    val path = workspaceLocation.append(relativePath)
    Assert.assertPathExists(path)
    path
  }

  /** Enable workspace auto-building */
  def enableAutoBuild(enable: Boolean): Unit = {
    // auto-building is on
    val desc = workspace.getDescription
    desc.setAutoBuilding(enable)
    workspace.setDescription(desc)
  }

  def isAutoBuilding: Boolean = workspaceDescription.isAutoBuilding

  def setupProject(name: String): ScalaProject = setupProject(new Path(name))

  /**
   * Setup the project in the target workspace. The 'name' project should
   * exist in the source workspace.
   */
  def setupProject(relativePath: IPath): ScalaProject = {
    EclipseUtils.workspaceRunnableIn(workspace) { monitor =>
      val wspaceLoc = workspace.getRoot.getLocation
      val src = new File(workspaceLocation.toFile.getAbsolutePath + File.separatorChar + relativePath.toOSString)
      val dst = new File(wspaceLoc.toFile.getAbsolutePath + File.separatorChar + relativePath.toOSString)
      println("copying %s to %s".format(src, dst))
      FileUtils.copyDirectory(src, dst)
      val project = workspace.getRoot.getProject(relativePath.lastSegment)
      if (!relativePath.removeLastSegments(1).isEmpty) {
        val projectDescription = workspace.newProjectDescription(relativePath.lastSegment)
        val location = workspace.getRoot.getLocation.append(relativePath)
        projectDescription.setLocation(location)
        project.create(projectDescription, null)
      } else project.create(null)
      project.open(null)
      JavaCore.create(project)
    }
    ScalaPlugin.plugin.getScalaProject(workspace.getRoot.getProject(relativePath.lastSegment))
  }

  def createProjects(names: String*): Seq[ScalaProject] =
    names map (n => createProject(n, true))

  def createProject(projectName: String, withSourceRoot: Boolean = true): ScalaProject = {
    import org.eclipse.core.resources.ResourcesPlugin;
    import org.eclipse.jdt.internal.core.JavaProject;
    import org.eclipse.jdt.core._;
    import org.eclipse.jdt.launching.JavaRuntime;
    import scala.collection.mutable._;

    val workspaceRoot = workspace.getRoot();
    val project = workspaceRoot.getProject(projectName);
    project.create(null);
    project.open(null);

    val description = project.getDescription();
    description.setNatureIds(Array(ScalaPlugin.plugin.natureId, JavaCore.NATURE_ID));
    project.setDescription(description, null);

    val javaProject = JavaCore.create(project);
    javaProject.setOutputLocation(new Path("/" + projectName + "/bin"), null);

    var entries = new ArrayBuffer[IClasspathEntry]();
    val vmInstall = JavaRuntime.getDefaultVMInstall();
    val locations = JavaRuntime.getLibraryLocations(vmInstall);
    for (element <- locations)
      entries += JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null);

    if (withSourceRoot) {
      val sourceFolder = project.getFolder("/src");
      sourceFolder.create(false, true, null);
      val root = javaProject.getPackageFragmentRoot(sourceFolder);
      entries += JavaCore.newSourceEntry(root.getPath());
    }
    entries += JavaCore.newContainerEntry(Path.fromPortableString(ScalaPlugin.plugin.scalaLibId))
    javaProject.setRawClasspath(entries.toArray[IClasspathEntry], null);

    ScalaPlugin.plugin.getScalaProject(project);
  }

  def deleteProjects(projects: ScalaProject*): Unit = {
    scala.tools.eclipse.util.EclipseUtils.workspaceRunnableIn(ScalaPlugin.plugin.workspaceRoot.getWorkspace) { _ =>
      projects foreach (_.underlying.delete(true, null))
    }
  }
}