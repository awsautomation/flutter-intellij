/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.model

import com.android.annotations.concurrency.UiThread
import com.android.annotations.concurrency.WorkerThread
import com.android.tools.idea.npw.model.MultiTemplateRenderer
import com.android.tools.idea.observable.core.ObjectProperty
import com.android.tools.idea.observable.core.ObjectValueProperty
import com.android.tools.idea.observable.core.StringValueProperty
import com.android.tools.idea.wizard.model.WizardModel
import io.flutter.npw.recipes.createFlutterModule
import io.flutter.npw.template.ModuleTemplateData
import io.flutter.npw.template.ModuleTemplateDataBuilder
import io.flutter.npw.template.NamedModuleTemplate
import io.flutter.npw.template.ProjectTemplateDataBuilder
import io.flutter.npw.template.Recipe
import io.flutter.npw.template.TemplateData

interface ModuleModelData : FlutterProjectData {
  val template: ObjectProperty<NamedModuleTemplate>
  val moduleName: StringValueProperty
  val moduleTemplateDataBuilder: ModuleTemplateDataBuilder
}

// See com.android.tools.idea.npw.model.NewAndroidModuleModel
class NewFlutterModuleModel(
  name: String,
  val projectModelData: FlutterProjectData,
  _template: NamedModuleTemplate = with(projectModelData) {
    createDefaultTemplateAt(if (!isNewProject) project.value.basePath!! else "", name)
  },
  val moduleParent: String,
  val commandName: String = "New Module"
) : WizardModel(), FlutterProjectData by projectModelData, ModuleModelData {
  final override val template: ObjectProperty<NamedModuleTemplate> = ObjectValueProperty(_template)
  final override val moduleName = StringValueProperty(name).apply { addConstraint(String::trim) }
  val renderer: MultiTemplateRenderer.TemplateRenderer = ModuleTemplateRenderer()
  override val projectTemplateDataBuilder = ProjectTemplateDataBuilder(false)
  override val moduleTemplateDataBuilder = ModuleTemplateDataBuilder(projectTemplateDataBuilder, true)

  override fun handleFinished() {
    multiTemplateRenderer.requestRender(renderer)
  }

  inner class ModuleTemplateRenderer : MultiTemplateRenderer.TemplateRenderer {

    val recipe: Recipe get() = { data: TemplateData ->
        createFlutterModule(data as ModuleTemplateData, projectName.get())
      }

    /**
     * Runs any needed Model pre-initialisation, for example, setting Template default values.
     */
    @WorkerThread
    override fun init() {
    }

    /**
     * Run validation, but don't write any file
     *
     * @return true if the validation succeeded. Returning false will stop any call to [render]
     */
    @WorkerThread
    override fun doDryRun(): Boolean {
      TODO("Not yet implemented")
    }

    /**
     * Do the actual work of writing the files.
     * I.e. 'flutter create ...'
     */
    @WorkerThread
    override fun render() {
      TODO("Not yet implemented")
    }

    /**
     * Runs any needed Model finalization, for example, after generating a project, import it or open generated files on the editor.
     */
    @UiThread
    override fun finish() {
      TODO("Not yet implemented")
    }
  }
}

private fun createDefaultTemplateAt(path: String, name: String): NamedModuleTemplate {
  return NamedModuleTemplate(name, path)
}
