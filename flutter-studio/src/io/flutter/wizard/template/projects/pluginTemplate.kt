/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template.projects

import com.android.tools.idea.npw.model.NewProjectModel.Companion.getSuggestedProjectPackage
import com.android.tools.idea.wizard.template.BooleanParameter
import com.android.tools.idea.wizard.template.CheckBoxWidget
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.StringParameter
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.booleanParameter
import com.android.tools.idea.wizard.template.camelCaseToUnderlines
import com.android.tools.idea.wizard.template.extractClassName
import com.android.tools.idea.wizard.template.stringParameter
import io.flutter.npw.template.ModuleTemplateData
import io.flutter.npw.template.TemplateData
import io.flutter.npw.template.template
import java.io.File

// TODO Make this generic based on FlutterProjectType: get() = flutterTemplate(FlutterProjectType.PLUGIN, ...)
val pluginTemplate
  get() = template {
    name = "Flutter Plugin"
    description = "Creates a new Flutter plugin with an example app"

    lateinit var projectName: StringParameter
    val baseClassName: StringParameter = stringParameter {
      name = "Class name"
      suggest = { extractClassName(projectName.value) }
      default = "FlutterPlugin"
      visible = { false }
    }

    projectName = stringParameter {
      name = "Project name"
      constraints = listOf(Constraint.NONEMPTY)
      suggest = { camelCaseToUnderlines(baseClassName.value) }
      default = "flutter_plugin"
      help = "The name of the plugin"
    }

    val sdkSelector: StringParameter = stringParameter {
      name = "Flutter SDK"
      constraints = listOf(Constraint.NONEMPTY)
      //suggest = { baseClassName.value }
      default = ""
      help = "The path to the Flutter SDK"
    }

    val projectLocation: StringParameter = stringParameter {
      name = "Save location"
      constraints = listOf(Constraint.NONEMPTY)
      //suggest = { baseClassName.value }
      default = ""
      help = "The path to the parent directory of the new project"
    }

    val packageName: StringParameter = stringParameter {
      name = "Package name"
      constraints = listOf(Constraint.NONEMPTY)
      suggest = { getSuggestedProjectPackage() }
      default = getSuggestedProjectPackage()
      help = "The package name for new classes"
    }

    val isOffline: BooleanParameter = booleanParameter {
      name = "Create off-line"
      default = false
      help = "If true, this plugin will be created in off-line mode, meaning pub packages will not be downloaded"
    }

    val useKotlin: BooleanParameter = booleanParameter {
      name = "Use Kotlin in generated Android code"
      default = true
      help = "If false, Java will be used"
    }

    val useSwift: BooleanParameter = booleanParameter {
      name = "Use Swift in generated iOS code"
      default = true
      help = "If false, Objective-C will be used"
    }

    val useLegacyLibraries: BooleanParameter = booleanParameter {
      name = "Use legacy android.support libraries"
      default = false
      help = "If true, this plugin uses AndroidX libraries"
    }

    widgets(
      TextFieldWidget(projectName),
      TextFieldWidget(sdkSelector),
      TextFieldWidget(projectLocation),
      PackageNameWidget(packageName),
      CheckBoxWidget(isOffline),
      CheckBoxWidget(useKotlin),
      CheckBoxWidget(useSwift),
      CheckBoxWidget(useLegacyLibraries)
    )

    thumb {
      File("template_basic_activity.png")
    }

    recipe = { data: TemplateData ->
      generatePlugin(
        data as ModuleTemplateData, projectName.value, sdkSelector.value, projectLocation.value, packageName.value,
        isOffline.value, useKotlin.value, useSwift.value, useLegacyLibraries.value)
    }

  }
