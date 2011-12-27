package oqube.patchwork.report.coverage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;

import junit.framework.TestCase;

public class AggregateObjectiveTest extends TestCase {

  private AggregateObjective aggregate;

  protected void setUp() throws Exception {
    super.setUp();
    this.aggregate = new AggregateObjective();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "linesinfo.bytes");
    Map<String, ControlGraph> graphs = new CachedControlGraphBuilder()
        .createAllGraphs(is);
    /* add two wethods from AllNodesObjective */
    AllNodesObjective obj = new AllNodesObjective();
    obj
        .setGraph(graphs
            .get("oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V"));
    obj
        .setMethod("oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V");
    aggregate.addObjective(obj);
    obj = new AllNodesObjective();
    obj.setGraph(graphs
        .get("oqube/patchwork/report/coverage/AllNodesObjective.high()D"));
    obj.setMethod("oqube/patchwork/report/coverage/AllNodesObjective.high()D");
    aggregate.addObjective(obj);
    obj = new AllNodesObjective();
    obj.setGraph(graphs
        .get("oqube/patchwork/report/coverage/AllNodesObjective.reset()V"));
    obj.setMethod("oqube/patchwork/report/coverage/AllNodesObjective.reset()V");
    aggregate.addObjective(obj);
  }

  public void test01High() throws IOException {
    assertEquals(19, aggregate.high(), 0);
  }

  public void test02Low() throws IOException {
    assertEquals(9.5, aggregate.low(), 0);
  }

  public void test03Coverage() throws IOException {
    Map m = new HashMap();
    List l = new ArrayList();
    int[] p1 = { 1, 2 };
    int[] p2 = { 1, 3, 4, 6, 10, 11 };
    l.add(p1);
    l.add(p2);
    m
        .put(
            "oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V",
            l);
    p1 = new int[] { 1 };
    m.put("oqube/patchwork/report/coverage/AllNodesObjective.high()D", l);
    this.aggregate.update(m);
    assertEquals(17, aggregate.coverage(), 0);
  }

}
