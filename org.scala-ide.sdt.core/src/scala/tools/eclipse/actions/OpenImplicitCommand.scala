package scala.tools.eclipse.actions

import scala.tools.eclipse.ScalaSourceFileEditor
import scala.tools.eclipse.hyperlink.text.detector.BaseHyperlinkDetector
import scala.tools.eclipse.hyperlink.text.detector.ImplicitHyperlinkDetector
import scala.tools.eclipse.ui.actions.HyperlinkOpenActionStrategy
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.handlers.HandlerUtil

class OpenImplicitCommand extends AbstractHandler with HyperlinkOpenActionStrategy {
  override val detectionStrategy: BaseHyperlinkDetector = ImplicitHyperlinkDetector()

  override def execute(event: ExecutionEvent): Object = {
    HandlerUtil.getActiveEditor(event) match {
      case editor: ScalaSourceFileEditor =>
        openHyperlink(editor)
      case _ => ()
    }
    null
  }
}