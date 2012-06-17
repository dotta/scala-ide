package scala.tools.eclipse

import scala.tools.eclipse.testframework.TestContext
import scala.tools.eclipse.testframework.TestWorkspace

trait HasTestContext {
  implicit val context: TestContext = CoreTestContext
}

object CoreTestContext extends TestContext {
  val TestBundleName: String = "org.scala-ide.sdt.core.tests"
  private object CoreTestWorkspace extends TestWorkspace {
    protected val testBundleName: String = TestBundleName
  }
  
  override def workspace: TestWorkspace = CoreTestWorkspace
}