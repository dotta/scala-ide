package scala.tools.eclipse.diagnostic

import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.util.SWTUtils.asyncExec

import org.eclipse.core.runtime.Plugin
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.preference.IPreferenceStore

object StartupDiagnostics extends HasLogger {
  
  private val InstalledVersionKey = ScalaPlugin.plugin.pluginId + ".diagnostic.currentPluginVersion" 
  private val AskDiagnostics = ScalaPlugin.plugin.pluginId + ".diagnostic.askOnUpgrade"
  
  def run(plugin: Plugin, prefStore: IPreferenceStore) {
    val previousVersion = prefStore.getString(InstalledVersionKey)
    val currentVersion = plugin.getBundle.getVersion.toString
    val beforeAskDiagnostics = prefStore.getBoolean(AskDiagnostics)
    prefStore.setDefault(AskDiagnostics, true)
    val askDiagnostics = prefStore.getBoolean(AskDiagnostics)
    
    logger.info("startup diagnostics: previous version = " + previousVersion)
    logger.info("startup diagnostics: CURRENT version = " + currentVersion)
 
    if (previousVersion != currentVersion) {
      prefStore.setValue(InstalledVersionKey, currentVersion)
      
      if (askDiagnostics) {
        asyncExec { 
            val labels = Array(IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, "Never")
            val dialog = 
              new MessageDialog(ScalaPlugin.getShell, "Run Scala Setup Diagnostics?", 
                null, "Upgrade of Scala plugin detected.\n\n" +
                "Run setup diagnostics to ensure correct plugin settings?",
                MessageDialog.QUESTION, labels, 0)
            dialog.open match {
              case 0 => // user pressed Yes
                DiagnosticDialog(ScalaPlugin.getShell).open()
              case 2 => // user pressed Never
                prefStore.setValue(AskDiagnostics, false)
              case _ => // user pressed close button (-1) or No (1)
            }
            
            plugin.savePluginPreferences // TODO: this method is deprecated, but the solution given in the docs is unclear and is not used by Eclipse itself. -DM
        }
      }
    }
  }
}
