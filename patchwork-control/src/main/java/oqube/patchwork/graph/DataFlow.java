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
package oqube.patchwork.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import oqube.bytes.Opcodes;
import oqube.bytes.attributes.LocalVariableTableAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.OneArgInst;
import oqube.bytes.instructions.TwoArgInst;
import oqube.bytes.struct.MethodFileInfo;
import salvo.jesus.graph.DirectedGraph;

/**
 * A class for annotating a {@link oqube.patchwork.graph.ControlGraph} with
 * data-flow information. This class identifies definition and usage of
 * variables in a control flow and construct a map from the graph's blocks to a
 * list of nodes. This information can then be used to construct data-flow paths
 * or pairs. Note that this algorithms distinguishes all definition/uses of the
 * same variable in each block.
 * 
 * @author nono
 * 
 */
public class DataFlow implements Opcodes {

  /**
   * Defines a node that represents usage of a variable. The node encapsulates a
   * reference to its enclosing block.
   * 
   */
  public static class VarNode {

    private int pos; // position in code

    private int index; // index in local array. Integer.MAX_VALUE means static
                        // variable

    private String name;

    VarNode(int pos, int i, String n) {
      this.pos = pos;
      this.index = i;
      this.name = n;
    }

    public int getPos() {
      return pos;
    }

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }

  }

  public static abstract class UseNode extends VarNode {

    UseNode(int pos, int i, String n) {
      super(pos, i, n);
    }
  }

  /**
   * A class representing computational-use of a variable.
   * 
   * @author nono
   * @version $Id: DataFlow.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class CUseNode extends UseNode {

    CUseNode(int pos, int i, String n) {
      super(pos, i, n);
    }

    public String toString() {
      return "c-use " + getName();
    }

  }

  /**
   * A class represneting predicate-use of a variable.
   * 
   * @author nono
   * @version $Id: DataFlow.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class PUseNode extends UseNode {

    PUseNode(int pos, int i, String n) {
      super(pos, i, n);
    }

    public String toString() {
      return "p-use " + getName();
    }

  }

  /**
   * A class representing definition of variable.
   * 
   * @author nono
   * @version $Id: DataFlow.java 1 2007-03-05 22:06:45Z arnaud.oqube $
   */
  public static class DefNode extends VarNode {

    DefNode(int pos, int i, String n) {
      super(pos, i, n);
    }

    public String toString() {
      return "def " + getName();
    }

  }

  private Map /* < BasicBlock, List<VarNode> > */data;

  private int maxLocals;

  /**
   * Construct a data-flow graph from a control graph.
   * 
   * @param graph
   */
  public DataFlow(ControlGraph cgraph) throws Exception {
    this.data = new HashMap();
    this.maxLocals = cgraph.getMethod().getCodeAttribute().maxlocals();
    populateData(cgraph);
  }

  /**
   * Add all def and use nodes to each block.
   * 
   * @param graph
   */
  private void populateData(ControlGraph cg) throws Exception {
    DirectedGraph cgraph = cg.getGraph();
    /* variable names */
    LocalVariableTableAttribute lv = (LocalVariableTableAttribute) cg.getCode()
        .getAttribute("LocalVariableTable");
    /* stack for identifying variables */
    Stack st = new Stack();
    /* map from bb to in instruction */
    BasicBlock bbs = new BasicBlock.StartBlock();
    BasicBlock bbe = new BasicBlock.EndBlock();
    /* iterate over vertices to store references */
    Iterator it = cgraph.getVerticesIterator();
    while (it.hasNext()) {
      BasicBlock bb = (BasicBlock) it.next();
      if (bbs.equals(bb)) {
        /* add definitions for method parameters */
        MethodFileInfo mfi = cg.getMethod();
        List args = mfi.getArguments();
        int j = 0;
        for (Iterator i = args.iterator(); i.hasNext();) {
          String type = (String) i.next();
          String name = (lv != null) ? lv.getVariableAt(0, j) : "" + j;
          DefNode node = new DefNode(0, j, name);
          addToBlock(bb, node);
          if ("D".equals(type) || "L".equals(type))
            j += 2;
          else
            j++;
        }
        continue;
      } else if (bbe.equals(bb)) {
        continue;
      }
      Iterator it2 = bb.getInstructions();
      while (it2.hasNext()) {
        Instruction ins = (Instruction) it2.next();
        if (ins.isStore()) {
          /* retrieve index of variable */
          int vari = getVariableIndex(ins);
          /*
           * add size of instruction to pc as definition of variable usually
           * appears visible not on the instruction it is effectively defined
           * but on the following instruction
           */
          String vn = lv != null ? lv.getVariableAt(ins.getPc() + ins.size(),
              vari) : "" + vari;
          /* make vertex */
          DefNode dn = new DefNode(ins.getPc(), vari, vn);
          addToBlock(bb, dn);
        } else if (ins.isLoad()) {
          /* retrieve last define instruction */
          int vari = getVariableIndex(ins);
          String vn = lv != null ? lv.getVariableAt(ins.getPc(), vari) : ""
              + vari;
          /* make vertex */
          CUseNode cn = new CUseNode(ins.getPc(), vari, vn);
          addToBlock(bb, cn);
        } else if (ins.opcode() == opc_getfield) {
          // use an instance field
        } else if (ins.opcode() == opc_getstatic) {
          // use a class field
          String n = cg.getMethod().getClassFile().getConstantPool().getEntry(
              ((TwoArgInst) ins).shortArg()).toString();
          CUseNode cn = new CUseNode(ins.getPc(), Integer.MAX_VALUE, n);
          addToBlock(bb, cn);
        } else if (ins.opcode() == opc_putstatic) {
          // use a class field
          String n = cg.getMethod().getClassFile().getConstantPool().getEntry(
              ((TwoArgInst) ins).shortArg()).toString();
          DefNode cn = new DefNode(ins.getPc(), Integer.MAX_VALUE, n);
          addToBlock(bb, cn);
        } else if (ins.opcode() == opc_aaload) {

        } else if (ins.opcode() == opc_aastore) {

        }
      }
    }
  }

  /*
   * Adds a new node to the DU map for this block. @param bb @param node
   */
  private void addToBlock(BasicBlock bb,
      oqube.patchwork.graph.DataFlow.VarNode node) {
    List nodes = (List) data.get(bb);
    if (nodes == null) {
      nodes = new ArrayList();
      data.put(bb, nodes);
    }
    nodes.add(node);
  }

  /**
   * @param ins
   * @return
   */
  private int getVariableIndex(Instruction ins) {
    switch (ins.opcode()) {
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
      return (int) ((OneArgInst) ins).arg();
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
   * @return Returns the data. A Map<BasicBlock,List<VarNode>> instance.
   */
  public Map getData() {
    return data;
  }

  /**
   * @param data
   *          The data to set.
   */
  public void setData(Map data) {
    this.data = data;
  }

}
