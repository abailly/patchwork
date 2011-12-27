/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.struct.AttributeFileInfo;
import oqube.bytes.struct.ClassFileComponent;

/**
 * Base class for visibel and invisible parameters annotations.
 * 
 * @author nono
 * 
 */
public class AbstractParameterAnnotationAttribute extends AttributeFileInfo {

  private List<AnnotationValueSet> parameters = new ArrayList<AnnotationValueSet>();

  public AbstractParameterAnnotationAttribute() {
  }

  public AbstractParameterAnnotationAttribute(ClassFile cf, String name) {
    super(cf, name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#write(java.io.DataOutputStream)
   */
  @Override
  public void write(DataOutputStream dos) throws IOException {
    dos.writeShort(nameIndex);
    dos.writeInt(getLength());
    dos.writeByte(parameters.size());
    for (AnnotationValueSet v : parameters)
      v.write(dos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    int ln = in.readByte();
    parameters = new ArrayList<AnnotationValueSet>(ln);
    for (int i = 0; i < ln; i++) {
      AnnotationValueSet vals = new AnnotationValueSet(classFile);
      vals.read(in);
      parameters.add(vals);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    int sum = 1; // 1-byte length
    for (AnnotationValueSet v : parameters)
      sum += v.getLength();
    return sum;
  }

}
