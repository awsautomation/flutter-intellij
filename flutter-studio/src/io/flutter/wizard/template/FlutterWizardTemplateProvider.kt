/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template

import io.flutter.npw.template.Template
import io.flutter.wizard.template.projects.appTemplate
import io.flutter.wizard.template.projects.moduleTemplate
import io.flutter.wizard.template.projects.packageTemplate
import io.flutter.wizard.template.projects.pluginTemplate

/**
 * Implementation of the Android Wizard Template plugin extension point.
 */
class FlutterWizardTemplateProvider {
  fun getTemplates(): List<Template> = listOf(
    //appTemplate,
    //moduleTemplate,
    //packageTemplate,
    pluginTemplate
  )
}
