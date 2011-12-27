/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.gui;

public interface DependencyComputationProgressListener {

  /**
   * Callback method from dependency graph to notify analysis progress.
   * 
   * @param countAnalyzedDependencies
   * @param totalNumberToAnalyze
   */
  void progress(int countAnalyzedDependencies, int totalNumberToAnalyze);

}
