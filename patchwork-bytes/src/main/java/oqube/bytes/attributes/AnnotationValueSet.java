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
import oqube.bytes.struct.ClassFileComponent;

class AnnotationValueSet implements ClassFileComponent {

  private ClassFile classFile;

  public AnnotationValueSet(ClassFile cf) {
    this.classFile = cf;
  }
  
  public AnnotationValueSet() {
  }

  List<AnnotationValue> annotations = new ArrayList<AnnotationValue>();

  public void write(DataOutputStream out) throws IOException {
    out.writeShort(annotations.size());
    for (AnnotationValue v : annotations)
      v.write(out);
  }

  public void read(DataInputStream in) throws IOException {
    int ln = in.readShort();
    annotations = new ArrayList<AnnotationValue>(ln);
    for (int i = 0; i < ln; i++) {
      AnnotationValue val = new AnnotationValue(classFile);
      val.read(in);
      annotations.add(val);
    }
  }

  public int getLength() {
    int sum = 2; // 2-bytes length
    for(AnnotationValue v : annotations)
      sum += v.getLength();
    return sum;
  }
}