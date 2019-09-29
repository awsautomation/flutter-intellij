/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.TextRange;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PreviewsForEditor implements WidgetViewModeInterface {

  private Balloon popup;

  public PreviewsForEditor(WidgetViewModelDataBase data) {
    this.data= data;
    previews = new ArrayList<>();
    overallPreview = new PreviewViewModel(new WidgetViewModelData(null, data));
  }

  private final WidgetViewModelDataBase data;
  // By convention the first preview is the full screenshot preview.
  private ArrayList<PreviewViewModel> previews;
  private final PreviewViewModel overallPreview;

  boolean includeFullScreenshot = true;

  public void relayout() {
    updateVisibleArea(data.data.visibleRect); // XXX
  }

  @Override
  public void updateVisibleArea(Rectangle newRectangle) {
    // TODO(jacobr): do a single layout pass here and then set the rectangles for each child.
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.updateVisibleArea(newRectangle);
    }
  }

  public void outlinesChanged(Iterable<WidgetIndentGuideDescriptor> newDescriptors) {
    final ArrayList<PreviewViewModel> newPreviews = new ArrayList<>();
    boolean changed = false;

    int i = 0;
    // TODO(jacobr): be smarter about reusing.
    for (WidgetIndentGuideDescriptor descriptor : newDescriptors) {
      if (descriptor.parent == null) {
        if (i >= previews.size() || !descriptor.equals(previews.get(i).getDescriptor())) {
          newPreviews.add(new PreviewViewModel(new WidgetViewModelData(descriptor, data)));
          changed = true;
        } else {
          newPreviews.add(previews.get(i));
          i++;
        }
      }
    }
    while ( i < previews.size()) {
      changed = true;
      previews.get(i).dispose();
      i++;
    }
    previews = newPreviews;
    if (changed) {
      relayout();
    }
  }

  @Override
  public void onInspectorAvailable() {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onInspectorAvailable();
    }
  }

  private Iterable<PreviewViewModel> getAllPreviews(boolean paintOrder) {
    final ArrayList<PreviewViewModel> all = new ArrayList<>();
    all.add(overallPreview);
    all.addAll(previews);
    if (paintOrder ) {
      all.sort((a, b) -> { return Integer.compare(a.getPriority(), b.getPriority());});
    } else {
      all.sort((a, b) -> {
        return Integer.compare(b.getPriority(), a.getPriority());
      });
    }
    return all;
  }

  @Override
  public void onVisibleChanged() {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onVisibleChanged();
    }
  }

  @Override
  public void forceRender() {
    /// XXX only one force is needed given current hacks.
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.forceRender();
    }
  }

  @Override
  public void onInspectorDataChange(boolean invalidateScreenshot) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onInspectorDataChange(invalidateScreenshot);
    }

  }

  @Override
  public void onSelectionChanged() {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onSelectionChanged();
    }
  }

  @Override
  public void onMouseMoved(MouseEvent event) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      if (event.isConsumed()) {
        preview.onMouseExited(event);
      } else {
        preview.onMouseMoved(event);
      }
    }

  }

  @Override
  public void onMousePressed(MouseEvent event) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onMousePressed(event);
      if (event.isConsumed()) break;
    }
    if (!event.isConsumed() && event.isShiftDown()) {
      event.consume();
      final LogicalPosition logicalPosition = data.editor.xyToLogicalPosition(event.getPoint());
      System.out.println("XXX logicalPosition = " + logicalPosition);

      XSourcePositionImpl position = XSourcePositionImpl.create(data.editor.getVirtualFile(), logicalPosition.line, logicalPosition.column);
      Point point = event.getLocationOnScreen();

      if (popup != null) {
        popup.dispose();;
        popup = null;
      }
      popup = PropertyEditorPanel.showPopup(data.getApp(), data.editor, null, position, data.flutterDartAnalysisService, point);
    } else {
      if (popup != null) {
        popup.dispose();
      }
    }
  }

  @Override
  public void onMouseClicked(MouseEvent event) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onMouseClicked(event);
      if (event.isConsumed()) break;
    }
  }

  @Override
  public void onMouseEntered(MouseEvent event) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      if (event.isConsumed()) {
        preview.onMouseExited(event);
      } else {
        preview.onMouseEntered(event);
      }
    }

  }

  @Override
  public void onMouseExited(MouseEvent event) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.onMouseExited(event);
    }
  }

  @Override
  public void updateSelected(Caret carat) {
    for (PreviewViewModel preview : getAllPreviews(false)) {
      preview.updateSelected(carat);
    }
  }

  @Override
  public void onFlutterFrame() {
    for (PreviewViewModel preview : getAllPreviews(true)) {
      preview.onFlutterFrame();
    }
  }

  @Override
  public void paint(@NotNull Editor editor, @NotNull RangeHighlighter highlighter, @NotNull Graphics graphics) {
    for (PreviewViewModel preview : getAllPreviews(true)) {
      if (preview.visible) {
        preview.paint(editor, highlighter, graphics);
      }
    }
  }
}
