package oqube.patchwork.report.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

public class MethodObjectiveTest extends TestCase {

  public void testAllEdgesDisplayName() {
    Locale.setDefault(Locale.ENGLISH);
    List<Class> cls = new ArrayList<Class>();
    cls.add(AllEdgesObjective.class);
    String s = MethodObjective.getDisplayName(cls);
    assertEquals("Bad display english name", "All-edges", s);
  }

  public void testAllNodesDisplayName() {
    Locale.setDefault(Locale.ENGLISH);
    List<Class> cls = new ArrayList<Class>();
    cls.add(AllNodesObjective.class);
    String s = MethodObjective.getDisplayName(cls);
    assertEquals("Bad display english name", "All-nodes", s);
  }
  
  public void testCompoundDisplayName() {
    Locale.setDefault(Locale.ENGLISH);
    List<Class> cls = new ArrayList<Class>();
    cls.add(AllEdgesObjective.class);
    cls.add(AllNodesObjective.class);
    String s = MethodObjective.getDisplayName(cls);
    assertEquals("Bad display english name", "All-edges,All-nodes", s);
  }
  
}
