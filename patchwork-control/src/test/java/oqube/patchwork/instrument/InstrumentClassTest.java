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
 * Created on Dec 29, 2005
 *
 */
package oqube.patchwork.instrument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.lifl.utils.Pipe;
import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.attributes.LineNumberTableAttribute;
import oqube.bytes.events.BytecodeInputStream;
import oqube.bytes.events.BytecodeOutputStream;
import oqube.bytes.events.EventRecorder;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.instructions.TwoArgInst;
import oqube.bytes.instructions.VarArgInst;
import oqube.bytes.instructions.ZeroArgInst;
import oqube.bytes.loading.ByteArrayClassLoader;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.TestClassfileMaker;
import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class InstrumentClassTest extends TestCase {

  private ClassFile cf;

  private InstrumentClass inst;

  private CodeAttribute ca;

  protected void setUp() throws Exception {
    super.setUp();
    TestClassfileMaker testClassfileMaker = new TestClassfileMaker();
    /* create classfile object */
    this.cf = testClassfileMaker.makeTestClassfile();
    this.ca = testClassfileMaker.getCode();
    /* instrumentation */
    this.inst = new InstrumentClass();
  }

  /*
   * check code structure for code containing only loops: - total length of code -
   * jump targets
   */
  public void testBaseLoop() throws IOException, SecurityException,
      NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
      InvocationTargetException {
    /* code sequence */
    Sequence seq = new Sequence(cf);
    seq._iload_1()._iload_2()._ifeq(7)._nop()._goto(-6)._nop()._iconst_0()
        ._ireturn();
    ca.add(seq);
    inst.instrument(cf);
    /* basic structure test */
    assertEquals(1, inst.getClasses().size());
    assertEquals(1, ((List) inst.getMethods().get(0)).size());
    /* check code structure */
    ClassFile instrumented = (ClassFile) inst.getClasses().get(0);
    MethodFileInfo mfi = instrumented.getMethodInfo("test", "(II)I");
    CodeAttribute code = mfi.getCodeAttribute();
    /* there must e three blocks (excl. start and end) */
    assertEquals(12 + 36 + 12 + 24, code.getLength());
    /* check jumps */
    byte[] args = ((TwoArgInst) code.getAllInstructions().get(10)).args();
    assertEquals(19, (args[0] << 8 | args[1]));
    args = ((TwoArgInst) code.getAllInstructions().get(16)).args();
    assertEquals(-30, (args[0] << 8 | args[1]));
    /* check indices of call to cover */
    args = ((TwoArgInst) code.getAllInstructions().get(4)).args();
    assertEquals(0, (args[0] << 8 | args[1]));
    args = ((TwoArgInst) code.getAllInstructions().get(5)).args();
    assertEquals(0, (args[0] << 8 | args[1]));
    args = ((TwoArgInst) code.getAllInstructions().get(6)).args();
    assertEquals(1, (args[0] << 8 | args[1]));
  }

  /*
   * Check code structure for lookupswitch instruction
   */
  public void testLookupSwitch() {
    Sequence seq = new Sequence(cf);
    int[][] pairs = new int[3][2];
    pairs[0][0] = 1;
    pairs[1][0] = 2;
    pairs[2][0] = 3;
    pairs[0][1] = 37;
    pairs[1][1] = 39;
    pairs[2][1] = 41;
    seq._iload_1()._lookupswitch(1, 35, 3, pairs)._iconst_0()._ireturn()
        ._iconst_1()._ireturn()._iconst_2()._ireturn()._iconst_3()._ireturn();
    ca.add(seq);
    inst.instrument(cf);
    /* check code structure */
    ClassFile instrumented = (ClassFile) inst.getClasses().get(0);
    MethodFileInfo mfi = instrumented.getMethodInfo("test", "(II)I");
    CodeAttribute code = mfi.getCodeAttribute();
    /* there must be three blocks (excl. start and end) */
    assertEquals(12 + 60 + 12 + 44 + 48, code.getLength());
    /* check jumps */
    VarArgInst lk = ((VarArgInst) code.getAllInstructions().get(9));
    int def = lk.getDefault();
    assertEquals(35, def);
    pairs = lk.getPairs();
    assertEquals(3, pairs.length);
    assertEquals(1, pairs[0][0]);
    assertEquals(61, pairs[0][1]);
    assertEquals(2, pairs[1][0]);
    assertEquals(87, pairs[1][1]);
    assertEquals(3, pairs[2][0]);
    assertEquals(113, pairs[2][1]);
  }

  /*
   * Check code structure for tableswitch instruction
   */
  public void testTableSwitch() {
    Sequence seq = new Sequence(cf);
    int[] offsets = new int[4];
    offsets[0] = 32;
    offsets[1] = 34;
    offsets[2] = 36;
    offsets[3] = 36;
    seq._iload_1()._nop()._tableswitch(2, 30, 1, 4, offsets)._iconst_0()
        ._ireturn()._iconst_1()._ireturn()._iconst_2()._ireturn()._iconst_3()
        ._ireturn();
    ca.add(seq);
    inst.instrument(cf);
    /* check code structure */
    ClassFile instrumented = (ClassFile) inst.getClasses().get(0);
    MethodFileInfo mfi = instrumented.getMethodInfo("test", "(II)I");
    CodeAttribute code = mfi.getCodeAttribute();
    /* there must be three blocks (excl. start and end) */
    // assertEquals(60 + 12 + 40 , code.getLength());
    /* check jumps */
    VarArgInst lk = ((VarArgInst) code.getAllInstructions().get(10));
    int def = lk.getDefault();
    assertEquals(30, def);
    offsets = lk.getOffsets();
    assertEquals(4, offsets.length);
    assertEquals(56, offsets[0]);
    assertEquals(82, offsets[1]);
    assertEquals(108, offsets[2]);
    assertEquals(108, offsets[3]);
  }

  /*
   * check code structure for code with exception block
   */
  public void testExceptions() {
    /* code sequence */
    Sequence seq = new Sequence(cf);
    seq._iload_2()._nop()._goto(5)._iconst_0()._ireturn()._iconst_1()
        ._ireturn()._iconst_m1()._ireturn();
    ca.add(seq);
    /* add exceptions */
    ca.makeExceptionEntry("java/lang/Exception", (short) 0, (short) 5,
        (short) 5);
    ca.makeExceptionEntry("java/lang/RuntimeException", (short) 7, (short) 9,
        (short) 9);
    inst.instrument(cf);
    /* check structure */
    ClassFile instrumented = (ClassFile) inst.getClasses().get(0);
    MethodFileInfo mfi = instrumented.getMethodInfo("test", "(II)I");
    CodeAttribute code = mfi.getCodeAttribute();
    /* there must be four blocks (excl. start and end) */
    assertEquals(12 + 48 + 11 + 36, code.getCodeLength());
    /* check exception table */
    code.getExceptions();
    CodeAttribute.ExceptionTableEntry exc = (CodeAttribute.ExceptionTableEntry) code
        .getExceptions().get(0);
    assertEquals(12, exc.getStart());
    assertEquals(29, exc.getEnd());
    assertEquals(29, exc.getJump());
    exc = (CodeAttribute.ExceptionTableEntry) code.getExceptions().get(1);
    assertEquals(55, exc.getStart());
    assertEquals(81, exc.getEnd());
    assertEquals(81, exc.getJump());
  }
  
  /*
   * check code structure for code with exception and finally blocks (without
   * jsr). We specifically check block splitting on try range coverage.
   */
  public void testExceptionsAndFinally() throws Exception {
    /* code sequence */
    Sequence seq = new Sequence(cf);
    seq
    ._iconst_0() 
    ._istore_1() // block try
    ._iload_1()
    ._istore_2() 
    ._iconst_0() // fin bloc try
    ._istore_1() 
    ._iload_1()
    ._istore_2()
    ._iload_1()
    ._goto(13)  // jump to end
    ._iconst_1() // catch block 
    ._istore_1()
    ._iload_1()
    ._istore_2()
    ._iconst_0()
    ._ireturn()
    ._iconst_3() // bloc finally
    ._istore_1() 
    ._iload_1()
    ._istore_2()
     ._ireturn(); // end
    ca.add(seq);
    /* add exceptions */
    ca.makeExceptionEntry("java/lang/Exception", (short) 1, (short) 4,
        (short) 12);
    ca.makeExceptionEntry((short)0, (short) 1, (short) 4,
        (short) 18);
    // finally pour exception
    ca.makeExceptionEntry((short)0, (short) 12, (short) 18,
        (short) 18);
    inst.instrument(cf);
    /* check structure */
    ClassFile instrumented = (ClassFile) inst.getClasses().get(0);
    MethodFileInfo mfi = instrumented.getMethodInfo("test", "(II)I");
    CodeAttribute code = mfi.getCodeAttribute();
    System.err.println(code.getExceptions());
    /* check exception table */
    CodeAttribute.ExceptionTableEntry exc = (CodeAttribute.ExceptionTableEntry) code
        .getExceptions().get(0);
    assertEquals(25, exc.getStart());
    assertEquals(40, exc.getEnd());
    assertEquals(60, exc.getJump());
    exc = (CodeAttribute.ExceptionTableEntry) code.getExceptions().get(1);
    assertEquals(25, exc.getStart());
    assertEquals(40, exc.getEnd());
    assertEquals(90, exc.getJump());
    exc = (CodeAttribute.ExceptionTableEntry) code.getExceptions().get(2);
    assertEquals(60, exc.getStart());
    assertEquals(90, exc.getEnd());
    assertEquals(90, exc.getJump());
   }

  
  public void testClassFile() throws Exception {
    ClassFile cf = new ClassFile();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "oqube/patchwork/instrument/InstrumentClass.class");
    BytecodeInputStream bis = new BytecodeInputStream(is);
    cf.addIOListener(bis);
    cf.read(new DataInputStream(bis));
    /* read recorder */
    EventRecorder rr = bis.getRecorder();
    inst.instrument(cf);
    /* write class */
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BytecodeOutputStream bbos = new BytecodeOutputStream(bos);
    cf.addIOListener(bbos);
    cf.write(new DataOutputStream(bbos));
  }

  /*
   * test for IOSMAdapter
   */
  public void testAutomaton() throws IOException {
    ClassFile cf = new ClassFile();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "Automaton.bytes");
    BytecodeInputStream bis = new BytecodeInputStream(is);
    cf.addIOListener(bis);
    cf.read(new DataInputStream(bis));
    /* read recorder */
    EventRecorder rr = bis.getRecorder();
    cf = inst.instrument(cf);
    /* write class */
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BytecodeOutputStream bbos = new BytecodeOutputStream(bos);
    cf.addIOListener(bbos);
    cf.write(new DataOutputStream(bbos));
  }


  public void testLineNumbersPreserved() throws Exception {
    ClassFile cf = new ClassFile();
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "linesinfo.bytes");
    BytecodeInputStream bis = new BytecodeInputStream(is);
    cf.addIOListener(bis);
    cf.read(new DataInputStream(bis));
    /* read recorder */
    EventRecorder rr = bis.getRecorder();
    cf = inst.instrument(cf);
    /* check graph */
    ControlGraph cg = new ControlGraph(cf.getMethodInfo("update",
        "(Ljava/util/Map;)V"));
    // check line mapping
    int[][] lines = { { 59, 60 }, { 61, 61 }, { 64, 67 }, { 68, 70 },
        { 72, 72 }, { 74, 74 },{ 75, 76}, { 76, 77 }, { 76, 76 }, { 74, 74 },
       { 79, 80 } };
 for (BasicBlock bb : cg.getBlocks()) {
      System.err.print(bb.getNumBlock() + "{" + bb.getStartLine() + ","
          + bb.getEndLine() + "},");
      assertEquals(lines[bb.getNumBlock() - 1][0], bb.getStartLine());
      assertEquals(lines[bb.getNumBlock() - 1][1], bb.getEndLine());
    }

  }

  /**
   * Check discrepancies between lists.
   * 
   * @param rr
   * @param wr
   */
  private void assertLists(List events, List events2) {
    List discrepancies = new ArrayList();
    Iterator i = events.iterator();
    Iterator j = events2.iterator();
    int k = 0;
    while (true) {

      if (!(i.hasNext() & j.hasNext()))
        break;
      Object o1 = i.next();
      Object o2 = j.next();
      if (!(o1.equals(o2)))
        discrepancies.add("At " + k + ": expected " + o1 + ", found " + o2);
      k++;
    }
    System.err.println(discrepancies);
    if (!discrepancies.isEmpty())
      throw new AssertionFailedError("Found discrepancies: " + discrepancies);
    if (i.hasNext() ^ j.hasNext()) {
      throw new AssertionFailedError(
          "List do not have the same sizes: expected " + events.size()
              + " found " + events2.size());
    }
  }

}
