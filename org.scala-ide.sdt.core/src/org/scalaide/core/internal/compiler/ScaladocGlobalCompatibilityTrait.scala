package org.scalaide.core.internal.compiler

import scala.tools.nsc.interactive.Global
import scala.tools.nsc.interactive
import scala.tools.nsc.doc.{ScaladocGlobalTrait => _, _}
import scala.tools.nsc.symtab.BrowsingLoaders

trait InteractiveScaladocAnalyzer extends interactive.InteractiveAnalyzer with ScaladocAnalyzer {
    val global : Global
    override def newTyper(context: Context) = new Typer(context) with InteractiveTyper with ScaladocTyper {
      override def canAdaptConstantTypeToLiteral = false
    }
  }

trait ScaladocGlobalCompatibilityTrait extends Global
   with scala.tools.nsc.doc.ScaladocGlobalTrait { outer =>

    // @see analogous member in scala.tools.nsc.interactive.Global
    override lazy val loaders = new {
    val global: outer.type = outer
    val platform: outer.platform.type = outer.platform } with BrowsingLoaders {

    // SI-5593 Scaladoc's current strategy is to visit all packages in search of user code that can be documented
    // therefore, it will rummage through the classpath triggering errors whenever it encounters package objects
    // that are not in their correct place (see bug for details)
    // (see also the symmetric comment in s.t.nsc.doc.ScaladocGlobalTrait)
    override protected def signalError(root: Symbol, ex: Throwable) {
      log(s"Suppressing error involving $root: $ex")
    }
  }
}
