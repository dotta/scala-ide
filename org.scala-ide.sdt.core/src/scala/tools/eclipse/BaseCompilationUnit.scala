package scala.tools.eclipse

import scala.tools.nsc.util.{ BatchSourceFile, SourceFile }
import scala.tools.nsc.interactive.Response
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.core.resources.IFile

abstract class BaseCompilationUnit extends InteractiveCompilationUnit {

  override def workspaceFile: IFile = null

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  override def sourceFile(contents: Array[Char]): SourceFile = 
    new BatchSourceFile(file, contents)

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  def batchSourceFile(contents: Array[Char]): BatchSourceFile = {
    new BatchSourceFile(file, contents)
  }

  override def exists(): Boolean = true

  override def getContents: Array[Char] = file.toCharArray

  /** no-op */
  override def scheduleReconcile(): Response[Unit] = {
    val r = new Response[Unit]
    r.set()
    r
  }

  override def currentProblems: List[IProblem] = {
    scalaProject.withPresentationCompiler { pc =>
      pc.problemsOf(file)
    }(Nil)
  }

  /** Reconcile the unit. Return all compilation errors.
    * Blocks until the unit is type-checked.
    */
  override def reconcile(newContents: String): List[IProblem] =
    scalaProject.withPresentationCompiler { pc =>
      askReload(newContents.toCharArray)
      pc.problemsOf(file)
    }(Nil)

  def askReload(newContents: Array[Char] = getContents): Unit =
    scalaProject.withPresentationCompiler { pc =>
      val src = batchSourceFile(newContents)
      pc.withResponse[Unit] { response =>
        pc.askReload(List(src), response)
        response.get
      }
    }()
}