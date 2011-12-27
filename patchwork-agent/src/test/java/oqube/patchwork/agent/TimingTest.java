/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import oqube.bytes.loading.IncludeExclude;

import org.junit.Assert;
import org.junit.Test;

public class TimingTest {

  List<String>   startTimedMethod = new ArrayList<String>();
  List<String>   endTimedMethod   = new ArrayList<String>();

  long           durationCalled   = 0;

  TimingObserver notification     = new TimingObserver() {

                                    public void methodEnd(String className, String methodName) {
                                      startTimedMethod.add(methodName);
                                    }

                                    public void methodStart(String className, String methodName) {
                                      endTimedMethod.add(methodName);
                                    }
                                  };

  @Test
  public void canNotifyStartStopInformationForAMethod() throws Exception {
    TimingClassLoader loader = new TimingClassLoader(Thread.currentThread().getContextClassLoader(), new TimingClassFactory(notification,
        new IncludeExclude(".*Something")));
    loader.setInclude(Pattern.compile("oqube.patchwork.agent.test.TestClass"));
    Class<?> loadClass = loader.loadClass("oqube.patchwork.agent.test.TestClass");
    Object o = loadClass.newInstance();
    Method m = loadClass.getMethod("doSomething", new Class[0]);
    Assert.assertEquals(new Integer(12), (Integer)m.invoke(o, new Object[0]));
    Assert.assertEquals("doSomething", startTimedMethod.get(0));
    Method m1 = loadClass.getMethod("complexSomething", new Class[0]);
    try {
      m1.invoke(o, new Object[0]);
      Assert.fail("should have thrown exception");
    } catch(Exception e) {}
    Assert.assertEquals("complexSomething", startTimedMethod.get(1));
    Assert.assertEquals("complexSomething", endTimedMethod.get(1));
  }

  @Test
  public void excludesNonInstrumentedMethod() throws Exception {
    TimingClassLoader loader = new TimingClassLoader(Thread.currentThread().getContextClassLoader(), new TimingClassFactory(notification,
        new IncludeExclude(".*omething.*",".*Otherwise.*")));
    loader.setInclude(Pattern.compile("oqube.patchwork.agent.test.OtherTestClass"));
    Class<?> loadClass = loader.loadClass("oqube.patchwork.agent.test.OtherTestClass");
    Object o = loadClass.newInstance();
    Method m = loadClass.getMethod("doSomething", new Class[0]);
    Assert.assertEquals(new Integer(14), (Integer)m.invoke(o, new Object[0]));
    Assert.assertEquals("doSomething", startTimedMethod.get(0));
    Method m1 = loadClass.getMethod("doOtherwise", new Class[0]);
    try {
      m1.invoke(o, new Object[0]);
      Assert.fail("should have thrown exception");
    } catch(Exception e) {}
    Assert.assertEquals(1, startTimedMethod.size());
  }
}
