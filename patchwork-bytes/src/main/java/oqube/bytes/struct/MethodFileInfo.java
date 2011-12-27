package oqube.bytes.struct;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.UTFData;

/**
 * This class encapsulates the necessary data to generate a method in class
 * file.
 */
public class MethodFileInfo implements Constants, ClassFileComponent {

  // name
  private short nameIndex;

  // type
  private short typeIndex;

  // attributes
  private List attributes;

  // underlying class file
  private ClassFile classFile;

  // access flags
  private short flags;

  // accessors
  public CodeAttribute code() {
    if (attributes == null)
      return null;
    Iterator it = attributes.iterator();
    AttributeFileInfo info = null;
    while (it.hasNext()) {
      info = (AttributeFileInfo) it.next();
      if (info.getName().equals("code"))
        break;
      else
        info = null;
    }
    return (CodeAttribute) info;
  }

  public MethodFileInfo(ClassFile cf) {
    this.classFile = cf;
  }

  public MethodFileInfo() {
  }

  public void setCode(CodeAttribute attr) {
    if (attributes == null)
      attributes = new ArrayList();
    Iterator it = attributes.iterator();
    AttributeFileInfo info = null;
    while (it.hasNext()) {
      info = (AttributeFileInfo) it.next();
      if (info instanceof CodeAttribute) {
        it.remove();
        break;
      }
    }
    attributes.add(attr);
  }

  /**
   * Sets the name of this method
   */
  public void setName(String name) {
    this.nameIndex = UTFData.create(classFile.getConstantPool(), name);
  }

  public String getName() {
    return ((UTFData) classFile.getConstantPool().getEntry(nameIndex))
        .toString();
  }

  /**
   * Sets the type of this method
   */
  public void setType(String sig) {
    typeIndex = UTFData.create(classFile.getConstantPool(), sig);
  }

  /**
   * Sets the private flag
   */
  public void setPrivate() {
    flags |= ACC_PRIVATE;
  }

  /**
   * Sets the public flag
   */
  public void setPublic() {
    flags |= ACC_PUBLIC;
  }

  /**
   * Sets the protected flag
   */
  public void setProtected() {
    flags |= ACC_PROTECTED;
  }

  /**
   * Sets the protected flag
   */
  public void setNative() {
    flags |= ACC_NATIVE;
  }

  public void setAbstract() {
    flags |= ACC_ABSTRACT;
  }

  /**
   * Adds an attribute to this method
   */
  public void addAttribute(AttributeFileInfo info) {
    if (info == null)
      return;
    if (attributes == null)
      attributes = new LinkedList();
    attributes.add(info);
  }

  public void write(java.io.DataOutputStream dos) throws java.io.IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "MethodFileInfo"));
    dos.writeShort(flags);
    dos.writeShort(nameIndex);
    dos.writeShort(typeIndex);
    if (attributes == null)
      dos.writeShort(0);
    else {
      dos.writeShort(attributes.size());
      Iterator it = attributes.iterator();
      while (it.hasNext())
        ((AttributeFileInfo) it.next()).write(dos);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "MethodFileInfo"));
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "MethodFileInfo"));
    flags = in.readShort();
    nameIndex = in.readShort();
    typeIndex = in.readShort();
    int acount = in.readShort();
    attributes = new LinkedList();
    for (int i = 0; i < acount; i++)
      try {
        addAttribute(AttributeFileInfo.read(classFile, in));
      } catch (IOException e) {
        throw new IOException("I/O error in method "
            + classFile.getConstantPool().getEntry(nameIndex) + " : "
            + e.getMessage());
      }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "MethodFileInfo"));
  }

  /**
   * Returns the attributes.
   * 
   * @return List
   */
  public List getAttributes() {
    return attributes;
  }

  /**
   * Returns the classFile.
   * 
   * @return ClassFile
   */
  public ClassFile getClassFile() {
    return classFile;
  }

  /**
   * Returns the flags.
   * 
   * @return short
   */
  public short getFlags() {
    return flags;
  }

  /**
   * Returns the nameIndex.
   * 
   * @return short
   */
  public short getNameIndex() {
    return nameIndex;
  }

  /**
   * Returns the typeIndex.
   * 
   * @return short
   */
  public short getTypeIndex() {
    return typeIndex;
  }

  /**
   * Sets the attributes.
   * 
   * @param attributes
   *          The attributes to set
   */
  public void setAttributes(List attributes) {
    this.attributes = attributes;
  }

  /**
   * Sets the classFile.
   * 
   * @param classFile
   *          The classFile to set
   */
  public void setClassFile(ClassFile classFile) {
    this.classFile = classFile;
  }

  /**
   * Sets the flags.
   * 
   * @param flags
   *          The flags to set
   */
  public void setFlags(short flags) {
    this.flags = flags;
  }

  /**
   * Sets the nameIndex.
   * 
   * @param nameIndex
   *          The nameIndex to set
   */
  public void setNameIndex(short nameIndex) {
    this.nameIndex = nameIndex;
  }

  /**
   * Sets the typeIndex.
   * 
   * @param typeIndex
   *          The typeIndex to set
   */
  public void setTypeIndex(short typeIndex) {
    this.typeIndex = typeIndex;
  }

  /**
   * Method setStatic.
   */
  public void setStatic() {
    this.flags |= ACC_STATIC;
  }

  /**
   * Returns a string representation of the signature of this method
   * 
   */
  public String getSignature() {
    return classFile.getConstantPool().getEntry(typeIndex).toString();
  }

  /**
   * Returns code for this method or null
   * 
   * @return
   */
  public CodeAttribute getCodeAttribute() {
    Iterator it = attributes.iterator();
    while (it.hasNext()) {
      AttributeFileInfo attr = (AttributeFileInfo) it.next();
      if (attr.getName().equals("Code"))
        return (CodeAttribute) attr;
    }
    return null;
  }

  /**
   * Return a list of string representing type of arguments for this method.
   * Note that if this method is not static, then an argument of the type of
   * this method is <strong>NOT</strong> returned in first position but instead
   * a "Ljava/lang/Object;" value is added. This implies that arguments count
   * includes the object.
   * 
   * @return a List<String> of arguments' types plus return type.
   */
  public List getArguments() {
    if (isAbstract())
      return null;
    List ret = new ArrayList();
    if (isStatic())
      ret.add("Ljava/lang/Object;");
    return ClassFile.parseSignature(getSignature(), ret);
  }

  public boolean isAbstract() {
    return (flags & ACC_ABSTRACT) != 0;
  }

  public boolean isStatic() {
    return (flags & ACC_STATIC) != 0;
  }

}
