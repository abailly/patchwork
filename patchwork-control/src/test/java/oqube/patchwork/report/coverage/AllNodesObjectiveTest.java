package oqube.patchwork.report.coverage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraph;

import junit.framework.TestCase;

public class AllNodesObjectiveTest extends TestCase {

  private AllNodesObjective objective;

  private static final String METHOD = "oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V";

  protected void setUp() throws Exception {
    super.setUp();
    this.objective = new AllNodesObjective();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "linesinfo.bytes");
    ControlGraph cg = new CachedControlGraphBuilder().createAllGraphs(is).get(
        METHOD);
    this.objective.setGraph(cg);
    this.objective.setMethod(METHOD);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test01CoverageOK() {
    Map m = new HashMap();
    List l = new ArrayList();
    int[] p1 = { 1, 2 };
    int[] p2 = { 1, 3, 4, 6, 10, 11, 12 };
    l.add(p1);
    l.add(p2);
    m.put(METHOD, l);
    this.objective.update(m);
    assertEquals(9, this.objective.coverage(), 0.00000001);
  }

  public void testOnlineCoverageOK() {
    this.objective.update(1, METHOD, 0);
    this.objective.update(2, METHOD, 1);
    this.objective.update(1, METHOD, 2);
    this.objective.update(2, METHOD, 3);
    this.objective.update(2, METHOD, 4);
    this.objective.update(2, METHOD, 6);
    this.objective.update(2, METHOD, 10);
    this.objective.update(2, METHOD, 11);
    this.objective.update(1, METHOD, 12);
    this.objective.update(2, METHOD, 12);
    assertEquals(9, this.objective.coverage(), 0.00000001);
    assertEquals(10, this.objective.hit());
  }
}
