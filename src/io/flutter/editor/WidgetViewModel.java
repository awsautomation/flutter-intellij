/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.util.TextRange;
import io.flutter.inspector.DiagnosticsNode;
import io.flutter.inspector.InspectorObjectGroupManager;
import io.flutter.inspector.InspectorService;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static io.flutter.inspector.InspectorService.toSourceLocationUri;
import static java.lang.Math.min;

public abstract class WidgetViewModel implements WidgetViewModeInterface {
  protected boolean isSelected = false;
  final WidgetViewModelData data;

  boolean visible = false;
  private InspectorObjectGroupManager groups;
  boolean isDisposed = false;

  InspectorObjectGroupManager getGroups() {
    final InspectorService service = getInspectorService();
    if (service == null) return null;
    if (groups == null || groups.getInspectorService() != service) {
      groups = new InspectorObjectGroupManager(service, "active");
    }
    return groups;
  }

  public ArrayList<DiagnosticsNode> _nodes;
  public int activeIndex = 0;

  WidgetViewModel(WidgetViewModelData data ) {
    this.data = data;
    updateVisibleArea(data.data.visibleRect);
  }

  @Override
  public void updateVisibleArea(Rectangle newRectangle) {
    if (getDescriptor() == null) {
      if (!visible) {
        visible = true;
        onVisibleChanged();
      }
      return;
    }
    final TextRange marker = data.getMarker();
    if (marker == null) return;

    final Point start = offsetToPoint(marker.getStartOffset());
    final Point end = offsetToPoint(marker.getEndOffset());
    final boolean nowVisible = newRectangle.y <= end.y && newRectangle.y + newRectangle.height >= start.y ||
                               updateVisiblityLocked(newRectangle);
    if (visible != nowVisible) {
      visible = nowVisible;
      onVisibleChanged();
    }
  }
  public boolean updateVisiblityLocked(Rectangle newRectangle) { return false; }

  @Override
  public void onInspectorAvailable() {
    onVisibleChanged();
  }

  public Point offsetToPoint(int offset) {
    return data.editor.visualPositionToXY( data.editor.offsetToVisualPosition(offset));
  }

  @Override
  public void forceRender() {
    if (!visible) return;
    data.editor.getComponent().repaint(); // XXX repaint rect?
    /*
    if (data.descriptor == null) {
      // TODO(just repaint the sreenshot area.
      data.editor.repaint(0, data.document.getTextLength());
      return;
    }
    data.editor.repaint(0, data.document.getTextLength());

     */
/*
    final TextRange marker = data.getMarker();
    if (marker == null) return;

    data.editor.repaint(marker.getStartOffset(), marker.getEndOffset());
 */
  }

  public InspectorService getInspectorService() {
    if (data.data == null ) return null;
    return data.data.inspectorService;
  }

  boolean setSelection(boolean value) {
    if (value == isSelected) return false;
    isSelected = value;
    if (data.descriptor == null) {
      // TODO(jacobr): do we want to display any selection for the global preview?
      return true;
    }

    if (value) {
      computeActiveElements();
    }
    return true;
  }

  void dispose() {
    isDisposed = true;
    // Descriptors must be disposed so they stop getting notified about
    // changes to the Editor.
    // TODO(Jacobr): fix missing code disposing descriptors?
    if (groups != null) {
      groups.clear(false);// XXX??
    }
  }

  @Override
  public void onInspectorDataChange(boolean invalidateScreenshot) {

    final InspectorObjectGroupManager manager = getGroups();
    if (manager != null ){
      manager.cancelNext();;
    }
    // XX this might be too much.
    onVisibleChanged();
  }

  @Override
  public void onSelectionChanged() {
    onInspectorDataChange(false); // XXX a bit smarter and don't kill the screenshot.
  }

  void computeActiveElements() {
    InspectorObjectGroupManager groupManager = getGroups();
    if (groupManager == null) {
      return;
      // XXX be smart based on if the element actually changed. The ValueId should work for this.
      //        screenshot = null;
    }
    groupManager.cancelNext();
    InspectorService.ObjectGroup group = groupManager.getNext();
    // XXX
    final String file = toSourceLocationUri(data.editor.getVirtualFile().getPath());
    CompletableFuture<ArrayList<DiagnosticsNode>> nodesFuture;
    if (data.descriptor == null) {
      // Special case for whole app preview.
      nodesFuture = group.getElementForScreenshot().thenApply((node) -> {
        ArrayList<DiagnosticsNode> ret = new ArrayList<>();
        if (node != null) {
          ret.add(node);
        }
        return ret;
      });
    } else {
      int line = data.descriptor.widget.getLine();
      int column = data.descriptor.widget.getColumn();
      // XXX is this the right range?
      final Document document = data.descriptor.widget.getDocument();
      final TextRange range = data.descriptor.widget.getGuideTextRange();
      if (range != null) {
        final int offset = range.getStartOffset();
        // FIXup handling of Foo.bar named constructors.
        final int documentEnd = data.document.getTextLength();
        final int constructorStart = range.getEndOffset() - 1;
        final int candidateEnd = min(documentEnd, constructorStart + 1000); // XX hack.
        final String text = data.document.getText(new TextRange(constructorStart, candidateEnd));
        for (int i = 0; i < text.length(); ++i) {
          char c = text.charAt(i);
          if (c == '.') {
            final int offsetKernel = constructorStart + i + 1;
            line = document.getLineNumber(offsetKernel);
            column = data.descriptor.widget.getColumnForOffset(offsetKernel);
            break;
          }
          if (c == '(') break;
        }
      }
      nodesFuture = group.getElementsAtLocation(file, line + 1, column + 1, 10);
    }
    group.safeWhenComplete(nodesFuture, (nextNodes, error) -> {
      if (error != null || isDisposed) {
        setNodes(null);
        activeIndex = 0;
        return;
      }
      InspectorObjectGroupManager manager = getGroups();
      if (isEquivalent(_nodes, nextNodes)) {
        onMaybeFetchScreenshot();
        manager.cancelNext();
        // Continue using the current.
      } else {
        // TODO(jacobr): simplify this logic.
        if (_nodes != null && nextNodes != null && _nodes.size() == nextNodes.size() && nextNodes.size() > 1) {
          boolean found = false;
          for (int i = 0; i < _nodes.size(); i++) {
            if (nextNodes.get(0).getValueRef().equals(_nodes.get(i).getValueRef())) {
              if (i <= activeIndex) {
                // Hacky.. fixup as we went backwards.
                activeIndex = Math.max(0, i - 1);
              } else {
                activeIndex = i;
              }
              found = true;
              break;
            }
          }
          if (!found) {
            activeIndex = 0;
          }
        } else {
          activeIndex = 0;
        }
        manager.promoteNext();
        setNodes(nextNodes);

        onActiveNodesChanged();
      }
    });
  }

  public void onMaybeFetchScreenshot()
  {

  }

  public boolean isNodesEmpty() {
    return _nodes == null || _nodes.isEmpty() || isDisposed;
  }

  public void setNodes(ArrayList<DiagnosticsNode> nodes) {
    _nodes = nodes;
  }

  public void onActiveNodesChanged() {
    if (isNodesEmpty()) return;
    final InspectorObjectGroupManager manager = getGroups();
    if (manager == null) return;

    if (isSelected) {
      manager.getCurrent().setSelection(getSelectedNode().getValueRef(), false, true);
    }
  }

  public DiagnosticsNode getSelectedNode() {
    if (isNodesEmpty()) return null;
    return _nodes.get(0);
  }
  private boolean isEquivalent(ArrayList<DiagnosticsNode> a, ArrayList<DiagnosticsNode> b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    if (a.size() != b.size()) return false;
    for (int i = 0; i < a.size(); i++) {
      if (!isEquivalent(a.get(i), b.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean isEquivalent(DiagnosticsNode a, DiagnosticsNode b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    if (!Objects.equals(a.getValueRef(), b.getValueRef())) return false;
    if (!a.identicalDisplay(b)) return false;
    return true;
  }

  public WidgetIndentGuideDescriptor getDescriptor() { return data.descriptor; }
}
