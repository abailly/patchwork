/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent.test;

public class OtherTestClass {

  public int doSomething() {
    return 14;
  }

  public int doOtherwise() {
    try {
      System.err.println("12");
      if(true)
        throw new Exception("error");
      return 51;
    } catch(Exception e) {
      throw new RuntimeException("message");
    } finally {
      System.err.println("14");
    }
  }

  public int doSomethingElse() {
    long start = System.nanoTime();
    try {
      System.err.println("12");
      return 12;
    } finally {
      long duration = System.nanoTime() - start;
    }
  }

}
