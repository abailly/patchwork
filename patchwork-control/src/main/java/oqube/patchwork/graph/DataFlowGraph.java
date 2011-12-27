/*
 * Created on May 12, 2004
 * 
 * $Log: DataFlowGraph.java,v $
 * Revision 1.3  2004/10/15 13:32:47  bailly
 * added names in instructions such as getfield, cast, invoke,...
 * corrected several bugs in control graph display (handling of jsr)
 * May break some dependencies due to API modification of Instruction
 *
 * Revision 1.2  2004/08/30 21:06:07  bailly
 * cleaned imports
 *
 * Revision 1.1  2004/05/18 12:57:39  bailly
 * correction graphe de controle
 * ajout graphe de flot de donnees
 *
 */
package oqube.patchwork.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oqube.bytes.Opcodes;
import oqube.bytes.attributes.LocalVariableTableAttribute;
import oqube.bytes.attributes.VariableInfo;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.OneArgInst;
import salvo.jesus.graph.DirectedEdge;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;

/**
 * A class that encapsulates the data-flow graph of a method body.
 * <p>
 * The data-flow graph is constructed from the control flow graph, hence the one
 * argument constructor and make a graph where each node is either a variable
 * definition or a variable usage. Edges between nodes show the relationship
 * between variable definitions and usages.
 * 
 * @author nono
 * @version $Id: DataFlowGraph.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class DataFlowGraph implements Opcodes {

  /**
   * Defines a node that represents usage of a variable.
   * The node encapsulates a reference to its enclosing block.
   * 
   */
  public static class VarNode {

    private BasicBlock block;

    private int        index;

    private String     name;

    VarNode(BasicBlock bb, int i, String n) {
      this.block = bb;
      this.index = i;
      this.name = n;
    }

    public BasicBlock getBlock() {
      return block;
    }

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return getName();
    }
  }

  public static abstract class UseNode extends VarNode {

    UseNode(BasicBlock bb, int i, String n) {
      super(bb, i, n);
    }
  }

  /**
   * A class representing computational-use of a variable.
   * 
   * @author nono
   * @version $Id: DataFlowGraph.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class CUseNode extends UseNode {

    CUseNode(BasicBlock bb, int i, String n) {
      super(bb, i, n);
    }

    public String toString() {
      return "c-use " + getName();
    }

  }

  /**
   * A class represneting predicate-use of a variable.
   * 
   * @author nono
   * @version $Id: DataFlowGraph.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class PUseNode extends UseNode {

    PUseNode(BasicBlock bb, int i, String n) {
      super(bb, i, n);
    }

    public String toString() {
      return "p-use " + getName();
    }

  }

  /**
   * A class representing definition of variable.
   * 
   * @author nono
   * @version $Id: DataFlowGraph.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class DefNode extends VarNode {

    DefNode(BasicBlock bb, int i, String n) {
      super(bb, i, n);
    }

    public String toString() {
      return "def " + getName();
    }

  }

  /*
   * the data-flow graph - nodes are Instructions objects, edges are empty
   */
  private DirectedGraph graph;

  /* map used in the process of construction the graph */
  private Map          /* < Instruction, Vertex > */
                        vmap;

  private int           nvars;

  private Object        start;

  /**
   * Construct a data-flow graph from a control graph.
   * 
   * @param graph
   * @param number
   *            of variables
   */
  public DataFlowGraph(ControlGraph cgraph, int nvars) throws Exception {
    this.graph = new DirectedGraphImpl();
    this.vmap = new HashMap();
    this.nvars = nvars;
    makeGraph(cgraph);
  }

  /**
   * @param graph
   */
  private void makeGraph(ControlGraph cg) throws Exception {
    DirectedGraph cgraph = cg.getGraph();
    /* variable names */
    LocalVariableTableAttribute lv = (LocalVariableTableAttribute)cg.getCode().getAttribute("LocalVariableTable");
    /* add start vertex */
    /* map from bb to in instruction */
    Map binm = new HashMap();
    Map boutm = new HashMap();
    int numb = 1;
    BasicBlock bbs = new BasicBlock.StartBlock();
    BasicBlock bbe = new BasicBlock.EndBlock();
    this.start = new VarNode(bbs, -1, "START");
    Object end = new VarNode(bbe, -1, "END");
    graph.add(start);
    graph.add(end);
    /* iterate over vertices to store references */
    Iterator it = cgraph.getVerticesIterator();
    while(it.hasNext()) {
      BasicBlock bb = (BasicBlock)it.next();
      if(bbs.equals(bb)) {
        /* add definitions for method parameters */
        if(lv != null) {
          for(Iterator i = lv.iterator(); i.hasNext();) {
            VariableInfo info = (VariableInfo)i.next();
            /* a variable defined at method call */
            if(info.getStart_pc() == 0) {
              Object v = new DefNode(bb, info.getIndex(), lv.getVariableName(info));
              graph.add(v);
              graph.addEdge(new DirectedEdgeImpl(start, v));
              start = v;
            }
          }
        }
        continue;
      } else if(bbe.equals(bb)) {
        binm.put(bb, end);
        continue;
      }
      numb = bb.getNumBlock();
      Object bin = null; /* entry to block */
      Object bout = null; /* exit from block */
      Iterator it2 = bb.getInstructions();
      while(it2.hasNext()) {
        Instruction ins = (Instruction)it2.next();
        if(ins.isStore()) {
          /* retrieve index of variable */
          int vari = getVariableIndex(ins);
          /* add size of instruction to pc as definition of variable usually appears visible
           * not on the instruction it is effectively defined but on the following
           * instruction
           */
          String vn = lv != null ? lv.getVariableAt(ins.getPc() + ins.size(), vari) : "" + vari;
          /* make vertex */
          Object v = new DefNode(bb, vari, vn);
          graph.add(v);
          vmap.put(ins, v);
          /* link to last use */
          if(bin == null)
            bin = v;
          /* link this vertex */
          if(bout != null) {
            graph.addEdge(new DirectedEdgeImpl(bout, v));
          } else if(bin != v)
            graph.addEdge(new DirectedEdgeImpl(bin, v));
          bout = v;
        } else if(ins.isLoad()) {
          /* retrieve last define instruction */
          int vari = getVariableIndex(ins);
          String vn = lv != null ? lv.getVariableAt(ins.getPc(), vari) : "" + vari;
          /* make vertex */
          Object v = new CUseNode(bb, vari, vn);
          graph.add(v);
          vmap.put(ins, v);
          if(bin == null)
            bin = v;
          /* link */
          if(bout != null)
            graph.addEdge(new DirectedEdgeImpl(bout, v));
          else if(bin != v)
            graph.addEdge(new DirectedEdgeImpl(bin, v));
          bout = v;
        }
      }
      /* update block maps */
      if(bout == null)
        bin = bout = new VarNode(bb, -1, "Block " + numb);
      binm.put(bb, bin);
      boutm.put(bb, bout);
    }
    /* add control edges between vertices */
    it = boutm.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry me = (Map.Entry)it.next();
      BasicBlock bb = (BasicBlock)me.getKey();
      Object nv = me.getValue();
      Object ov = cgraph.findVertex(bb);
      Iterator it2 = cgraph.getOutgoingEdges(ov).iterator();
      while(it2.hasNext()) {
        DirectedEdge de = (DirectedEdge)it2.next();
        BasicBlock bbf = (BasicBlock)de.getSink();
        Object nvo = binm.get(bbf);
        graph.addEdge(new DirectedEdgeImpl(nv, nvo));
      }

    }
    /* link to start */
    it = cgraph.getAdjacentVertices(cgraph.findVertex(bbs)).iterator();
    while(it.hasNext()) {
      BasicBlock bb = (BasicBlock)it.next();
      Object iv = (Object)binm.get(bb);
      graph.addEdge(new DirectedEdgeImpl(start, iv));
    }
  }

  /**
   * @param ins
   * @return
   */
  private int getVariableIndex(Instruction ins) {
    switch(ins.opcode()) {
    case opc_iload:
    case opc_lload:
    case opc_fload:
    case opc_dload:
    case opc_aload:
    case opc_istore:
    case opc_lstore:
    case opc_fstore:
    case opc_dstore:
    case opc_astore:
      return (int)((OneArgInst)ins).arg();
    case opc_iload_0:
    case opc_lload_0:
    case opc_fload_0:
    case opc_dload_0:
    case opc_aload_0:
    case opc_istore_0:
    case opc_lstore_0:
    case opc_fstore_0:
    case opc_dstore_0:
    case opc_astore_0:
      return 0;
    case opc_iload_1:
    case opc_lload_1:
    case opc_fload_1:
    case opc_dload_1:
    case opc_aload_1:
    case opc_istore_1:
    case opc_lstore_1:
    case opc_fstore_1:
    case opc_dstore_1:
    case opc_astore_1:
      return 1;
    case opc_iload_2:
    case opc_lload_2:
    case opc_fload_2:
    case opc_dload_2:
    case opc_aload_2:
    case opc_istore_2:
    case opc_lstore_2:
    case opc_fstore_2:
    case opc_dstore_2:
    case opc_astore_2:
      return 2;
    case opc_iload_3:
    case opc_lload_3:
    case opc_fload_3:
    case opc_dload_3:
    case opc_aload_3:
    case opc_istore_3:
    case opc_lstore_3:
    case opc_fstore_3:
    case opc_dstore_3:
    case opc_astore_3:
      return 3;
    default:
      return -1;
    }
  }

  /**
   * @return
   */
  public DirectedGraph getGraph() {
    return graph;
  }

  public Object getStart() {
    return start;
  }
}