/**
 * 
 */
package oqube.patchwork;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;

/**
 * A class that creates class file objects.
 * 
 * @author nono
 * 
 */
public class TestClassfileMaker {

  private CodeAttribute ca;

  public ClassFile makeTestClassfile() {
    ClassFile cf = new ClassFile();
    ClassFileInfo cfi = new ClassFileInfo(cf, "test/Sample");
    cfi.setParent("java/lang/Object");
    cf.setClassFileInfo(cfi);
    /* dummy code */
    MethodFileInfo mfi = new MethodFileInfo(cf);
    mfi.setName("test");
    mfi.setType("(II)I");
    CodeAttribute ca = new CodeAttribute(cf);
    mfi.addAttribute(ca);
    cf.add(mfi);
    this.ca = ca;
    return cf;
  }

  public CodeAttribute getCode() {
    return ca;
  }
}
