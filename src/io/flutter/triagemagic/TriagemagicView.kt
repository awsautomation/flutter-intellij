/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package io.flutter.triagemagic

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.Label
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.UIUtil
import com.intellij.util.xmlb.annotations.Attribute
import icons.FlutterIcons
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

@com.intellij.openapi.components.State(
    name = "TriagemagicView",
    storages = [Storage("\$WORKSPACE_FILE$")]
)
class TriagemagicView : PersistentStateComponent<TriagemagicViewState>, Disposable {

  private val viewState: TriagemagicViewState = TriagemagicViewState()

  private var windowPanel: SimpleToolWindowPanel = SimpleToolWindowPanel(true)
  private var emptyContent: Content? = null


  override fun loadState(state: TriagemagicViewState) {
    this.viewState.copyFrom(state)
  }

  override fun getState(): TriagemagicViewState = viewState

  override fun dispose() {
    Disposer.dispose(this)
  }

  fun initToolWindow(project: Project, toolWindow: ToolWindow) {
    if (toolWindow.isDisposed) return

    updateForEmptyContent(toolWindow)
  }

  private fun updateForEmptyContent(toolWindow: ToolWindow) {
    // There's a possible race here where the tool window gets disposed while we're displaying contents.
    if (toolWindow.isDisposed) {
      return
    }

    // Display a 'No running applications' message.
    val contentManager = toolWindow.contentManager
    val panel = JPanel(BorderLayout())
    val label = JBLabel("Triagemagic Loaded", SwingConstants.CENTER)
    label.foreground = UIUtil.getLabelDisabledForeground()
    panel.add(label, BorderLayout.CENTER)
    emptyContent = contentManager.factory.createContent(panel, null, false)
    contentManager.addContent(emptyContent!!)
  }
}

class TriagemagicViewState {
  private val dispatcher = EventDispatcher.create(ChangeListener::class.java)

  @Attribute(value = "splitter-proportion")
  var splitterProportion = 0f

  @JvmName("getSplitterProportion1")
  fun getSplitterProportion(): Float {
    return if (splitterProportion <= 0.0f) 0.7f else splitterProportion
  }

  @JvmName("setSplitterProportion1")
  fun setSplitterProportion(value: Float) {
    splitterProportion = value
    dispatcher.multicaster.stateChanged(ChangeEvent(this))
  }

  fun addListener(listener: ChangeListener) {
    dispatcher.addListener(listener)
  }

  fun removeListener(listener: ChangeListener) {
    dispatcher.removeListener(listener)
  }

  // This attribute exists only to silence the "com.intellij.util.xmlb.Binding - no accessors for class" warning.
  @Attribute(value = "placeholder")
  var placeholder: String? = null
  fun copyFrom(other: TriagemagicViewState) {
    placeholder = other.placeholder
    splitterProportion = other.splitterProportion
  }
}