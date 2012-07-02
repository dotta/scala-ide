package scala.tools.eclipse

import scala.tools.eclipse.resources.MarkerFactory

object SettingProblemMarker extends MarkerFactory {
  override protected def markerId: String = ScalaPlugin.pluginId + ".settingProblem"
}
