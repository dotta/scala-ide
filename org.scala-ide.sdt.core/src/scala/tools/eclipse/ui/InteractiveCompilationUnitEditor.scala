package scala.tools.eclipse.ui

import org.scalaide.core.compiler.InteractiveCompilationUnit

trait InteractiveCompilationUnitEditor {
  def getInteractiveCompilationUnit(): InteractiveCompilationUnit
}