/*______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Dec 28, 2005
 *
 */
package oqube.patchwork.instrument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.attributes.LineNumberTableAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.instructions.VarArgInst;
import oqube.bytes.pool.MethodRefData;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;

/**
 * Modify the bytecode of a ClassFile objects.
 * <p>
 * Instances of this class work with {@link oqube.bytes.ClassFile} objects that
 * should be constructed one way or another by clients. Between each invocation
 * of method {@link #reset()}, they store a list of <em>instrumented</em>
 * ClassFile objects and a corresponding list of instrumented methods. The order
 * of classes and methods within each list is important as they correspond to
 * indices passed to
 * {@link oqube.patchwork.instrument.Coverage#cover(int,int,int)} method at
 * runtime.
 * </p>
 * 
 * @author nono
 * @version $Id: InstrumentClass.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class InstrumentClass {

  /*
   * size of coverage invocation instruction
   */
  private static final int COVER_SIZE = 12;

  /*
   * result of instrumentation
   */
  private ClassFile instrumented;

  /*
   * list of classes
   */
  private List<ClassFile> classes;

  /*
   * map of list of method names and types
   */
  private List<List<String>> methods;

  /**
   * Default constructor. Initializes the data structures instrumented.
   * 
   */
  public InstrumentClass() {
    reset();
  }

  /**
   * Reinitializes data stored in this instrumentation object.
   * 
   */
  public void reset() {
    this.classes = new ArrayList<ClassFile>();
    this.methods = new ArrayList<List<String>>();
  }

  /**
   * Instrument the given class file instance.
   * 
   * @param cf
   */
  public ClassFile instrument(ClassFile cf) {
    /* create new instrumented classfile */
    this.instrumented = initializeFrom(cf);
    /* append to classes list */
    if (this.classes.size() > Short.MAX_VALUE)
      throw new RuntimeException("Cannot handle more than " + Short.MAX_VALUE
          + " number of classes");
    short cid = (short) (this.classes.size());
    this.classes.add(this.instrumented);
    /*
     * loop over all methods of class
     */
    Iterator it = instrumented.getAllMethods().iterator();
    List<String> meths = new ArrayList<String>();
    String mname = null;
    short mid = 0;
    while (it.hasNext()) {
      MethodFileInfo mfi = (MethodFileInfo) it.next();
      if (mid > Short.MAX_VALUE)
        throw new RuntimeException("Cannot handle more than " + Short.MAX_VALUE
            + " number of methods");
      mname = mfi.getName();
      meths.add(mname + mfi.getSignature());
      /* update instrumented classfile */
      /* Special case: if instrumenting Coverage class, bypass */
      if (cf.getClassFileInfo().getName().equals(
          "oqube/patchwork/report/Coverage"))
        continue;
      instrument(mfi, cid, mid);
      mid++;
    }
    /* store list of method names */
    methods.add(meths);
    return this.instrumented;
  }

  /**
   * Create new ClassFile instance that is a byte-to-byte copy of the given
   * ClassFile object.
   * 
   * @param cf
   * @return cloned class file
   * @throws IOException
   */
  private ClassFile initializeFrom(ClassFile cf) {
    try {
      ClassFile inst = new ClassFile();
      /* write to a byte array the class file and read it back in new object */
      /* write all data */
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(bos);
      cf.write(dos);
      dos.flush();
      dos.close();
      bos.close();
      /* read all data */
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      DataInputStream dis = new DataInputStream(bis);
      inst.read(dis);
      return inst;
    } catch (IOException io) {
      throw new RuntimeException("Unexpected IOException :", io);
    }
  }

  private void instrument(MethodFileInfo mfi, short cid, short mid) {
    /* extract control graph from method */
    CodeAttribute code = mfi.getCodeAttribute();
    if (code == null)
      return;
    ClassFile cf = mfi.getClassFile();
    ControlGraph cgraph;
    try {
      cgraph = new ControlGraph(mfi);
    } catch (Exception e) {
      throw new RuntimeException(
          "Unexpected exception while constructing graph", e);
    }
    /* create new code block */
    CodeAttribute newcode = new CodeAttribute(cf, (short) code.maxlocals());
    /* extract blocks */
    List l = cgraph.getGraph().getAllVertices();
    BasicBlock bbst = new BasicBlock.StartBlock();
    BasicBlock bben = new BasicBlock.EndBlock();
    List<BasicBlock> bbs = new ArrayList<BasicBlock>();
    for (Iterator i = l.iterator(); i.hasNext();) {
      BasicBlock bb = (BasicBlock) i.next();
      /* skip dummy blocks */
      if (bbst.equals(bb) || bben.equals(bb))
        continue;
      bbs.add(bb);
    }
    /* new offset table */
    int[][] offsets = new int[bbs.size() + 1][2];
    /* index of end block */
    int endix = bbs.size() + 1;
    /* order basic blocks by their starting pc */
    Collections.sort(bbs);
    /* map for recreating exception handler and jump offsets */
    Map<Instruction, Instruction> startinstmap = new HashMap<Instruction, Instruction>();
    Map<Instruction, Instruction> endinstmap = new HashMap<Instruction, Instruction>();
    int nbs = 0; // number of blocks
    int offset = 0;
    short cover = MethodRefData.create(cf.getConstantPool(),
        "oqube/patchwork/report/Coverage", "cover", "(SSS)V");
    /* prepend call instruction to each basic block */
    for (Iterator i = bbs.iterator(); i.hasNext();) {
      BasicBlock bb = (BasicBlock) i.next();
      if (bbst.equals(bb) || bben.equals(bb))
        continue;
      nbs++;
      /* create call sequence */
      Sequence seq = new Sequence(cf);
      if (bb.getNumBlock() == 1) {
        // add method enter to first block
        seq._sipush(cid)._sipush(mid)._sipush((short) 0)._invokestatic(cover,
            cf);/* invocation */
        offset = COVER_SIZE;
      }
      /* start of block */
      seq._sipush(cid)._sipush(mid)._sipush((short) bb.getNumBlock())
          ._invokestatic(cover, cf);/* invocation */
      Sequence orig = (Sequence) bb.getAllInstructions().clone();
      // handle possible return
      List<Instruction> oldints = bb.getAllInstructions().getInstructions();
      Instruction last = (Instruction) oldints.get(oldints.size() - 1);
      if (last.isBranching() && last.targetPc() == Integer.MAX_VALUE) {
        for (Iterator it = orig.iterator(); it.hasNext();) {
          Instruction iti = (Instruction) it.next();
          if (it.hasNext())
            seq.add(iti);
          else {
            seq._sipush(cid)._sipush(mid)._sipush((short) endix)._invokestatic(
                cover, cf);/* invocation */
            seq.add(iti);
          }
        }
      } else
        seq.add(orig);
      List<Instruction> insts = seq.getInstructions();
      Instruction first = insts.get(0);
      /* map for exception handlers */
      startinstmap.put(oldints.get(0), first);
      endinstmap.put(last, insts.get(insts.size() - 1));
      newcode.add(seq);
      offsets[bb.getNumBlock() - 1][0] = bb.getStart();
      offsets[bb.getNumBlock() - 1][1] = offset;
      if (bb.getNumBlock() == 1)
        offset = seq.size();
      else
        offset += seq.size();
    }
    offsets[nbs][0] = code.getCodeLength();
    offsets[nbs][1] = newcode.getCodeLength();
    /* recompute jump offsets */
    fixJumps(bbs, offsets, endinstmap);
    /* recompute exception handlers table */
    fixExceptions(code, newcode, offsets);
    /* change line number attribute */
    fixLineNumbers(code, newcode, offsets);
    mfi.setCode(newcode);
  }

  private void fixLineNumbers(CodeAttribute code, CodeAttribute newcode,
      int[][] offsets) {
    LineNumberTableAttribute lines = (LineNumberTableAttribute) code
        .getAttribute(LineNumberTableAttribute.attname);
    if (lines == null)
      return;
    LineNumberTableAttribute nlines = new LineNumberTableAttribute(newcode
        .getClassFile());
    for (int[] line : lines.getLineInfo()) {
      int bk = blockFromIndex(offsets, line[0]);
      int pc = line[0] == 0 ? 0 : (line[0] - offsets[bk][0]) + offsets[bk][1];
      nlines.addLineInfo(pc, line[1]);
    }
    newcode.addAttribute(nlines);
  }

  /**
   * @param code
   * @param newcode
   * @param offsets
   */
  private void fixExceptions(CodeAttribute code, CodeAttribute newcode,
      int[][] offsets) {
    for (Iterator i = code.getExceptions().iterator(); i.hasNext();) {
      CodeAttribute.ExceptionTableEntry exc = (CodeAttribute.ExceptionTableEntry) i
          .next();
      int nb = blockFromStartIndex(offsets, exc.getEnd());
      assert nb != -1 : "Cannot compute block number from end index "
          + exc.getEnd() + ", dumping code:" + code + ", dumping offsets :"
          + print(offsets);
      int ne = offsets[nb][1];
      nb = blockFromStartIndex(offsets, exc.getStart());
      assert nb != -1;
      int nst = offsets[nb][1];
      nb = blockFromStartIndex(offsets, exc.getJump());
      assert nb != -1;
      int nj = offsets[nb][1];
      newcode.makeExceptionEntry(exc.getClassIndex(), (short) nst, (short) ne,
          (short) nj);
    }
  }

  private String print(int[][] offsets) {
    StringBuffer sb = new StringBuffer();
    final String EOL = System.getProperty("line.separator");
    for (int i = 0, ln = offsets.length; i < ln; i++) {
      sb.append(offsets[i][0]).append(" -> ").append(offsets[i][1]).append(EOL);
    }
    return sb.toString();
  }

  private int blockFromStartIndex(int[][] off, int idx) {
    for (int i = 0; i < off.length; i++) {
      if (off[i][0] == idx)
        return i;
    }
    return -1;
  }

  /*
   * for fixing exceptions
   */
  private int blockFromIndex(int[][] off, int idx) {
    for (int i = 0; i < off.length; i++) {
      if (off[i][0] >= idx)
        return i;
    }
    return -1;
  }

  /*
   * for fixing line numbers
   */
  private int blockFromIndex2(int[][] off, int idx) {
    int i = 0;
    while (i < off.length && off[i][0] <= idx)
      i++;
    return i - 1;
  }

  /**
   * @param bbs
   * @param offsets
   * @param endinstmap
   */
  private void fixJumps(List bbs, int[][] offsets, Map endinstmap) {
    for (Iterator i = bbs.iterator(); i.hasNext();) {
      BasicBlock bb = (BasicBlock) i.next();
      /* skip dummy blocks */
      if (bb.isStart() || bb.isEnd())
        continue;
      /* if last instruction is a jump, we need to recompute its offset */
      List insts = (List) bb.getAllInstructions().getInstructions();
      Instruction last = (Instruction) insts.get(insts.size() - 1);
      if (last.isBranching()) {
        int j = last.targetPc();
        if (j == Integer.MAX_VALUE) // return
          continue;
        if (j == Integer.MIN_VALUE) /*
                                     * special cases of tablewitch and lookup
                                     * switch
                                     */
          fixSpecial(last, bb, offsets, endinstmap);
        else {
          int bi = blockFromStartIndex(offsets, j + last.getPc());
          /* retrieve last instruction from new block */
          Instruction nlast = (Instruction) endinstmap.get(last);
          nlast.setTargetPc(offsets[bi][1] - nlast.getPc());
        }
      }
    }
  }

  /**
   * Special handling for tableswitch and lookupswithc instructions
   * 
   * @param last
   * @param bb
   * @param offsets
   * @param endinstmap
   */
  private void fixSpecial(Instruction last, BasicBlock bb, int[][] offsets,
      Map endinstmap) {
    if (last.opcode() == Opcodes.opc_ret) // nothing to do
      return;
    VarArgInst sw = (VarArgInst) last;
    /* retrieve last instruction from new block */
    VarArgInst nlast = (VarArgInst) endinstmap.get(last);
    int pc = nlast.getPc();
    int opc = last.getPc();
    /* fix tableswitch */
    if (last.opcode() == Opcodes.opc_tableswitch) {
      int def = sw.getDefault();
      int off[] = sw.getOffsets();
      int nb = blockFromStartIndex(offsets, def + opc);
      def = offsets[nb][1] - pc;
      for (int i = 0; i < off.length; i++) {
        nb = blockFromStartIndex(offsets, off[i] + opc);
        off[i] = offsets[nb][1] - pc;
      }
      nlast.setDefault(def);
      nlast.setOffsets(off);
    } else if (last.opcode() == Opcodes.opc_lookupswitch) {
      int def = sw.getDefault();
      int off[][] = sw.getPairs();
      int nb = blockFromStartIndex(offsets, def + opc);
      def = offsets[nb][1] - pc;
      for (int i = 0; i < off.length; i++) {
        nb = blockFromStartIndex(offsets, off[i][1] + opc);
        off[i][1] = offsets[nb][1] - pc;
      }
      nlast.setDefault(def);
      nlast.setPairs(off);
    }
  }

  /**
   * @return Returns the classes.
   */
  public List<ClassFile> getClasses() {
    return classes;
  }

  /**
   * @return Returns the methods.
   */
  public List<List<String>> getMethods() {
    return methods;
  }
}
