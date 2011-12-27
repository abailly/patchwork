/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * Attribute holding default value for an annotation.
 * 
 * @author nono
 *
 */
public class AnnotationDefaultAttribute extends AttributeFileInfo {

  private static final String attributeName = "AnnotationDefault";
  
  private ElementValue element;
  
  public AnnotationDefaultAttribute() {}
  
  public AnnotationDefaultAttribute(ClassFile cf) {
    super(cf,attributeName);
  }
  /* (non-Javadoc)
   * @see oqube.bytes.struct.AttributeFileInfo#write(java.io.DataOutputStream)
   */
  @Override
  public void write(DataOutputStream dos) throws IOException {
    dos.writeShort(nameIndex);
    dos.writeInt(getLength());
    element.write(dos);
  }

  /* (non-Javadoc)
   * @see oqube.bytes.struct.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    element = new ElementValue(classFile);
    element.read(in);
  }

  /* (non-Javadoc)
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return element.getLength();
  } 

}
