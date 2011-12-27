/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import oqube.bytes.loading.InstrumentingClassLoader;

public class TimingClassLoader extends InstrumentingClassLoader<TimingClassFactory> {

  public TimingClassLoader(ClassLoader contextClassLoader, TimingClassFactory timingClassFactory) {
    super(contextClassLoader);
    this.setFactory(timingClassFactory);
  }

}
