/**
 * Copyright 2010 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.io.FileNotFoundException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class Agent {

  public static void premain(String agentArgs, Instrumentation inst) {
    String filter = agentArgs;
    CountTransformer transformer;
    try {
      transformer = new CountTransformer(filter);
      inst.addTransformer(transformer);
      Runtime.getRuntime().addShutdownHook(transformer.getFinalizer());
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
}
