/*
 * Copyright 2005-2010 LAMP/EPFL
 * @author Josh Suereth
 */
// $Id$

package scala.tools.eclipse.properties

import org.eclipse.core.runtime.preferences.{ AbstractPreferenceInitializer, DefaultScope }
import scala.tools.nsc.Settings
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.SettingConverterUtil._
import scala.tools.eclipse.util.Utils
import scala.tools.eclipse.logging.HasLogger

/** This is responsible for initializing Scala Compiler
  * Preferences to their default values.
  */
class ScalaCompilerPreferenceInitializer extends AbstractPreferenceInitializer with HasLogger {

  /** Actually initializes preferences */
  def initializeDefaultPreferences(): Unit = {

    Utils.tryExecute {
      val store = ScalaPlugin.prefStore

      def defaultPreference(s: Settings#Setting) {
        val preferenceName = convertNameToProperty(s.name)
        s match {
          case bs: Settings#BooleanSetting     => store.setDefault(preferenceName, "false")
          case is: Settings#IntSetting         => store.setDefault(preferenceName, is.default.toString)
          case ss: Settings#StringSetting      => store.setDefault(preferenceName, ss.default)
          case ms: Settings#MultiStringSetting => store.setDefault(preferenceName, "")
          case cs: Settings#ChoiceSetting      => store.setDefault(preferenceName, cs.default)
        }
      }

      val defaultSettings = ScalaPlugin.defaultScalaSettings()
      IDESettings.shownSettings(defaultSettings).foreach { _.userSettings.foreach(defaultPreference) }
      val defaultPluginsDir = {
        ScalaPlugin.plugin.defaultPluginsDir getOrElse {
          eclipseLog.error("Failed to correctly initialized compiler settings `Xpluginsdir`. Reason: No default location found. Value is set to empty.")
          ""
        }
      }
      store.setDefault(convertNameToProperty(defaultSettings.pluginsDir.name), defaultPluginsDir)
      IDESettings.buildManagerSettings.foreach { _.userSettings.foreach(defaultPreference) }
      store.setDefault(convertNameToProperty(ScalaPluginSettings.stopBuildOnErrors.name), true)
      store.setDefault(convertNameToProperty(ScalaPluginSettings.debugIncremental.name), false)
      store.setDefault(convertNameToProperty(ScalaPluginSettings.withVersionClasspathValidator.name), true)
    }
  }
}
