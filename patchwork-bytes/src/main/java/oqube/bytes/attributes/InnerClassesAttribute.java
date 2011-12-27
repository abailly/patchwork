package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.struct.AttributeFileInfo;
import oqube.bytes.struct.ClassFileComponent;

/**
 * 
 * <code>
 InnerClasses_attribute {
 u2 attribute_name_index;
 u4 attribute_length;
 u2 number_of_classes;
 {  u2 inner_class_info_index;        
 u2 outer_class_info_index;        
 u2 inner_name_index;      
 u2 inner_class_access_flags;      
 } classes[number_of_classes];
 }
 </code>
 * 
 * @author bailly
 * @version $Id: InnerClassesAttribute.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class InnerClassesAttribute extends AttributeFileInfo {

  public static final String attributeName = "InnerClasses";

  private List innerClasses = new ArrayList();

  public class InnerClassInfo {

    private int innerindex;

    private int outerindex;

    private int innername;

    private int innerflags;

    public InnerClassInfo(int innerindex, int outerindex, int innername,
        int innerflags) {
      this.innerindex = innerindex;
      this.outerindex = outerindex;
      this.innername = innername;
      this.innerflags = innerflags;
    }

    public void write(DataOutputStream out) throws IOException {
      out.writeShort(innerindex);
      out.writeShort(outerindex);
      out.writeShort(innername);
      out.writeShort(innerflags);
    }
  }

  /**
   * Constructor for InnerClassesAttribute.
   * 
   * @param cf
   */
  public InnerClassesAttribute(ClassFile cf) {
    super(cf, attributeName);
  }

  /**
   * Constructor for InnerClassesAttribute.
   */
  public InnerClassesAttribute() {
    super();
  }

  /**
   * @see oqube.bytes.struct.ClassFileComponent#write(DataOutputStream)
   */
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(nameIndex);
    out.writeInt(getLength());
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "attribute size=" + getLength()));
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "InnerClasses"));
    out.writeShort(innerClasses.size());
    for (Iterator i = innerClasses.iterator(); i.hasNext();) {
      ((InnerClassInfo) i.next()).write(out);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "InnerClasses"));
  }

  /**
   * @see oqube.bytes.struct.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "InnerClasses"));
    /* number of classes */
    int ncl = in.readShort();
    for (int i = 0; i < ncl; i++) {
      int innerindex = in.readShort();
      int outerindex = in.readShort();
      int innername = in.readShort();
      int innerflags = in.readShort();
      innerClasses.add(new InnerClassInfo(innerindex, outerindex, innername,
          innerflags));
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "InnerClasses"));
  }

  /**
   * @return Returns the innerClasses.
   */
  public List getInnerClasses() {
    return innerClasses;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return innerClasses.size() * 8 + 2;
  }

}
