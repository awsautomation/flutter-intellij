/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

// Builder for ModuleTemplateData.
class ModuleTemplateDataBuilder(val projectTemplateDataBuilder: ProjectTemplateDataBuilder, val isNew: Boolean) {
  var name: String? = null
  var path: String? = null

  fun build() = ModuleTemplateData(
    projectTemplateDataBuilder.build(),
    name!!,
    path!!
  )
}
