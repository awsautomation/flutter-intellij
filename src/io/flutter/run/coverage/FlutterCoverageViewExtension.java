/*
 * Copyright 2021 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.run.coverage;

import com.intellij.CommonBundle;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.view.*;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ui.ColumnInfo;
import io.flutter.FlutterBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlutterCoverageViewExtension extends CoverageViewExtension {

  private final FlutterCoverageAnnotator myAnnotator;

  public FlutterCoverageViewExtension(FlutterCoverageAnnotator annotator,
                                      @NotNull Project project,
                                      CoverageSuitesBundle suitesBundle,
                                      CoverageViewManager.StateBean stateBean) {
    super(project, suitesBundle, stateBean);
    this.myAnnotator = annotator;
  }

  @Override
  public @Nullable
  @Nls
  String getSummaryForNode(@NotNull AbstractTreeNode<?> node) {
    if (!myCoverageViewManager.isReady()) return CommonBundle.getLoadingTreeNodeText();
    if (myCoverageDataManager.isSubCoverageActive()) {
      return showSubCoverageNotification();
    }
    final PsiPackage aPackage = (PsiPackage)node.getValue();
    final String coverageInformationString = myAnnotator
      .getPackageCoverageInformationString(aPackage, null, myCoverageDataManager, myStateBean.myFlattenPackages);
    return FlutterBundle.message("coverage.view.node.summary", getNotCoveredMessage(coverageInformationString),
                                 aPackage != null ? aPackage.getQualifiedName() : node.getName());
  }

  private static @Nls
  String showSubCoverageNotification() {
    return FlutterBundle.message("sub.coverage.notification");
  }

  @Override
  @Nullable
  public String getSummaryForRootNode(@NotNull AbstractTreeNode<?> node) {
    if (myCoverageDataManager.isSubCoverageActive()) {
      return showSubCoverageNotification();
    }
    final Object value = node.getValue();
    String coverageInformationString = myAnnotator.getPackageCoverageInformationString((PsiPackage)value, null,
                                                                                       myCoverageDataManager);
    if (coverageInformationString == null) {
      if (!myCoverageViewManager.isReady()) return CommonBundle.getLoadingTreeNodeText();
      PackageAnnotator.SummaryCoverageInfo info = new PackageAnnotator.PackageCoverageInfo();
      final Collection<? extends AbstractTreeNode<?>> children = node.getChildren();
      for (Object child : children) {
        final Object childValue = ((CoverageListNode)child).getValue();
        final PackageAnnotator.SummaryCoverageInfo childInfo = getSummaryCoverageForNodeValue(childValue);
        info = FlutterCoverageAnnotator.merge(info, childInfo);
      }
      coverageInformationString = FlutterCoverageAnnotator.getCoverageInformationString(info, false);
    }
    return FlutterBundle.message("coverage.view.root.node.summary", getNotCoveredMessage(coverageInformationString));
  }

  private static String getNotCoveredMessage(String coverageInformationString) {
    if (coverageInformationString == null) {
      coverageInformationString = FlutterBundle.message("coverage.view.no.coverage");
    }
    return coverageInformationString;
  }

  @Override
  public @Nullable
  String getPercentage(int columnIndex, @NotNull AbstractTreeNode<?> node) {
    final Object value = node.getValue();
    final PackageAnnotator.SummaryCoverageInfo info = getSummaryCoverageForNodeValue(value);

    if (columnIndex == 1) {
      return myAnnotator.getClassCoveredPercentage(info);
    }
    if (columnIndex == 2) {
      return myAnnotator.getMethodCoveredPercentage(info);
    }
    if (columnIndex == 3) {
      return myAnnotator.getLineCoveredPercentage(info);
    }
    return "";
  }

  @Override
  public List<AbstractTreeNode<?>> getChildrenNodes(AbstractTreeNode<?> node) {
    final List<AbstractTreeNode<?>> children = new ArrayList<>();
    if (node instanceof CoverageListNode) {
      final Object val = node.getValue();
      if (val instanceof PsiClass) return Collections.emptyList();

      //append package classes
      if (val instanceof PsiPackage) {
        final PsiPackage psiPackage = (PsiPackage)val;
        if (ReadAction.compute(() -> isInCoverageScope(psiPackage))) {
          final PsiPackage[] subPackages = ReadAction.compute(() -> psiPackage.isValid()
                                                                    ? psiPackage
                                                                      .getSubPackages(mySuitesBundle.getSearchScope(node.getProject()))
                                                                    : PsiPackage.EMPTY_ARRAY);
          for (PsiPackage subPackage : subPackages) {
            processSubPackage(subPackage, children);
          }

          final PsiFile[] childFiles = ReadAction.compute(() -> psiPackage.isValid()
                                                                ? psiPackage.getFiles(mySuitesBundle.getSearchScope(node.getProject()))
                                                                : PsiFile.EMPTY_ARRAY);
          for (final PsiFile file : childFiles) {
            collectFileChildren(file, node, children);
          }
        }
        else if (!myStateBean.myFlattenPackages) {
          collectSubPackages(children, (PsiPackage)val);
        }
      }
      if (node instanceof CoverageListRootNode) {
        for (CoverageSuite suite : mySuitesBundle.getSuites()) {
          final List<? extends PsiClass> classes = ((FlutterCoverageSuite)suite).getCurrentSuiteClasses(myProject);
          for (PsiClass aClass : classes) {
            children.add(new CoverageListNode(myProject, aClass, mySuitesBundle, myStateBean));
          }
        }
      }
      for (AbstractTreeNode childNode : children) {
        childNode.setParent(node);
      }
    }
    return children;
  }

  private boolean isInCoverageScope(PsiElement element) {
    if (element instanceof PsiPackage) {
      final PsiPackage psiPackage = (PsiPackage)element;
      final String qualifiedName = psiPackage.getQualifiedName();
      for (CoverageSuite suite : mySuitesBundle.getSuites()) {
        if (((FlutterCoverageSuite)suite).isPackageFiltered(qualifiedName)) return true;
      }
    }
    return false;
  }

  private void collectSubPackages(List<AbstractTreeNode<?>> children, final PsiPackage rootPackage) {
    final GlobalSearchScope searchScope = mySuitesBundle.getSearchScope(rootPackage.getProject());
    final PsiPackage[] subPackages = ReadAction.compute(() -> rootPackage.getSubPackages(searchScope));
    for (final PsiPackage aPackage : subPackages) {
      processSubPackage(aPackage, children);
    }
  }

  private void processSubPackage(final PsiPackage aPackage, List<AbstractTreeNode<?>> children) {
    if (ReadAction.compute(() -> isInCoverageScope(aPackage))) {
      final CoverageListNode node = new CoverageListNode(aPackage.getProject(), aPackage, mySuitesBundle, myStateBean);
      children.add(node);
    }
    else if (!myStateBean.myFlattenPackages) {
      collectSubPackages(children, aPackage);
    }
    if (myStateBean.myFlattenPackages) {
      collectSubPackages(children, aPackage);
    }
  }

  @Override
  public ColumnInfo[] createColumnInfos() {
    final ArrayList<ColumnInfo> infos = new ArrayList<>();
    infos.add(new ElementColumnInfo());
    infos.add(new PercentageCoverageColumnInfo(1, FlutterBundle.message("coverage.view.column.class"), mySuitesBundle, myStateBean));
    infos.add(new PercentageCoverageColumnInfo(2, FlutterBundle.message("coverage.view.column.method"), mySuitesBundle, myStateBean));
    infos.add(new PercentageCoverageColumnInfo(3, FlutterBundle.message("coverage.view.column.line"), mySuitesBundle, myStateBean));
    // TODO Add functions.
    return infos.toArray(ColumnInfo.EMPTY_ARRAY);
  }

  @Override
  public @Nullable
  PsiElement getParentElement(PsiElement element) {
    if (element instanceof PsiClass) {
      final PsiDirectory containingDirectory = element.getContainingFile().getContainingDirectory();
      // TODO Get Dart package.
      return containingDirectory != null ? JavaDirectoryService.getInstance().getPackage(containingDirectory) : null;
    }
    return ((PsiPackage)element).getParentPackage();
  }

  protected void collectFileChildren(final PsiFile file, AbstractTreeNode<?> node, List<? super AbstractTreeNode<?>> children) {
    if (file instanceof PsiClassOwner) {
      final PsiClass[] classes = ReadAction.compute(() -> file.isValid() ? ((PsiClassOwner)file).getClasses() : PsiClass.EMPTY_ARRAY);
      for (PsiClass aClass : classes) {
        if (!(node instanceof CoverageListRootNode) && getClassCoverageInfo(aClass) == null) {
          continue;
        }
        children.add(new CoverageListNode(myProject, aClass, mySuitesBundle, myStateBean));
      }
    }
  }

  @Nullable
  private PackageAnnotator.ClassCoverageInfo getClassCoverageInfo(final PsiClass aClass) {
    return myAnnotator.getClassCoverageInfo(ReadAction.compute(() -> aClass.isValid() ? aClass.getQualifiedName() : null));
  }

  @Override
  public @NotNull
  AbstractTreeNode<?> createRootNode() {
    // TODO Get root DartComponentName.
    return new CoverageListRootNode(myProject, JavaPsiFacade.getInstance(myProject).findPackage(""), mySuitesBundle, myStateBean);
  }

  public PackageAnnotator.SummaryCoverageInfo getSummaryCoverageForNodeValue(Object value) {
    if (value instanceof PsiClass) {
      final String qualifiedName = ((PsiClass)value).getQualifiedName();
      return myAnnotator.getClassCoverageInfo(qualifiedName);
    }
    if (value instanceof PsiPackage) {
      return myAnnotator.getPackageCoverageInfo((PsiPackage)value, myStateBean.myFlattenPackages);
    }
    return null;
  }
}
