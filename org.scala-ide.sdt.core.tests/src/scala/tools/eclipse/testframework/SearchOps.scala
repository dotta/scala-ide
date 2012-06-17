package scala.tools.eclipse.testframework

import scala.collection.JavaConverters.asScalaBufferConverter

import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.SearchMatch
import org.eclipse.jdt.core.search.SearchParticipant
import org.eclipse.jdt.core.search.SearchPattern
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor

object SearchOps {
  def searchType(typeName: String): List[SearchMatch] = {
    val pattern = SearchPattern.createPattern(typeName,
      IJavaSearchConstants.CLASS,
      IJavaSearchConstants.REFERENCES,
      SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE)

    searchWorkspaceFor(pattern)
  }

  private def searchWorkspaceFor(pattern: SearchPattern): List[SearchMatch] = {
    val engine = new SearchEngine
    val participants = Array[SearchParticipant](SearchEngine.getDefaultSearchParticipant)
    val scope = SearchEngine.createWorkspaceScope()
    val requestor = new CollectingSearchRequestor
    engine.search(pattern, participants, scope, requestor, null)

    requestor.getResults.asScala.toList
  }
}
