package oqube.patchwork.graph;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.attributes.LineNumberTableAttribute;
import oqube.bytes.attributes.SourceFileAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.VarArgInst;
import oqube.bytes.struct.AttributeFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.GraphException;

/**
 * Main class for constructing and manipulating a control graph
 * 
 * @author Arnaud Bailly
 * @version $Id
 */
public class ControlGraph implements Opcodes {

  private Object startVertex;

  private Object endVertex;

  /*
   * the control graph - nodes are basic blocks and edges are braching
   * instruction
   */
  private DirectedGraph graph;

  private MethodFileInfo method;

  private CodeAttribute code;

  private SortedSet<BasicBlock> bset;

  private ClassFile classFile;

  /*
   * the source file where this method is located. May be null.
   */
  private String sourceFile;

  /**
   * Constructs an empty ControlGraph. The graph can later be constructed
   * through calls to {@link parseCode},{@link parseExceptions},
   * {@link parseLineNumbers}or {@link parseLocalVariables}.
   * 
   * @throws Exception
   * 
   */
  public ControlGraph() {
    this.graph = new DirectedGraphImpl();
    this.endVertex = new BasicBlock.EndBlock();
    this.startVertex = new BasicBlock.StartBlock();
    try {
      graph.add(endVertex);
      graph.add(startVertex);
    } catch (GraphException e) {
      // never happens ?
      e.printStackTrace();
    }
  }

  /**
   * Create a ControlGraph from a CodeAttribute object. The instructions are
   * directly retrieved from the CodeAttribute as a list and all available
   * information is used to construct exceptions handlers nodes, line number and
   * variable names info.
   * 
   * @param info
   *          the method this control graph is created from.
   * @throws Exception
   */
  public ControlGraph(MethodFileInfo info) throws Exception {
    this();
    this.classFile = info.getClassFile();
    this.method = info;
    this.code = method.getCodeAttribute();
    this.sourceFile = getSourceFile(this.classFile);
    if (this.code == null) // abstract method
      throw new IOException(
          "Cannot construct control graph for an abstract method");
    parseCode(code.getAllInstructions(), 0);
    parseExceptions(code);
    /* numerotate blocks in ascending order of start pc */
    identifyBlocks();
  }

  private String getSourceFile(ClassFile file) {
    List<AttributeFileInfo> sfa = file
        .getAttribute(SourceFileAttribute.attname);
    if (!sfa.isEmpty())
      return ((SourceFileAttribute) sfa.get(0)).getSourceFile();
    else
      return null;
  }

  /**
   * This method simply do a breadth-first traversal of blocks and numerotates
   * them.
   * 
   */
  private void identifyBlocks() {
    /* sort list of blocks used in graph */
    SortedSet<BasicBlock> l = new TreeSet<BasicBlock>();
    for (Iterator i = graph.getAllVertices().iterator(); i.hasNext();) {
      l.add((BasicBlock) i.next());
    }
    /* numerotate */
    int j = 0;
    for (Iterator<BasicBlock> i = l.iterator(); i.hasNext();) {
      i.next().setNumBlock(j++);
    }
    /* get line numbers */
    LineNumberTableAttribute lines = (LineNumberTableAttribute) code
        .getAttribute("LineNumberTable");
    if (lines != null)
      setLines(lines, l);
  }

  /*
   * add information about start and end line numbers for each block in this
   * control graph.
   */
  private void setLines(LineNumberTableAttribute lines, SortedSet blocks) {
    int[][] ln = lines.getLineInfo();
    for (Iterator i = blocks.iterator(); i.hasNext();) {
      BasicBlock bb = ((BasicBlock) i.next());
      if (bb.isStart() || bb.isEnd())
        continue;
      bb.setStartLine(lineFromPc(ln, bb.getStart()));
      bb.setEndLine(lineFromPc(ln, bb.getEnd()));
      if (bb.getStartLine() > bb.getEndLine()) {
        int l = bb.getStartLine();
        bb.setStartLine(bb.getEndLine());
        bb.setEndLine(l);
      }
    }

  }

  private Comparator<int[]> pcComp = new Comparator<int[]>() {

    // compare o[0], pc number
    public int compare(int[] o1, int[] o2) {
      return (o1[0] > o2[0] ? 1 : (o1[0] < o2[0] ? -1 : 0));
    }

  };

  /*
   * return the line number for a given pc. The line number of a pc is the first
   * line found in lines whose pc is greater than or equal to pc. Search is
   * binary.
   */
  private int lineFromPc(int[][] lines, int pc) {
    int ix = Arrays.<int[]>binarySearch(lines, new int[] { pc, 0 }, pcComp);
    if (ix >= 0)
      return lines[ix][1];
    else
      return lines[-ix - 2][1];
  }

  /**
   * Populate this control graph with the code from given code array. This
   * method can be called several times provided the code arrays do not overlap
   * one another. Note that this method does not handle exceptions handlers
   * which means control graph will probably be not complete nor meaningful
   * 
   * @param code
   *          the byte array containing bytecode instructions
   * @param pc
   *          the pc offset to complete control graph
   * @throws Exception
   */
  public void parseCode(byte[] code, int pc) throws Exception {
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(code));
    Instruction ins;
    List<Instruction> instructions = new ArrayList<Instruction>();
    int l = code.length;
    for (int i = 0; i < l; i += ins.size()) {
      ins = Instruction.read(i + pc, dis, this.code.getClassFile());
      instructions.add(ins);
    }
    parseCode(instructions, pc);
  }

  /*
   * Main method for construction control flow graph
   */
  private void parseCode(List instructions, int startpc) throws Exception {
    /* store blocks in ascending start offset order */
    if (bset == null)
      bset = new TreeSet<BasicBlock>();
    /* initialize first block */
    BasicBlock cur = lookup(startpc);
    Object v = cur;
    if (startpc == 0)
      graph.addEdge(new DirectedEdgeImpl(startVertex, v, ""));
    /* add instructions one at a time */
    Iterator it = instructions.iterator();
    while (it.hasNext()) {
      Instruction ins = (Instruction) it.next();
      int pc = ins.getPc();
      cur.addInstruction(ins);
      Object from = cur;
      /* handle branching instruction */
      if (ins.isBranching()) {
        /* add instruction to block whose start offset matches ins */
        /* split block after instruction */
        BasicBlock split = lookup(pc + ins.size());
        Object splitv = split;
        /* make edge to target */
        int targetpc = ins.targetPc();
        if (targetpc == Integer.MIN_VALUE) { /* special cases */
          if (ins.opcode() == opc_tableswitch) {
            tableSwitch(cur, (VarArgInst) ins);
          } else if (ins.opcode() == opc_lookupswitch)
            lookupSwitch(cur, (VarArgInst) ins);
          else if (ins.opcode() == opc_ret)
            ; /*
               * should add edge to each jsr ?
               */
          else
            throw new Exception(
                "Dont know how to handle branch for instruction " + ins);
        } else if (targetpc == Integer.MAX_VALUE) {
          /* a return instruction - branch to end */
          graph.addEdge(new DirectedEdgeImpl(from, endVertex, ins.toString()));
        } else {
          /* get target block and its vertex */
          BasicBlock tgt = lookup(pc + targetpc);
          Object tgtv = tgt;
          /* split block if not new */
          if (tgt.getStart() != (pc + targetpc)) {
            BasicBlock bb = splitBlock(tgt, (pc + targetpc));
            Object bbv = bb;
            graph.addEdge(new DirectedEdgeImpl(tgtv, bbv, ""));
          }
          /* make edge from cur */
          graph.addEdge(new DirectedEdgeImpl(from, tgtv, ins.toString()));
        }
        /* check if ins is conditional branch */
        if (ins.isConditional())
          /* make edge from cur */
          graph
              .addEdge(new DirectedEdgeImpl(from, splitv, "!" + ins.toString()));
        /* remove last block */
        if (!it.hasNext())
          graph.remove(splitv);
        cur = split;
      } else {
        /* update cur */
        BasicBlock tmp = find(ins.size() + pc);
        if (tmp != null) {
          cur = tmp;
          /* link blocks */
          Object lv = cur;
          graph.addEdge(new DirectedEdgeImpl(from, lv, ""));
        }
      }
    }
  }

  /*
   * parse exceptions constructed in a codeattribute
   */
  private void parseExceptions(CodeAttribute code) throws Exception {
    Iterator it = code.getExceptions().iterator();
    while (it.hasNext()) {
      CodeAttribute.ExceptionTableEntry exc = (CodeAttribute.ExceptionTableEntry) it
          .next();
      BasicBlock exchandler = lookup(exc.getJump());
      Object ev = exchandler;
      /* get exception name */
      String ename = exc.getClassIndex() == 0 ? "any" : code.getClassFile()
          .getConstantPool().getEntry(exc.getClassIndex()).toString();
      int start = exc.getStart();
      int end = exc.getEnd();
      /* find all blocks withing start and end pc */
      BasicBlock tryblock = findLocation(start);
      BasicBlock bb;
      if (tryblock == null)
        throw new Exception("Unable to find try block for exception " + exc);
      BasicBlock trybv = tryblock;
      // split block if start address is before try block
      if (tryblock.getStart() < start) {
        bb = splitBlock(tryblock, (start));
        trybv = bb;
      }
      // link try block to exception block
      graph.addEdge(new DirectedEdgeImpl(trybv, ev, ename));
      // split block if end address is after try block
      start = ((BasicBlock) trybv).getEnd() + 1;
      while (start < end) {
        bb = findLocation(start);
        trybv = bb;
        assert (trybv != null) : "Unable to find try block for exception "
            + exc + " @ " + start + " in code " + code;
        graph.addEdge(new DirectedEdgeImpl(trybv, ev, ename));
        start = bb.getEnd() + 1;
      } 
      assert start >= end;
      // split last block
      if(start > end){
        bb = splitBlock(trybv, end);        
      }
    }
  }

  /*
   * Find a block in bset which contains start address
   * 
   * @param start @return
   */
  private BasicBlock findLocation(int start) {
    Iterator it = bset.iterator();
    while (it.hasNext()) {
      BasicBlock bb = (BasicBlock) it.next();
      if ((bb.getStart() <= start) && (bb.getEnd() >= start))
        return bb;
    }
    return null;
  }

  /*
   * handle lookupswitch instruction
   * 
   * @param cur @param inst
   */
  private void lookupSwitch(BasicBlock cur, VarArgInst lkp) throws Exception {
    int def = lkp.getDefault();
    int[][] pairs = lkp.getPairs();
    int spc = lkp.getPc();
    Object fv = cur;
    /* get block at def address */
    BasicBlock tgt = lookup(spc + def);
    Object tgtv = tgt;
    graph.addEdge(new DirectedEdgeImpl(fv, tgtv, "default"));
    for (int i = 0; i < pairs.length; i++) {
      int match = pairs[i][0];
      tgt = lookup(pairs[i][1] + spc);
      tgtv = tgt;
      graph.addEdge(new DirectedEdgeImpl(fv, tgtv, "" + match));
    }
  }

  /*
   * handle tableswitch instructions
   */
  private void tableSwitch(BasicBlock from, VarArgInst tbl) throws Exception {
    int def = tbl.getDefault();
    int[] off = tbl.getOffsets();
    int high = tbl.getHigh();
    int low = tbl.getLow();
    int spc = tbl.getPc();
    Object fv = from;
    /* get block at def address */
    BasicBlock tgt = lookup(spc + def);
    Object tgtv = tgt;
    graph.addEdge(new DirectedEdgeImpl(fv, tgtv, "default"));
    for (int i = 0; i < off.length; i++) {
      tgt = lookup(off[i] + spc);
      tgtv = tgt;
      graph.addEdge(new DirectedEdgeImpl(fv, tgtv, "" + (high - low + i)));
    }
  }

  /**
   * Retrieves a block containing pc. If block does not exists, return null.
   * 
   * @param i
   * @return
   */
  private BasicBlock find(int targetpc) {
    BasicBlock bs = new BasicBlock(targetpc, classFile);
    SortedSet tgts = bset.headSet(bs);
    BasicBlock tgt = null;
    /* search blocks overlaping targetpc */
    if (tgts.size() != 0) {
      tgt = (BasicBlock) tgts.last();
      if (tgt.getEnd() > targetpc) {
        return tgt;
      }
    }
    /* search block starting at pc */
    tgts = bset.tailSet(bs);
    if (tgts.size() > 0) {
      tgt = (BasicBlock) tgts.first();
      if (tgt.getStart() == targetpc) {
        return tgt;
      }
    }
    return null;
  }

  /*
   * Retrieves a block containing pc. If block does not exists, create it. The
   * vertexmap and graph are updated.
   */
  private BasicBlock lookup(int targetpc) throws Exception {
    BasicBlock tgt = find(targetpc);
    if (tgt != null)
      return tgt;
    /* create new block */
    tgt = new BasicBlock(targetpc, classFile);
    bset.add(tgt);
    Object tgtv = tgt;
    graph.add(tgtv);
    return tgt;
  }

  /**
   * @param tgt
   * @param targetpc
   */
  private BasicBlock splitBlock(BasicBlock tgt, int targetpc) throws Exception {
    BasicBlock bb = new BasicBlock(targetpc, classFile);
    bset.add(bb);
    int s1 = 0;
    Iterator it = tgt.getInstructions();
    while (it.hasNext()) {
      Instruction ins = (Instruction) it.next();
      if (ins.getPc() >= targetpc) {
        it.remove();
        bb.addInstruction(ins);
      } else
        s1 += ins.size();
    }
    /* rechain block in graph */
    Object orig = tgt;
    Object bbv = bb;
    graph.add(bbv);
    /*
     * iterate over outgoing edges of orig - list returned is cloned to prevent
     * concurrent modifications problems in graph
     */
    it = new ArrayList(graph.getOutgoingEdges(orig)).iterator();
    while (it.hasNext()) {
      Edge e = (Edge) it.next();
      Object to = e.getOppositeVertex(orig);
      Object data = e.getData();
      graph.removeEdge(e);
      graph.addEdge(new DirectedEdgeImpl(bbv, to, data));
    }
    /* add edge to split block */
    graph.addEdge(new DirectedEdgeImpl(orig, bbv, ""));
    return bb;
  }

  /**
   * Get the generated graph.
   * 
   * @return
   */
  public DirectedGraph getGraph() {
    return graph;
  }

  public CodeAttribute getCode() {
    return code;
  }

  /**
   * Return the list of all blocks in this control flow graph without start and
   * end bloks.
   * 
   * @return a List<BasicBlock> without StartBLock and EndBlock instances. The
   *         list is ordered by block number.
   */
  public List<BasicBlock> getBlocks() {
    /* sort list of blocks used in graph */
    SortedSet<BasicBlock> l = new TreeSet<BasicBlock>();
    for (Iterator i = graph.getAllVertices().iterator(); i.hasNext();) {
      BasicBlock bb = (BasicBlock) i.next();
      if (bb.isStart() || bb.isEnd())
        continue;
      else
        l.add(bb);
    }
    return new ArrayList<BasicBlock>(l);
  }

  /**
   * @return Returns the method.
   */
  public MethodFileInfo getMethod() {
    return method;
  }

  /**
   * @return Returns the sourceFile.
   */
  public String getSourceFile() {
    return sourceFile;
  }

  /**
   * @param sourceFile
   *          The sourceFile to set.
   */
  public void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }
}