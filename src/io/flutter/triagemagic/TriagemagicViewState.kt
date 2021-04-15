/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package io.flutter.triagemagic

import com.intellij.util.EventDispatcher
import com.intellij.util.xmlb.annotations.Attribute
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

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