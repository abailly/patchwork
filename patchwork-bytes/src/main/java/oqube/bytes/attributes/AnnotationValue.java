/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.pool.ClassData;
import oqube.bytes.struct.ClassFileComponent;

/**
 * @author nono
 * 
 */
public class AnnotationValue extends AbstractAnnotation {

  /*
   * UTF-8 constant
   */
  private short typeIndex;

  class ElementNameValue implements ClassFileComponent {

    short nameIndex;

    ElementValue value;

    public void write(DataOutputStream out) throws IOException {
      out.writeShort(nameIndex);
      value.write(out);
    }

    public void read(DataInputStream in) throws IOException {
      nameIndex = in.readShort();
      value = new ElementValue(classFile);
      value.read(in);
    }

    public int getLength() {
      return 2 + value.getLength();
    }

  }

  private List<ElementNameValue> elements = new ArrayList<ElementNameValue>();

  private ClassFile classFile;

  public AnnotationValue(ClassFile cf) {
    this.classFile = cf;
  }

  public AnnotationValue() {
  }

  public void write(DataOutputStream out) throws IOException {
    out.writeShort(typeIndex);
    out.writeShort(elements.size());
    for (ElementNameValue val : elements)
      val.write(out);
  }

  public void read(DataInputStream in) throws IOException {
    typeIndex = in.readShort();
    int ln = in.readShort();
    elements = new ArrayList<ElementNameValue>(ln);
    for (int i = 0; i < ln; i++) {
      ElementNameValue val = new ElementNameValue();
      val.read(in);
      elements.add(val);
    }
  }

  public int getLength() {
    int sum = 4; // type index + 2-bytes length
    for (ElementNameValue val : elements)
      sum += val.getLength();
    return sum;
  }

  /**
   * Extract name of type of annotations.
   * @return the name of this annotation's type.
   */
  public String getAnnotationName() {
    return classFile.getConstantPool().getEntry(typeIndex).toString();
  }
  
  public Map<String, Object> getElementsMap() {
    Map<String,Object> ret= new HashMap<String, Object>();
    for(ElementNameValue val : elements) {
      String name = classFile.getConstantPool().getEntry(val.nameIndex).toString();
      Object value = val.value.getValue();
      ret.put(name, value);
    }
    return ret;
  }
}
