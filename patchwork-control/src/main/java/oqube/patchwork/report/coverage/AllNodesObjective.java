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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.ControlGraph;

/**
 * The all-nodes objective for a single method. This class assesses the
 * <em>all-nodes</em> objective for a single method.
 * 
 * @author nono
 * 
 */
public class AllNodesObjective extends MethodObjective {
  public static final String display = "All-nodes";
  

  /*
   * number of nodes in graph (including start and end)
   */
  private int nodes;

  private int coverage;

  private BitSet covered = new BitSet(nodes);

  private int hit;

  /**
   * Default constructor. This objective should be initialized with
   * {@link #setMethod(String)} before this object can be used.
   * 
   */
  public AllNodesObjective() {
  }

  public void update(Map codepaths) {
    List paths = (List) codepaths.get(getMethod());
    if (paths == null) {
      return;
    }
    /* make sure paths cover all nodes in graph */
    double count = 0;
    BitSet bs = new BitSet(nodes);
    /* start and end nodes */
    if (paths.size() > 0) {
      count += 2;
      bs.set(0);
      bs.set(nodes - 1);
    } else
      return;
    /* all other */
    for (Iterator i = paths.iterator(); i.hasNext();) {
      int[] pt = (int[]) i.next();
      for (int j = 0; j < pt.length; j++)
        bs.set(pt[j]);
    }
    coverage = bs.cardinality();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#high()
   */
  public double high() {
    return nodes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setGraph(oqube.patchwork.graph.ControlGraph)
   */
  public void setGraph(ControlGraph graph) {
    super.setGraph(graph);
    /* compute number of nodes */
    this.nodes = getGraph().getGraph().getAllVertices().size();
    this.covered = new BitSet(nodes);
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
    // just set the covered bit
    covered.set(block);
    hit++;
    coverage = covered.cardinality();
  }

  public int hit() {
    return hit;
  }
}
