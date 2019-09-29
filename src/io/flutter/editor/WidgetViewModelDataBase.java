/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import io.flutter.dart.FlutterDartAnalysisServer;
import io.flutter.run.daemon.FlutterApp;

public class WidgetViewModelDataBase {
  public final RangeHighlighter _highlighter;
  public final Document document;
  public final WidgetIndentsPassData data; // XXX for visilbe rect only.
  public final FlutterDartAnalysisServer flutterDartAnalysisService;
  public final EditorEx editor;

  public WidgetViewModelDataBase(
    RangeHighlighter highlighter,
    Document document,
    WidgetIndentsPassData data,
    FlutterDartAnalysisServer flutterDartAnalysisService,
    EditorEx editor) {
    this._highlighter = highlighter;
    this.document = document;
    this.data = data;
    this.flutterDartAnalysisService = flutterDartAnalysisService;
    this.editor = editor;
  }

  public FlutterApp getApp() {
    if (data == null || data.inspectorService == null) return null;
    return data.inspectorService.getApp();
  }
}
