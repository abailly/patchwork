package oqube.bytes.struct;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.ClassData;

/**
 * This class encapsulates information in the class info section of a class file
 * 
 * @author Arnaud Bailly
 * @version 14082002
 */
public class ClassFileInfo implements ClassFileComponent, Constants, Cloneable {

  // //////////////////////////////////////////////
  // FIELDS
  // /////////////////////////////////////////////

  // class constant data
  private short classIndex;

  // parent class constant
  private short parentIndex;

  // the underlying classfile
  private ClassFile classFile;

  // interfaces list
  private Set interfaces;

  // access flags - default to public
  private short flags = (short) ACC_PUBLIC;

  private String name;

  // //////////////////////////////////////////////
  // CONSTRUCTORS
  // /////////////////////////////////////////////

  /**
   * constructs a ClassfileInfo with a given fully qualified name, that is a
   * name comprising all components of the class name and hierarchy
   * 
   * @param fqn
   *          full class name
   */
  public ClassFileInfo(ClassFile cf, String fqn) {
    classIndex = ClassData.create(cf.getConstantPool(), fqn);
    this.classFile = cf;
    this.name = fqn;
    parentIndex = ClassData.create(classFile.getConstantPool(),
        "java/lang/Object");
  }

  /**
   * Default constructor for ClassFileInfo. This constructor should only be used
   * by read method from ClassFile (WHY ?)
   */
  public ClassFileInfo() {
  }

  // //////////////////////////////////////////////
  // PUBLIC METHODS
  // /////////////////////////////////////////////

  /**
   * Returns the classIndex.
   * 
   * @return short
   */
  public short getClassIndex() {
    return classIndex;
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
   * Returns the parentIndex.
   * 
   * @return short
   */
  public short getParentIndex() {
    return parentIndex;
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
   * Returns the interfaces.
   * 
   * @return List
   */
  public Set getInterfaces() {
    return interfaces;
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
   * Sets the classIndex.
   * 
   * @param classIndex
   *          The classIndex to set
   */
  public void setClassIndex(short classIndex) {
    this.classIndex = classIndex;
  }

  /**
   * Sets the flags.
   * 
   * @param flags
   *          The flags to set
   */
  public void setFlags(short flags) {
    this.flags = (short) (ACC_SUPER | flags);
  }

  /**
   * Adds another implemented interface to this class
   * 
   * @param fqn
   *          fully qualified name of added interface
   */
  public void addInterface(String fqn) {
    short ifaceIndex = ClassData.create(classFile.getConstantPool(), fqn);
    if (interfaces == null)
      interfaces = new HashSet();
    interfaces.add(new Short(ifaceIndex));
  }

  /**
   * Defines the parent class for this class. By default, this is
   * java.lang.Object.
   * 
   * @param fqn
   *          fully qualified class name of parent class
   */
  public void setParent(String fqn) {
    parentIndex = ClassData.create(classFile.getConstantPool(), fqn);
  }

  /**
   * Writes the ClassFileINfo object to a DataOutputStream supplied. Overrides
   * this method in ClassFileComponent.
   * 
   * @param out
   *          a DataOutputStream object
   * @exception IOException
   *              if underlying stream throws exception
   */
  public void write(DataOutputStream out) throws java.io.IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "ClassFileInfo"));
    // write access flags
    // all oun classes are public
    out.writeShort(flags);
    // write class name index
    out.writeShort(classIndex);
    // parent - Object si null
    out.writeShort(parentIndex);
    // ifaces
    if (interfaces == null)
      out.writeShort(0);
    else {
      Iterator it = interfaces.iterator();
      out.writeShort(interfaces.size());
      while (it.hasNext())
        out.writeShort(((Short) it.next()).shortValue());
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "ClassFileInfo"));
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "ClassFileInfo"));
    flags = in.readShort();
    classIndex = in.readShort();
    parentIndex = in.readShort();
    int icount = in.readShort();
    interfaces = new HashSet();
    for (int i = 0; i < icount; i++)
      interfaces.add(new Short(in.readShort()));
    name = classFile.getConstantPool().getEntry(classIndex).toString();
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "ClassFileInfo"));
  }

  /**
   * Returns the name.
   * 
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
  public String getParentName() {
    if (parentIndex == 0)
      return getName();
    else
      return classFile.getConstantPool().getEntry(parentIndex).toString();
  }

  /**
   * Returns a set of class names corresponding to all implemented interfaces
   * 
   * @return a Set object containing fully qualified class names.
   */
  public Set<String> getInterfacesNames() {
    Set<String> ret = new HashSet<String>();
    Iterator it = interfaces.iterator();
    while (it.hasNext()) {
      Short s = (Short) it.next();
      ret.add(classFile.getConstantPool().getEntry(s.shortValue()).toString());
    }
    return ret;
  }

  public Object clone() {
    ClassFileInfo cfi;
    try {
      cfi = (ClassFileInfo) super.clone();
    } catch (CloneNotSupportedException e) {
      // NEVER HAPPENS
      return null;
    }
    cfi.interfaces = new HashSet(interfaces);
    return cfi;
  }

}
