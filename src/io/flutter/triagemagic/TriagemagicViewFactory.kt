/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package io.flutter.triagemagic

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TriagemagicViewFactory : ToolWindowFactory, DumbAware {

  override fun init(toolWindow: ToolWindow) {
    toolWindow.isAvailable = true
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    DumbService.getInstance(project).runWhenSmart {
      (ServiceManager.getService(project, TriagemagicView::class.java)).initToolWindow(project, toolWindow)
    }
  }
}