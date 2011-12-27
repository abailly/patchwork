/**
 *  Copyright (C) 2006 - OQube / Arnaud Bailly
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 Created 11 sept. 2006
 */
package oqube.patchwork.report.coverage;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;
import salvo.jesus.graph.DirectedEdge;

/**
 * The all-edges objective for a single method. This class assesses the
 * <em>all-edges</em> objective for a single method.
 * 
 * @author nono
 * 
 */
public class AllEdgesObjective extends MethodObjective {
  public static final String display = "All-edges";
  
  /*
   * number of edges in graph (including start and end)
   */
  int edges;

  /*
   * adjacency matrix encoded with bitsets. immutable after setting
   */
  private BitSet[] matrix;

  private int nodes;

  private double coverage;

  /*
   * store last block for given thread id
   */
  private Map<Integer, Integer> threadMap = new HashMap<Integer, Integer>();

  /*
   * current transition covered.
   */
  private BitSet[] transitions;

  private int hit;

  /**
   * Default constructor. This objective should be initialized with
   * {@link #setMethod(String)} before this object can be used.
   * 
   */
  public AllEdgesObjective() {
  }

  /*
   * Returns the number of nodes covered.
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#coverage(java.util.Map)
   */
  public void update(Map codepaths) {
    List paths = (List) codepaths.get(getMethod());
    if (paths == null)
      return;
    double count = 0;
    BitSet[] mat = cloneTransitions();
    /* all other */
    for (Iterator i = paths.iterator(); i.hasNext();) {
      int[] pt = (int[]) i.next();
      int n = 0;
      for (int j = 0; j < pt.length; j++) {
        // check there is an edge
        int node = pt[j];
        assert node >= 0 && n < mat.length;
        if (mat[n].get(node)) {
          mat[n].clear(node);
          count++;
        }
        n = pt[j];
      }
      // check last
      if (mat[n].get(nodes - 1)) {
        mat[n].clear(nodes - 1);
        count++;
      }
    }
    coverage = count;
  }

  /**
   * @return
   */
  private BitSet[] cloneTransitions() {
    // clone matrix
    int ln = matrix.length;
    BitSet[] mat = new BitSet[ln];
    for (int i = 0; i < ln; i++)
      mat[i] = (BitSet) matrix[i].clone();
    return mat;
  }

  /*
   * coverage(m (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#high()
   */
  public double high() {
    return edges;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setGraph(oqube.patchwork.graph.ControlGraph)
   */
  public void setGraph(ControlGraph graph) {
    super.setGraph(graph);
    /* compute number of edges */
    this.edges = getGraph().getGraph().getAllEdges().size();
    /* compute adjacency matrix of graph */
    int nodes = getGraph().getGraph().getAllVertices().size();
    this.matrix = new BitSet[nodes];
    for (int i = 0; i < nodes; i++)
      this.matrix[i] = new BitSet(nodes);
    for (Iterator i = getGraph().getGraph().getAllEdges().iterator(); i
        .hasNext();) {
      DirectedEdge de = (DirectedEdge) i.next();
      BasicBlock f = (BasicBlock) de.getVertexA();
      BasicBlock t = (BasicBlock) de.getVertexB();
      this.matrix[f.getNumBlock()].set(t.getNumBlock());
    }
    // reset
    this.nodes = nodes;
    this.transitions = cloneTransitions();
    this.coverage = 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#visit(oqube.patchwork.report.coverage.ObjectiveVisitor)
   */
  public void visit(ObjectiveVisitor vis) {
    vis.visit(this);
  }

  public void reset() {
    this.transitions = cloneTransitions();
    this.coverage = 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#coverage()
   */
  public double coverage() {
    return coverage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#update(int,
   *      java.lang.String, int)
   */
  public void update(int tid, String method, int block) {
    if (!method.equals(getMethod()))
      return;
    hit++;
    // get last block for tid
    Integer lb = threadMap.get(tid);
    if (lb == null) {
      lb = 0;
    }
    if (transitions[lb].get(block)) {
      transitions[lb].clear(block);
      coverage++;
    }
    threadMap.put(tid, block);
    return;
  }

  public int hit() {
    return hit;
  }

}
