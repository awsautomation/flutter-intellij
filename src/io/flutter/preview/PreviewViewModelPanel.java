/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.preview;

import com.intellij.openapi.ui.popup.Balloon;
import io.flutter.editor.PreviewViewModel;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class PreviewViewModelPanel extends JPanel {
  final PreviewViewModel preview;
  Balloon popup;

  public PreviewViewModelPanel(PreviewViewModel preview) {
    this.preview = preview;
    preview.updateVisibleArea(null);

    addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseDragged(MouseEvent e) {

      }

      @Override
      public void mouseMoved(MouseEvent e) {
        preview.onMouseMoved(e);
      }
    });
    addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent e) {
        // TODO(jacobr): verify popup case.
        preview.onMousePressed(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        preview.onMouseReleased(e);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        preview.onMouseEntered(e);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        preview.onMouseExited(e);
      }
    });
  }
}
