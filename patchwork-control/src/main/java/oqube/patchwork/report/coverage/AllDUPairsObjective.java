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

 Created 14 sept. 2006
 */
package oqube.patchwork.report.coverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.DataFlowGraph;
import oqube.patchwork.graph.DataFlowGraph.DefNode;
import oqube.patchwork.graph.DataFlowGraph.UseNode;
import salvo.jesus.graph.DirectedEdge;
import salvo.jesus.graph.DirectedGraph;


/**
 * This coverage object computes the DU pairs coverage for some control-graph.
 * The DU-pair coverage criterion is based on the data-flow graph structure
 * which is itself constructed from the control-flow graph structure. Each
 * definitieon (D) and usage (U) of a variable is identifed in the graph and we
 * construct DU pairs (of blocks) representing paths free of other D nodes for
 * the same variable between each pair of D and U nodes.
 * 
 * @author nono
 * 
 */
public class AllDUPairsObjective extends MethodObjective {

  /*
   * list of arrays of ints denoting path for blocks
   */
  private int[][] paths;

  private double coverage = 0;

  /**
   * Default contructor.
   * 
   */
  public AllDUPairsObjective() {

  }
  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#update(java.util.Map)
   */
  public void update(Map codepaths) {
    // iterate over
    List pth = (List) codepaths.get(getMethod());
    if (pth == null)
      return;
    BitSet bs = new BitSet(paths.length); // count number of paths found so far
    int[] idxs = new int[paths.length]; // parallel reading of paths
    /* all other */
    for (Iterator i = pth.iterator(); i.hasNext();) {
      Arrays.fill(idxs, 0);
      int[] pt = (int[]) i.next();
      boolean stop = false;
      for (int k = 0; !stop && k < pt.length; k++) {
        stop = true;
        for (int j = 0; j < idxs.length; j++) {
          assert idxs[j] <= paths[j].length;
          if (idxs[j] == paths[j].length) // found a path
            bs.set(j);
          else if (paths[j][idxs[j]] == pt[k]) { // may advance
            idxs[j]++;
            stop = false;
          }
        }
      }
    }
    coverage = bs.cardinality();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#reset()
   */
  public void reset() {
    coverage = 0;
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
   * @see oqube.patchwork.report.coverage.CoverageObjective#high()
   */
  public double high() {
    return paths.length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#visit(oqube.patchwork.report.coverage.ObjectiveVisitor)
   */
  public void visit(ObjectiveVisitor vis) {
    vis.visit(this);
  }

  /**
   * Construct DU pairs for given definition node. This method traverse all
   * edges of the underlying graph starting from given node registering
   * def-clear paths to equivalent use nodes.
   * 
   * @param def
   * @return
   */
  private List makeDUPairs(DefNode def, DirectedGraph dgraph) {
    /* contains list of paths of definition clear du pairs */
    List /* < List > */ret = new ArrayList();
    Object adjacent;
    DirectedEdge edge;
    Stack stack = new Stack();
    Set visited = new HashSet();

    // Push the starting vertex onto the stack
    for (Iterator it = dgraph.getOutgoingAdjacentVertices(def).iterator(); it
        .hasNext();) {
      stack.push(it.next());
    }

    do {
      /* get next edge and mark it */
      adjacent = stack.pop();
      if (visited.contains(adjacent))
        continue;
      else
        visited.add(adjacent);

      /* check this is a def clear path - if not, continue */
      if (adjacent instanceof DefNode) {
        if (((DefNode) adjacent).getIndex() == def.getIndex())
          continue;
      }
      /* is this a matching use node ? */
      else if (adjacent instanceof UseNode) {
        if (((UseNode) adjacent).getIndex() == def.getIndex()) {
          List l = new ArrayList(stack);
          l.add(adjacent);
          /* we found a du path, store it */
          ret.add(l);
        }
      }
      /* continue traversal */
      for (Iterator it = dgraph.getOutgoingAdjacentVertices(adjacent)
          .iterator(); it.hasNext();) {
        Object v = it.next();
        if (!visited.contains(v) && !stack.contains(v)) {
          stack.push(v);
        }
      }

    } while (!stack.isEmpty());
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setGraph(oqube.patchwork.graph.ControlGraph)
   */
  public void setGraph(ControlGraph graph) {
    super.setGraph(graph);
    // construct DU paths
    List ret = new ArrayList();
    /* compute data-flow graph */
    DataFlowGraph dfg;
    try {
      dfg = new DataFlowGraph(graph, 256);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    /*
     * construct suite we traverse the graph from each definition node to each
     * use node and register the pair iff there is no other definition for this
     * variable
     */
    /* first identify all defs */
    List defs = new ArrayList();
    for (Iterator i = dfg.getGraph().getAllVertices().iterator(); i.hasNext();) {
      Object v = i.next();
      if (v instanceof DataFlowGraph.DefNode) {
        defs.add(v);
      }
    }
    /* then do traversal from each def */
    for (Iterator i = defs.iterator(); i.hasNext();) {
      Object v = i.next();
      List l = makeDUPairs((DefNode) v, dfg.getGraph());
      for (Iterator j = l.iterator(); j.hasNext();) {
        List p = (List) j.next();
        int[] pi = new int[p.size()];
        int n = 0;
        int lastb = 0;
        for (Iterator k = p.iterator(); k.hasNext();) {
          int mb = ((DataFlowGraph.VarNode) k.next()).getBlock().getNumBlock();
          if (lastb == 0 || mb != lastb)
            pi[n++] = mb;
          lastb = mb;
        }

        ret.add(pi);
      }
    }
    this.paths = (int[][]) ret.toArray(new int[ret.size()][]);
  }

  public void dumpPairs() {
    for(int i=0;i<paths.length;i++) {
      System.err.println(Arrays.toString(paths[i]));
    }
  }

  /* (non-Javadoc)
   * @see oqube.patchwork.report.coverage.CoverageObjective#update(int, java.lang.String, int)
   */
  public void update(int tid, String method, int block) {
    // TODO Auto-generated method stub
    
  }
  public int hit() {
    // TODO Auto-generated method stub
    return 0;
  }

}
