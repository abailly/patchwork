/*______________________________________________________________________________
 * 
 * Copyright 2006 Arnaud Bailly - NORSYS/LIFL
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
 * Created on Jan 3, 2006
 *
 */
package oqube.patchwork.instrument;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.loading.ByteArrayClassLoader;
import oqube.bytes.pool.MethodRefData;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.report.Coverage;
import oqube.patchwork.report.CoverageInfo;
import oqube.bytes.utils.TemporaryFS;
import junit.framework.TestCase;

public class CoverageGeneratorTest extends TestCase {

  private InstrumentClass inst;

  private CoverageGenerator cgen;

  private TemporaryFS temp;

  public void dummy() {
    String[][] strs = new String[1][];
    strs[0] = new String[2];
    strs[0][0] = "toto";
    strs[0][1] = "titi";
  }

  protected void setUp() throws Exception {
    super.setUp();
    this.temp = new TemporaryFS("tmp" + new Date().getTime());
    inst = new InstrumentClass();
    ClassFile cf = new ClassFile();
    ClassFileInfo cfi = new ClassFileInfo(cf, "test/Sample");
    cfi.setParent("java/lang/Object");
    cf.setClassFileInfo(cfi);
    /* dummy code */
    MethodFileInfo mfi = new MethodFileInfo(cf);
    mfi.setName("test");
    mfi.setType("(II)I");
    mfi.setPublic();
    CodeAttribute ca = new CodeAttribute(cf, (short) 3);
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
    mfi.addAttribute(ca);
    cf.add(mfi);
    /* ctor */
    mfi = new MethodFileInfo(cf);
    mfi.setName("<init>");
    mfi.setType("()V");
    mfi.setPublic();
    ca = new CodeAttribute(cf, (short) 1);
    seq = new Sequence(cf);
    short sup = MethodRefData.create(cf.getConstantPool(), "java/lang/Object",
        "<init>", "()V");
    seq._aload_0()._invokespecial(sup, cf)._return();
    ca.add(seq);
    mfi.addAttribute(ca);
    cf.add(mfi);
    inst.instrument(cf);
    cgen = new CoverageGenerator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    temp.clean();
    super.tearDown();
  }

  /*
   * basic testing of static initializers generation
   */
  public void testBase() throws IOException {
    List l = cgen.generate(inst);
    ClassFile cf = (ClassFile) l.get(0);
    /* check classfile code structure */
    MethodFileInfo mfi = cf.getMethodInfo("<init>", "()V");
    CodeAttribute code = mfi.getCodeAttribute();
    List seq = code.getAllInstructions();
    assertEquals(Opcodes.opc_aload_0, ((Instruction) seq.get(0)).opcode());
  }

  /*
   * test loading of generated coverage class
   */
  public void testClassLoader() throws IOException, InstantiationException,
      IllegalAccessException, SecurityException, NoSuchMethodException,
      IllegalArgumentException, InvocationTargetException,
      ClassNotFoundException {
    List l = cgen.generate(inst);
    ClassFile cf = (ClassFile) l.get(0);
    ByteArrayClassLoader bcl = new ByteArrayClassLoader(Thread.currentThread()
        .getContextClassLoader());
    bcl.setRootDir("/tmp");
    bcl.setWrite(true);
    byte[] bytes = cf.getBytes();
    bcl.putBytes("oqube.patchwork.report.CoverageInfoImpl", bytes);
    bcl.putBytes("test.Sample", ((ClassFile) inst.getClasses().get(0))
        .getBytes());
    Class cov = bcl.loadClass("oqube.patchwork.report.CoverageInfoImpl");
    CoverageInfo ci = (CoverageInfo) cov.newInstance();
    assertEquals(1, ci.getClasses().length);
    assertEquals("test/Sample", ci.getClasses()[0]);
    Coverage.setCoverageInfo(ci);
    Coverage.setBackend(new FileBackend(temp.root()));
    Class ins = bcl.loadClass("test.Sample");
    Object o = ins.newInstance();
    Method m = ins.getDeclaredMethod("test",
        new Class[] { int.class, int.class });
    m.invoke(o, new Object[] { new Integer(1), new Integer(2) });
    /* close output */
    Coverage.closeOutput();
  }
}
