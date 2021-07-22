/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.run.coverage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.rt.coverage.instrumentation.SourceLineCounter;
import org.jetbrains.coverage.gnu.trove.TIntObjectHashMap;
import org.jetbrains.coverage.org.objectweb.asm.ClassReader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// com.intellij.coverage.SourceLineCounterUtil
public class SourceLineCounterUtil {
  public static boolean collectNonCoveredClassInfo(final PackageAnnotator.ClassCoverageInfo classCoverageInfo,
                                                   final PackageAnnotator.PackageCoverageInfo packageCoverageInfo, byte[] content,
                                                   final boolean excludeLines,
                                                   final Condition<? super String> includeDescriptionCondition) {
    if (content == null) return false;
    final ClassReader reader = new ClassReader(content, 0, content.length);

    final SourceLineCounter counter = new SourceLineCounter(null, excludeLines, null);
    reader.accept(counter, 0);
    final Set<Object> descriptions = new HashSet<>();
    final TIntObjectHashMap<String> lines = counter.getSourceLines();
    lines.forEachEntry((line, description) -> {
      if (includeDescriptionCondition.value(description)) {
        classCoverageInfo.totalLineCount++;
        packageCoverageInfo.totalLineCount++;
        descriptions.add(description);
      }
      return true;
    });

    classCoverageInfo.totalMethodCount += descriptions.size();
    packageCoverageInfo.totalMethodCount += descriptions.size();

    if (!counter.isInterface()) {
      packageCoverageInfo.totalClassCount++;
    }

    packageCoverageInfo.totalBranchCount += counter.getTotalBranches();
    classCoverageInfo.totalBranchCount += counter.getTotalBranches();

    return !counter.isInterface();
  }

  public static void collectSrcLinesForUntouchedFiles(final List<? super Integer> uncoveredLines,
                                                      byte[] content,
                                                      final boolean excludeLines,
                                                      final Project project) {
    final ClassReader reader = new ClassReader(content);
    final SourceLineCounter collector = new SourceLineCounter(null, excludeLines, null);
    reader.accept(collector, 0);

    final String qualifiedName = reader.getClassName();
    //Condition<String> includeDescriptionCondition = description -> !JavaCoverageOptionsProvider.getInstance(project).isGeneratedConstructor(qualifiedName, description);
    final TIntObjectHashMap<String> lines = collector.getSourceLines();
    lines.forEachEntry((line, description) -> {
      //if (includeDescriptionCondition.value((String)description)) {
        line--;
        uncoveredLines.add(line);
      //}
      return true;
    });
  }
}
