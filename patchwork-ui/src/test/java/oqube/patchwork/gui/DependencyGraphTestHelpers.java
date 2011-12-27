/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.gui;

import java.util.Collection;

import junit.framework.Assert;
import salvo.jesus.graph.Edge;

public abstract class DependencyGraphTestHelpers {

  public static void assertNoEdgeFromTo(final Collection<Edge> edges, String from, String to) {
    for(Edge edge : edges) {
      if(edge.getVertexA().equals(new ClassInfo(from)) && edge.getVertexB().equals(new ClassInfo(to)))
        Assert.fail("should not contain link from " + from + " to " + to);
    }
  }

  public static void assertEdgeFromTo(Collection<Edge> edges, String from, String to) {
    for(Edge edge : edges) {
      if(edge.getVertexA().equals(new ClassInfo(from)) && edge.getVertexB().equals(new ClassInfo(to)))
        return;
    }
    Assert.fail("should contain a link from " + from + " to " + to);
  }
}