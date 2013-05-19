/*
 * Copyright 2005-2010 LAMP/EPFL
 */
// $Id$

package scala.tools.eclipse

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.search.SearchDocument
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer
import scala.tools.eclipse.javaelements.ScalaSourceFile
import scala.tools.eclipse.contribution.weaving.jdt.indexerprovider.IIndexerFactory
import scala.tools.eclipse.logging.HasLogger
import org.eclipse.jdt.core.search.IJavaSearchScope
import scala.tools.eclipse.buildmanager.BuildProblemMarker
import org.eclipse.core.resources.IMarker

class ScalaSourceIndexerFactory extends IIndexerFactory {
  override def createIndexer(document : SearchDocument) = new ScalaSourceIndexer(document);
}

class ScalaSourceIndexer(document : SearchDocument) extends AbstractIndexer(document) with HasLogger {
  private lazy val source: Option[ScalaSourceFile] = ScalaSourceFile.createFromPath(document.getPath)

  override def indexDocument() {
    logger.info("Indexing document: "+document.getPath)
    source.map(_.addToIndexer(this))
  }
  
  override protected def addTypeDeclaration(modifiers: Int, packageName: Array[Char], name: Array[Char], enclosingTypeNames: Array[Array[Char]], secondary: Boolean): Unit = {
    super.addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary)
    validatePackageDeclAgainstFilesystem(packageName)
  }
  
  private def validatePackageDeclAgainstFilesystem(packageName: Array[Char]): Unit = {
    val sourcePath = source.get.getResource().getLocation()
    val sourceFolder = source.get.scalaProject.sourceFolders.collect { 
      case path if path.isPrefixOf(sourcePath) => path
    }
    if(sourceFolder.isEmpty || sourceFolder.size > 1) logger.error("This is a bug")
    else {
      val relativePath = sourcePath.makeRelativeTo(sourceFolder.head)
      val sourcePackageName = relativePath.removeLastSegments(1)
      val expectedPackageName = sourcePackageName.segments().mkString(".")
      val currentPackageName = packageName.mkString
      if(currentPackageName != expectedPackageName) {
        val message = s"Package name of source ${relativePath.toOSString()} doesn't match filesystem location"
        BuildProblemMarker.create(source.get.getResource(), IMarker.SEVERITY_WARNING, message)
      }
    }
  }
  
  // copied from `org.eclipse.jdt.internal.core.index.Index.containerRelativePath`
  private def containerRelativePath(documentPath: String): String = {
    val index = documentPath.indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR);
    val p1= source.get.getResource().getProjectRelativePath()
    val p2= source.get.file.path
//    if (index == -1) {
//        scalaProject.underlying.getProjectRelativePath() 
//        index = this.containerPath.length();
//        if (documentPath.length() <= index)
//            throw new IllegalArgumentException("Document path " + documentPath + " must be relative to " + this.containerPath); //$NON-NLS-1$ //$NON-NLS-2$
//    }
    documentPath.substring(index + 1);
  }
}
