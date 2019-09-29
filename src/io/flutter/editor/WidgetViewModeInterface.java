/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.markup.CustomHighlighterRenderer;
import io.flutter.inspector.InspectorService;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface WidgetViewModeInterface extends CustomHighlighterRenderer {
  void updateVisibleArea(Rectangle newRectangle);

  void onInspectorAvailable();

  void onVisibleChanged();

  void forceRender();

  void onInspectorDataChange(boolean invalidateScreenshot);

  void onSelectionChanged();

  void onMouseMoved(MouseEvent event);

  void onMousePressed(MouseEvent event);
  void onMouseClicked(MouseEvent event);

  void onMouseEntered(MouseEvent event);

  void onMouseExited(MouseEvent event);

  void updateSelected(Caret carat);

  void onFlutterFrame();
}
