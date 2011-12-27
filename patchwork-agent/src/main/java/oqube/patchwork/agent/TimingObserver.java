/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

public interface TimingObserver {

  void methodEnd(String className, String methodName);
  
  void methodStart(String className, String methodName);

}
