/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template.projects

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.template

val pluginTemplate get() = template {
  revision = 1
  name = "Flutter Plugin"
  minApi = MIN_ANDROID_API
  minBuildApi = MIN_ANDROID_API
  description = "Creates a new Flutter app"

  category = Category.Activity
  formFactor = FormFactor.Mobile
  screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry, WizardUiContext.NewProject, WizardUiContext.NewModule)

}
