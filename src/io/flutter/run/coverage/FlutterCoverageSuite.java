/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.run.coverage;

import com.intellij.coverage.BaseCoverageSuite;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlutterCoverageSuite extends BaseCoverageSuite {

  @NotNull final private FlutterCoverageEngine coverageEngine;

  public FlutterCoverageSuite(@NotNull FlutterCoverageEngine coverageEngine) {
    this.coverageEngine = coverageEngine;
  }

  public FlutterCoverageSuite(CoverageRunner runner,
                              String name,
                              CoverageFileProvider coverageDataFileProvider,
                              Project project,
                              @NotNull FlutterCoverageEngine coverageEngine
  ) {
    super(name, coverageDataFileProvider, System.currentTimeMillis(), false, false,
          false, runner, project);
    this.coverageEngine = coverageEngine;
  }

  @Override
  public @NotNull CoverageEngine getCoverageEngine() {
    return coverageEngine;
  }

  @Override
  public void deleteCachedCoverageData() {
  }

  public List<? extends PsiClass> getCurrentSuiteClasses(Project project) {
    final List<PsiClass> classes = new ArrayList<>();
    final String[] classNames = getFilteredClassNames();
    if (classNames.length > 0) {
      final DumbService dumbService = DumbService.getInstance(project);
      for (final String className : classNames) {
        final ThrowableComputable<PsiClass, RuntimeException> computable = () -> {
          GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
          final RunConfigurationBase<?> configuration = getConfiguration();
          if (configuration instanceof ModuleBasedConfiguration) {
            final Module module = ((ModuleBasedConfiguration<?,?>)configuration).getConfigurationModule().getModule();
            if (module != null) {
              searchScope = GlobalSearchScope.moduleRuntimeScope(module, isTrackTestFolders());
            }
          }
          return JavaPsiFacade.getInstance(project).findClass(className.replace("$", "."), searchScope);
        };
        final PsiClass aClass = ReadAction.compute(() -> dumbService.computeWithAlternativeResolveEnabled(computable));
        if (aClass != null) {
          classes.add(aClass);
        }
      }
    }

    return classes;
  }

  public Collection<? extends PsiPackage> getCurrentSuitePackages(Project project) {
    return ReadAction.compute(() -> {
      final List<PsiPackage> packages = new ArrayList<>();
      final PsiManager psiManager = PsiManager.getInstance(project);
      final String[] filters = getFilteredPackageNames();
      if (filters.length == 0) {
        if (getFilteredClassNames().length > 0) return Collections.emptyList();

        final PsiPackage defaultPackage = JavaPsiFacade.getInstance(psiManager.getProject()).findPackage("");
        if (defaultPackage != null) {
          packages.add(defaultPackage);
        }
      }
      else {
        final List<String> nonInherited = new ArrayList<>();
        for (final String filter : filters) {
          if (!isSubPackage(filters, filter)) {
            nonInherited.add(filter);
          }
        }

        for (String filter : nonInherited) {
          final PsiPackage psiPackage = JavaPsiFacade.getInstance(psiManager.getProject()).findPackage(filter);
          if (psiPackage != null) {
            packages.add(psiPackage);
          }
        }
      }
      return packages;
    });
  }

  public String[] getFilteredClassNames() {
    return getClassNames(null/*myFilters*/);
  }

  public String[] getFilteredPackageNames() {
    return getPackageNames(null/*myFilters*/);
  }

  public boolean isPackageFiltered(final String packageFQName) {
    return true;
  }

  private static String[] getClassNames(final String[] filters) {
    if (filters == null) return ArrayUtilRt.EMPTY_STRING_ARRAY;
    final List<String> result = new ArrayList<>();
    for (String filter : filters) {
      if (!filter.equals("*") && !filter.endsWith(".*")) result.add(filter);
    }
    return ArrayUtilRt.toStringArray(result);
  }

  private static String[] getPackageNames(String[] filters) {
    if (filters == null || filters.length == 0) return ArrayUtilRt.EMPTY_STRING_ARRAY;
    final List<String> result = new ArrayList<>();
    for (String filter : filters) {
      if (filter.equals("*")) {
        result.add(""); //default package
      }
      else if (filter.endsWith(".*")) result.add(filter.substring(0, filter.length() - 2));
    }
    return ArrayUtilRt.toStringArray(result);
  }

  private static boolean isSubPackage(String[] filters, String filter) {
    for (String supPackageFilter : filters) {
      if (filter.startsWith(supPackageFilter + ".")) {
        return true;
      }
    }
    return false;
  }
}
