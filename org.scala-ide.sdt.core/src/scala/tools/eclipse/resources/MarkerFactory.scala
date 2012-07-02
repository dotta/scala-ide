package scala.tools.eclipse.resources

import scala.tools.eclipse.util.EclipseUtils

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IProgressMonitor

object MarkerFactory {
  case class Position(offset: Int, length: Int, line: Int)
}

/**
 * Generic factory for creating resource's markers.
 *
 * Markers are a general mechanism for associating notes and meta-data with resources.
 *
 * Clients extending {{{MarkerFactory}}} are expected to provide an implementation for {{{markerId}}},
 * which is a unique identifier for all markers created through a {{{MarkerFactory}}} instance.
 *
 * Example:
 * {{{
 *     class SettingMarker extends MarkerFactory {
 *       override def markerId: String = "org.scala-ide.sdt.core.settingProblem"
 *     }
 * }}}
 */
trait MarkerFactory {
  /**
   * Create marker without a source position in the Problem view.
   * @param resource: The resource to use to create the marker (hence, the marker will be associated to the passed resource)
   * @param severity: Indicates the marker's error state. Its value can be one of:
   *                  [IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING, IMarker.SEVERITY_INFO]
   * @param msg: The text message displayed by the marker. Note, the passed message is truncated to 21000 chars.
   */
  def create(resource: IResource, severity: Int, msg: String): Unit = runInWorkspace(resource) { _ =>
    createMarkerWithAttributes(severity, msg)(resource)
  }

  /**
   * Create marker with a source position in the Problem view.
   * @param resource: The resource to use to create the marker (hence, the marker will be associated to the passed resource)
   * @param severity: Indicates the marker's error state. Its value can be one of:
   *                  [IMarker.SEVERITY_ERROR, IMarker.SEVERITY_WARNING, IMarker.SEVERITY_INFO]
   * @param msg: The text message displayed by the marker. Note, the passed message is truncated to 21000 chars.
   * @param pos: The source position for the marker.
   */
  def create(resource: IResource, severity: Int, msg: String, pos: MarkerFactory.Position): Unit = runInWorkspace(resource) { _ =>
    (createMarkerWithAttributes(severity, msg) andThen withPos(pos))(resource)
  }

  /** Delete all markers on the given resource, but not any of its members. */
  def delete(resource: IResource): Unit = delete(resource, IResource.DEPTH_ZERO)

  /** Delete all markers on the given resource, and its direct and indirect members at any depth. */
  def deleteAll(resource: IResource): Unit = delete(resource, IResource.DEPTH_INFINITE)

  private def delete(resource: IResource, depth: Int): Unit = runInWorkspace(resource) { _ =>
    resource.deleteMarkers(markerId, /* includeSubtypes = */ true, depth)
  }

  private def createMarkerWithAttributes(severity: Int, msg: String): IResource => IMarker =
    resource => (createMarker andThen update(severity, msg))(resource)

  private def createMarker: IResource => IMarker =
    _.createMarker(markerId)

  /** A unique identifier for the created marker. */
  protected def markerId: String

  private def runInWorkspace(resource: IResource)(f: IProgressMonitor => Unit): Unit =
    EclipseUtils.workspaceRunnableIn(resource.getWorkspace, null)(f)

  private def update(severity: Int, msg: String): IMarker => IMarker = marker => {
    marker.setAttribute(IMarker.SEVERITY, severity)
    // Marker attribute values are limited to <= 65535 bytes and setAttribute will assert if they
    // exceed this. To guard against this we trim to <= 21000 characters ... see
    // org.eclipse.core.internal.resources.MarkerInfo.checkValidAttribute for justification
    // of this arbitrary looking number
    val maxMarkerLen = 21000
    val trimmedMsg = msg.take(maxMarkerLen)

    val attrValue = trimmedMsg.map {
      case '\n' | '\r' => ' '
      case c           => c
    }

    marker.setAttribute(IMarker.MESSAGE, attrValue)
    marker
  }

  private def withPos(position: MarkerFactory.Position): IMarker => IMarker = marker => {
    if (position.offset != -1) {
      marker.setAttribute(IMarker.CHAR_START, position.offset)
      marker.setAttribute(IMarker.CHAR_END, position.offset + math.max(position.length, 1))
      marker.setAttribute(IMarker.LINE_NUMBER, position.line)
    }
    marker
  }
}