package oqube.patchwork.report.coverage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraph;

import junit.framework.TestCase;

public class AllEdgesObjectiveTest extends TestCase {

  private static final String METHOD = "oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V";

  private AllEdgesObjective objective;

  protected void setUp() throws Exception {
    super.setUp();
    this.objective = new AllEdgesObjective();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "linesinfo.bytes");
    ControlGraph cg = new CachedControlGraphBuilder().createAllGraphs(is).get(
        METHOD);
    this.objective.setGraph(cg);
    this.objective.setMethod(METHOD);
  }

  public void test01High() {
    assertEquals(16, this.objective.high(), 0.00000001);
  }

  public void test02CoverageOK() {
    Map m = new HashMap();
    List l = new ArrayList();
    int[] p1 = { 1, 2 };
    int[] p2 = { 1, 3, 4, 6, 10, 11 };
    int[] p3 = { 1, 3, 4, 6, 10, 7, 9, 10, 7, 9, 10, 11 };
    l.add(p1);
    l.add(p2);
    l.add(p3);
    m.put(METHOD, l);
    this.objective.update(m);
    assertEquals(12, this.objective.coverage(), 0.00000001);
  }

  public void testOnlineCoverageOK() {
    // generate same sequence as test02
    this.objective.update(1, METHOD, 1);
    this.objective.update(2, METHOD, 1);
    this.objective.update(1, METHOD, 2);
    this.objective.update(2, METHOD, 3);
    this.objective.update(2, METHOD, 4);
    this.objective.update(3, METHOD, 1);
    this.objective.update(3, METHOD, 3);
    this.objective.update(3, METHOD, 4);
    this.objective.update(2, METHOD, 6);
    this.objective.update(2, METHOD, 10);
    this.objective.update(3, METHOD, 6);
    this.objective.update(3, METHOD, 10);
    this.objective.update(3, METHOD, 7);
    this.objective.update(2, METHOD, 11);
    this.objective.update(3, METHOD, 9);
    this.objective.update(3, METHOD, 10);
    this.objective.update(3, METHOD, 7);
    this.objective.update(3, METHOD, 9);
    this.objective.update(3, METHOD, 10);
    this.objective.update(3, METHOD, 11);
    // dont forget end of method
    this.objective.update(3, METHOD, 12);
    this.objective.update(1, METHOD, 12);
    this.objective.update(2, METHOD, 12);
    assertEquals(12, this.objective.coverage(), 0.00000001);
  }
}
