/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package io.flutter.triagemagic

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.Label
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.impl.JBTabsImpl

@com.intellij.openapi.components.State(
    name = "FlutterPreviewView",
    storages = [Storage("\$WORKSPACE_FILE$")]
)
class TriagemagicView : PersistentStateComponent<TriagemagicViewState> {

  private val viewState: TriagemagicViewState = TriagemagicViewState()

  private var windowPanel: SimpleToolWindowPanel = SimpleToolWindowPanel(true)

  override fun loadState(state: TriagemagicViewState) {
    this.viewState.copyFrom(state)
  }

  override fun getState(): TriagemagicViewState = viewState

  fun initToolWindow(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val contentManager = toolWindow.contentManager

    val content = contentFactory.createContent(null, null, false)
    content.isCloseable = false

    val tabs = JBTabsImpl(project)
    tabs.add(Label("Triage Issue"))
    tabs.add(Label("Response Template"))
    val toolbar = JBTabsImpl.Toolbar(tabs, null)
    windowPanel.toolbar = toolbar
    content.component = windowPanel
    contentManager.addContent(content)
    contentManager.setSelectedContent(content)

  }
}