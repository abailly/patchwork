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
 * Created on Jan 2, 2006
 *
 */
package oqube.patchwork.instrument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.pool.ClassData;
import oqube.bytes.pool.MethodRefData;
import oqube.bytes.pool.StringData;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.report.CoverageInfo;

/**
 * This class generates coverage information from a given InstrumentClass
 * object.
 * <p>
 * Once a set of classes have been instrumented, this class produces a ClassFile
 * object that implements {@link CoverageInfo} interface and contains code
 * necessary to initialize the static elements of
 * {@link oqube.patchwork.report.Coverage} class.
 * </p>
 * 
 * @author nono
 * @version $Id: CoverageGenerator.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class CoverageGenerator {

  /*
   * the generated class name
   */
  private String className = "oqube/patchwork/report/CoverageInfoImpl";

  /**
   * Default constructor.
   *
   */
  public CoverageGenerator()
  {
  }
  
  /**
   * Create a generator with given classname generated.
   * 
   * @param gn full name of class to generate.
   */
  public CoverageGenerator(String gn) {
    this.className = gn;
  }

  /**
   * Generate one or more classe files that are needed for outputting coverage
   * information.
   * 
   * @param instrumented
   *          the Instrumenter
   * @return a List - not null - of generated ClassFile objects
   * @throws IOException
   */
  public List generate(InstrumentClass instrumented) throws IOException {
    List ret = new ArrayList();
    ClassFile cf = new ClassFile();
    ClassFileInfo cfi = new ClassFileInfo(cf, className);
    cfi.addInterface("oqube/patchwork/report/CoverageInfo");
    cfi.setFlags((short) (Constants.ACC_FINAL | Constants.ACC_PUBLIC));
    cfi.setParent("java/lang/Object");
    cf.setClassFileInfo(cfi);
    /* constructor */
    MethodFileInfo mfi = new MethodFileInfo(cf);
    mfi.setName("<init>");
    mfi.setType("()V");
    mfi.setFlags((short) (Constants.ACC_PUBLIC));
    cf.add(mfi);
    CodeAttribute code = new CodeAttribute(cf);
    code.setMaxlocals((short)1);
    Sequence seq = new Sequence(cf);
    short mr = MethodRefData.create(cf.getConstantPool(),"java/lang/Object","<init>","()V");
    seq._aload_0()._invokespecial(mr,cf)._return();
    /* done */
    code.add(seq);
    mfi.setCode(code);
    /*
     * method getClasses()
     */
    mfi = new MethodFileInfo(cf);
    mfi.setName("getClasses");
    mfi.setType("()[Ljava/lang/String;");
    mfi.setFlags((short) (Constants.ACC_PUBLIC));
    code = new CodeAttribute(cf);
    code.setMaxlocals((short)1);
    seq = new Sequence(cf);
    List l = instrumented.getClasses();
    /* create and initialize array of classes */
    short str = ClassData.create(cf.getConstantPool(), "java/lang/String");
    seq._sipush((short) l.size())._anewarray(str);
    /* loop over classes definitions */
    int k = 0;
    for (Iterator i = l.iterator(); i.hasNext();) {
      ClassFile icf = (ClassFile) i.next();
      /* store class name in array */
      String cn = icf.getClassFileInfo().getName();
      short sr = StringData.create(cf.getConstantPool(), cn);
      seq._dup()._sipush((short) k)._ldc_w(sr)._aastore();
      k++;
    }    
    /* return */
    seq._areturn();
    /* done */
    code.add(seq);
    mfi.setCode(code);
    cf.add(mfi);
    /*
     * method getMethods
     */
    mfi = new MethodFileInfo(cf);
    mfi.setName("getMethods");
    mfi.setType("()[[Ljava/lang/String;");
    mfi.setFlags((short) (Constants.ACC_PUBLIC));
    code = new CodeAttribute(cf);
    code.setMaxlocals((short)1);
    seq = new Sequence(cf);    
    /* create and initialize array of array of methods */
    short tr = ClassData.create(cf.getConstantPool(), "[[Ljava/lang/String;");
    seq._sipush((short) l.size())._multianewarray(tr, 1);
    k = 0;
    for (Iterator i = l.iterator(); i.hasNext();) {
      ClassFile icf = (ClassFile) i.next();
      /* create method array */
      int mn = icf.getAllMethods().size();
      seq._dup()._sipush((short) k);
      seq._sipush((short) mn)._anewarray(str);
      seq._aastore();
      /* fill method array */
      for (int j = 0; j < mn; j++) {
        String mname = (String) ((List) instrumented.getMethods().get(k))
            .get(j);
        short msr = StringData.create(cf.getConstantPool(), mname);
        /* get array */
        seq._dup()._sipush((short) k)._aaload();
        seq._sipush((short) j)._ldc_w(msr)._aastore();
      }
      k++;
    }
    seq._areturn();
    /* done */
    code.add(seq);
    mfi.setCode(code);
    cf.add(mfi);
    /* add to return */
    ret.add(cf);
    return ret;
  }

  /**
   * @return Returns the className.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @param className The className to set.
   */
  public void setClassName(String className) {
    this.className = className;
  }
}
