package org.scalaide.ui.internal.preferences

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.eclipse.jface.preference.PreferencePage
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Link
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPreferencePage
import org.eclipse.ui.dialogs.PreferencesUtil
import org.scalaide.core.IScalaPlugin
import org.scalaide.ui.editor.hover.IScalaHover._

/** This class is referenced through plugin.xml */
class HoverPreferencePage extends PreferencePage with IWorkbenchPreferencePage {

  private val prefStore = IScalaPlugin().getPreferenceStore()
  private var cssArea: Text = _

  override def createContents(parent: Composite): Control = {
    import org.scalaide.util.eclipse.SWTUtils.fnToSelectionAdapter

    val base = new Composite(parent, SWT.NONE)
    base.setLayout(new GridLayout(1, true))

    val fontLink = new Link(base, SWT.NONE)
    fontLink.setText("""The font of the hover can be changed by modifying the 'Scala → Hover' entry in the <a href="org.eclipse.ui.preferencePages.ColorsAndFonts">Colors and Fonts</a> preference page.""")
    fontLink.addSelectionListener { e: SelectionEvent =>
      PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null)
    }

    val description = new Label(base, SWT.NONE)
    description.setText("The style of the hovers is specified by the following CSS code, which can be freely edited:")

    cssArea = new Text(base, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
    cssArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
    cssArea.setText(prefStore.getString(ScalaHoverStyleSheetId))
    base
  }

  override def init(workbench: IWorkbench): Unit = ()

  override def performOk(): Boolean = {
    val css = cssArea.getText()
    prefStore.setValue(ScalaHoverStyleSheetId, css)
    super.performOk()
  }

  override def performDefaults(): Unit = {
    super.performDefaults()
    cssArea.setText(DefaultScalaHoverStyleSheet)
  }

}

/** This class is referenced through plugin.xml */
class HoverPreferenceInitializer extends AbstractPreferenceInitializer {

  override def initializeDefaultPreferences(): Unit = {
    val p = IScalaPlugin().getPreferenceStore()
    val oldCss = p.getString(DefaultScalaHoverStyleSheetId)
    val newCss = DefaultScalaHoverStyleSheet
    val usedCss = p.getString(ScalaHoverStyleSheetId)

    // This can only happen the very first time a workspace is created
    if (oldCss.isEmpty()) {
      p.setValue(DefaultScalaHoverStyleSheetId, newCss)
      p.setValue(ScalaHoverStyleSheetId, newCss)
    }
    // This happens every time the CSS file of the bundle is updated
    else if (oldCss != newCss) {
      p.setValue(DefaultScalaHoverStyleSheetId, newCss)
      p.setValue(ScalaHoverStyleSheetId, if (oldCss == usedCss) newCss else s"""|/*
          |==================================================================
          |ATTENTION:
          |
          |    This CSS file has been updated to a newer version.
          |    Your changed CSS file has been commented out in order to keep your changes.
          |    The new file can be found after this comment.
          |    Please merge your changes manually.
          |
          |==================================================================
          |$usedCss
          |*/
          |
          |$newCss
          |""".stripMargin)
    }
  }
}
