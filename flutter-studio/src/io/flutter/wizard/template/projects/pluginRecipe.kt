/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template.projects

import com.android.tools.idea.wizard.template.PackageName
import com.android.tools.idea.wizard.template.RecipeExecutor
import io.flutter.npw.template.ModuleTemplateData

fun RecipeExecutor.generatePlugin(
  moduleData: ModuleTemplateData,
  projectName: String,
  sdkSelector: String,
  projectPath: String,
  packageName: PackageName,
  isOffline: Boolean,
  useKotlin: Boolean,
  useSwift: Boolean,
  useLegacyLibraries: Boolean
) {}