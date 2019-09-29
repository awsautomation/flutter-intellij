/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.TextRange;
import io.flutter.dart.FlutterDartAnalysisServer;

public class WidgetViewModelData extends WidgetViewModelDataBase {
  public final WidgetIndentGuideDescriptor descriptor;

  public WidgetViewModelData(WidgetIndentGuideDescriptor descriptor, WidgetViewModelDataBase context) {
    this(descriptor, context.document, context.data, context.flutterDartAnalysisService, context.editor, context._highlighter);
  }

  public WidgetViewModelData(WidgetIndentGuideDescriptor descriptor, Document document, WidgetIndentsPassData data, FlutterDartAnalysisServer flutterDartAnalysisService, EditorEx editor, RangeHighlighter highlighter) {
    super(highlighter, document, data, flutterDartAnalysisService, editor);
    this.descriptor = descriptor;
    if (descriptor != null) {
      descriptor.trackLocations(document);
    }
  }

  public TextRange getMarker() {
    if (descriptor != null) {
      return descriptor.getMarker();
    }
    return null;
  }
}
