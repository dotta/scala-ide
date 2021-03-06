/*
 * Copyright (c) 2014 Contributor. All rights reserved.
 */
package org.scalaide.debug.internal.expression

import org.eclipse.core.internal.resources.ResourceException
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.NullProgressMonitor
import org.junit.AfterClass
import org.junit.BeforeClass
import org.scalaide.core.testsetup.SDTTestUtils
import org.scalaide.core.testsetup.TestProjectSetup
import org.scalaide.debug.internal.ScalaDebugRunningTest
import org.scalaide.debug.internal.ScalaDebugTestSession
import org.scalaide.logging.HasLogger

class CommonIntegrationTestCompanion(projectName: String)
  extends TestProjectSetup(projectName, bundleName = "org.scala-ide.sdt.debug.expression.tests")
  with ScalaDebugRunningTest
  with HasLogger {

  var session: ScalaDebugTestSession = null

  protected def initDebugSession(launchConfigurationName: String): ScalaDebugTestSession =
    ScalaDebugTestSession(file(launchConfigurationName + ".launch"))

  private val testName = getClass.getSimpleName.init

  @BeforeClass
  def setup(): Unit = {
    logger.info(s"Test $testName started")
  }

  @AfterClass
  def doCleanup(): Unit = {
    logger.info(s"Test $testName finished")
    cleanDebugSession()
    deleteProject()
  }

  protected def refreshBinaryFiles(): Unit = {
    project.underlying.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor)
    project.underlying.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor)
  }

  protected def initializeEvaluator(session: ScalaDebugTestSession): JdiExpressionEvaluator = {
    val target = session.debugTarget
    new JdiExpressionEvaluator(target.classPath)
  }

  private def cleanDebugSession(): Unit = {
    if (session ne null) {
      session.terminate()
      session = null
    }
  }

  private def deleteProject(): Unit = {
    try {
      SDTTestUtils.deleteProjects(project)
    } catch {
      case e: ResourceException => // could not delete resource, but don't you worry ;)
    }
  }
}
