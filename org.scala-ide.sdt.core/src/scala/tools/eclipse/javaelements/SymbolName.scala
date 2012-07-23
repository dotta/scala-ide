package scala.tools.eclipse.javaelements

import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.nsc.symtab.Flags

trait SymbolName { self: ScalaPresentationCompiler =>

  /** Return the simple class name (i.e., without the package qualifier).*/
  def simpleClassName(sym: Symbol): String = javaSimpleName(sym)
  
  def simpleJavaClassName(sym: Symbol): String = mapSimpleType(sym)

  /** Return the full class name (i.e., with the package qualifier).*/
  def fullClassName(sym: Symbol): String = javaClassName(sym)
  
  def fullJavaClassName(sym: Symbol): String = mapType(sym)

  def declaredClassName(sym: Symbol): String = {
    if (isAnonymousClass(sym)) {
      val superClassName = simpleClassName(sym.superClass)
      val interfaceNames = sym.mixinClasses.map(simpleClassName)
      val mixings = (superClassName :: interfaceNames) filterNot (s => s == definitions.ObjectClass || s == definitions.ScalaObjectClass)
      mixings.mkString(" with ")
    } else simpleClassName(sym)
  }

  private def isTrait(sym: Symbol): Boolean = sym hasFlag Flags.TRAIT
  private def isAnonymousClass(sym: Symbol) = sym.isAnonymousClass
}