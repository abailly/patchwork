/*
 * Created on Jun 23, 2004
 * 
 * $Log: LocalVariableAttribute.java,v $
 * Revision 1.1  2004/06/23 13:42:02  bailly
 * debut  XMLIzer
 *
 */
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

/**
 * Representation of attributes for local variables in a code
 * 
 * @author nono
 * @version $Id: LocalVariableAttribute.java,v 1.1 2004/06/23 13:42:02 bailly
 *          Exp $
 */
public class LocalVariableTableAttribute extends AttributeFileInfo {

  private List<VariableInfo> variables = new ArrayList<VariableInfo>();

  /**
   * name of this attribute
   */
  public static final String attributeName = "LocalVariableTable";

  public LocalVariableTableAttribute() {
  }

  public LocalVariableTableAttribute(ClassFile cf) {
    super(cf, attributeName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#write(java.io.DataOutputStream)
   */
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(nameIndex);
    out.writeInt(getLength());
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "attribute size=" + getLength()));
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "LocalVariable"));
    int esz = variables.size();
    out.writeShort((short) esz);
    for (VariableInfo v : variables)
      v.write(out);
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "LocalVariable"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "LocalVariable"));
    short esz = in.readShort();
    for (int i = 0; i < esz; i++) {
      VariableInfo vi = new VariableInfo();
      vi.read(in);
      variables.add(vi);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "LocalVariable"));
  }

  /**
   * Returns the name of a variable at given pc and with given index
   * 
   * @param pc
   * @param index
   * @return
   */
  public String getVariableAt(int pc, int index) {
    /* lookup variables */
    Iterator it = variables.iterator();
    while (it.hasNext()) {
      VariableInfo vi = (VariableInfo) it.next();
      if ((vi.index == index) && (pc >= vi.start_pc)
          && (pc <= vi.start_pc + vi.length)) {
        String name = classFile.getConstantPool().getEntry(vi.name_index)
            .toString();
        return name;
      }
    }
    return null;
  }

  /**
   * Return an iterator over the local variable table.
   * 
   * @return
   */
  public Iterator iterator() {
    return variables.iterator();
  }

  /**
   * Retrieve the name of the given variable info structure.
   * 
   * @param info
   * @return
   */
  public String getVariableName(VariableInfo info) {
    return classFile.getConstantPool().getEntry(info.name_index).toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return 2 + variables.size() * VariableInfo.LENGTH;
  }

}