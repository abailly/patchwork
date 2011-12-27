/*
 * $Log: ClassFile.java,v $
 * Revision 1.8  2004/06/29 15:27:00  bailly
 * correction attribut LineNumberTable et SourceFile
 * correction null pointer sur ClassFile
 * correction calcul de la longueur dans CodeAttribute
 *
 * Revision 1.7  2004/06/23 13:42:02  bailly
 * debut  XMLIzer
 *
 * Revision 1.6  2004/05/10 15:59:55  bailly
 * started coverage analyzer from jpda
 *
 * Revision 1.5  2004/05/09 22:09:24  bailly
 * Added frame display capability to ControlGraph display
 * Corrected control graph construction
 * added exceptions block handling, tableswitch and lookupswitch
 * TODO : correct implementation of branch splitting and ordering of blocks
 *
 * Revision 1.4  2004/05/06 14:08:01  bailly
 * changed magic number handling  to account for different versions
 * 
 */
package oqube.bytes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.events.ClassFileIOListener;
import oqube.bytes.pool.ConstantPool;
import oqube.bytes.struct.AttributeFileInfo;
import oqube.bytes.struct.ClassFileComponent;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.FieldFileInfo;
import oqube.bytes.struct.MethodFileInfo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * This is the main class for manipulating instances of class definitions. A ClassFile object can be constructed either
 * from scratch, using default constructor and adding elements one at a time, or from an already existing class
 * definition, using either one of parameterized constructors or reading from a DataInputStream
 * <p>
 * It can then be manipulated through the offered functions, adding and removing fields, methodsInfo and attributes.
 * <p>
 * 
 * @author Arnaud Bailly
 * @version 13082002
 */
public class ClassFile implements ClassFileComponent {

  // //////////////////////////////////////////////
  // FIELDS
  // /////////////////////////////////////////////

  // versions
  private short minor = JAVA_DEFAULT_MINOR_VERSION;

  private short major = JAVA_DEFAULT_VERSION;

  // class file constant table
  private ConstantPool constantPool = new ConstantPool();

  // class file class info
  private ClassFileInfo classFileInfo;

  // class file fields
  private List<FieldFileInfo> fieldsInfo = new ArrayList<FieldFileInfo>();

  // class file methods
  private Multimap<String,MethodFileInfo> methodsInfo = LinkedHashMultimap.<String,MethodFileInfo> create();

  // class file attributes
  private List<AttributeFileInfo> attributesInfo = new ArrayList<AttributeFileInfo>();

  private Set listeners = new HashSet();

  // //////////////////////////////////////////////
  // CONSTRUCTORS
  // /////////////////////////////////////////////

  // //////////////////////////////////////////////
  // PUBLIC METHODS
  // /////////////////////////////////////////////

  /**
   * Get the ConstantPool object associated with this ClassFile
   * 
   * @return a ConstantPool instance
   */
  public ConstantPool getConstantPool() {
    return constantPool;
  }

  /**
   * Set the ConstantPool object associated with this ClassFile
   * 
   * @return a ConstantPool instance
   */
  public void setConstantPool(ConstantPool cpool) {
    this.constantPool = cpool;
  }

  /**
   * Defines the ClassFileInfo associated with this ClassFile (name of class, parent class, implemented interfaces,
   * access flags)
   * 
   * @param cf
   *          the ClassFileInfo object
   */
  public void setClassFileInfo(ClassFileInfo cf) {
    this.classFileInfo = cf;
  }

  /**
   * Adds a new FieldFileInfo to this ClassFile.
   * 
   * @param ff
   *          a FieldFileInfo object
   * @exception ClassFileException
   *              if the already exists in this ClassFile
   */
  public void add(FieldFileInfo ff) {
    fieldsInfo.add(ff);
  }

  /**
   * Adds a new MethodFileInfo to this ClassFile.
   * 
   * @param ff
   *          a MethodFileInfo object
   * @exception ClassFileException
   *              if the method already exists in this ClassFile
   */
  public void add(MethodFileInfo ff) {
    methodsInfo.put(ff.getName(), ff);
  }

  /**
   * Adds a new AttributeFileInfo to this ClassFile.
   * 
   * @param ff
   *          an AttributeFileInfo object
   * @exception ClassFileException
   *              if the method already exists in this ClassFile
   */
  public void add(AttributeFileInfo ff) {
    if (ff == null) return;
    attributesInfo.add(ff);
  }

  /**
   * Writes the ClassFile object to a DataOutputStream supplied. Overrides this method in ClassFileComponent.
   * 
   * @param out
   *          a DataOutputStream object
   * @exception IOException
   *              if underlying stream throws exception
   */
  public void write(DataOutputStream out) throws IOException {
    Iterator fit = null, mit = null, ait = null;
    // class file header as given by JVM Spec
    // magic
    out.writeInt(JAVA_MAGIC);
    // version
    out.writeShort(minor);
    out.writeShort(major);
    // write constant pool
    dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.START, "ConstantPool"));
    constantPool.write(out);
    dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.END, "ConstantPool"));
    // write class info
    classFileInfo.write(out);
    // write field and method numbers
    // write all fields
    if (fieldsInfo != null) {
      fit = fieldsInfo.iterator();
      out.writeShort(fieldsInfo.size());
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Field count="
        + fieldsInfo.size()));
      while (fit.hasNext())
        ((FieldFileInfo) fit.next()).write(out);
    } else {
      // write all methods
      out.writeShort(0);
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Field count=0"));
    }
    if (methodsInfo != null) {
      mit = methodsInfo.values().iterator();
      out.writeShort(methodsInfo.size());
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Method count="
        + methodsInfo.size()));
      while (mit.hasNext())
        ((MethodFileInfo) mit.next()).write(out);
    } else {
      out.writeShort(0);
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Method count=0"));
    }
    // write all attributes
    if (attributesInfo != null) {
      ait = attributesInfo.iterator();
      out.writeShort(attributesInfo.size());
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Attributes count="
        + attributesInfo.size()));
      while (ait.hasNext())
        ((AttributeFileInfo) ait.next()).write(out);
    } else {
      out.writeShort(0);
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.WRITE, ClassFileIOEvent.ATOMIC, "Attributes count=0"));
    }
  }

  /**
   * Reads definition of a class from a DataInputStream.
   * 
   * @param in
   *          input stream to read from
   * @exception IOException
   *              if stream throws exception
   */
  public void read(java.io.DataInputStream in) throws java.io.IOException {
    // read header
    int magic = in.readInt();
    if (magic != JAVA_MAGIC) throw new IOException("Bad MAGIC Number" + magic);
    minor = in.readShort();
    major = in.readShort();
    if ((major > JAVA_MAX_SUPPORTED_VERSION)
      || ((major == JAVA_MAX_SUPPORTED_VERSION) && (minor > JAVA_MAX_SUPPORTED_MINOR_VERSION))) throw new IOException(
      "Unsupported JVM Version : " + major + "." + minor);
    // read constant pool
    ConstantPool cpool = new ConstantPool();
    dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.READ, ClassFileIOEvent.START, "ConstantPool"));
    cpool.read(in);
    dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.READ, ClassFileIOEvent.END, "ConstantPool"));
    setConstantPool(cpool);
    // read class info
    ClassFileInfo cfi = new ClassFileInfo();
    cfi.setClassFile(this);
    cfi.read(in);
    setClassFileInfo(cfi);
    // read fields info
    int fcount = 0;
    try {
      fcount = in.readShort();
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.READ, ClassFileIOEvent.ATOMIC, "Field count=" + fcount));
    } catch (java.io.EOFException ex) {
      return;
    }
    for (int i = 0; i < fcount; i++) {
      FieldFileInfo ffi = new FieldFileInfo();
      ffi.setClassFile(this);
      ffi.read(in);
      add(ffi);
    }
    // read methods info
    try {
      fcount = in.readShort();
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.READ, ClassFileIOEvent.ATOMIC, "Method count=" + fcount));
    } catch (java.io.EOFException ex) {
      return;
    }
    for (int i = 0; i < fcount; i++) {
      MethodFileInfo mfi = new MethodFileInfo();
      mfi.setClassFile(this);
      mfi.read(in);
      add(mfi);
    }
    // read atttributes info
    try {
      fcount = in.readShort();
      dispatch(new ClassFileIOEvent(this, ClassFileIOEvent.READ, ClassFileIOEvent.ATOMIC, "Attributes count=" + fcount));
    } catch (java.io.EOFException ex) {
      return;
    }
    for (int i = 0; i < fcount; i++) {
      AttributeFileInfo ffi = AttributeFileInfo.read(this, in);
      add(ffi);
    }

  }

  /**
   * Returns the classFileInfo.
   * 
   * @return ClassFileInfo
   */
  public ClassFileInfo getClassFileInfo() {
    return classFileInfo;
  }

  /**
   * @param string
   */
  public Collection<MethodFileInfo> getMethodInfo(String string) {
    return methodsInfo.get(string);
  }

  /**
   * @param string
   */
  public MethodFileInfo getMethodInfo(String string, String signature) {
    Collection<MethodFileInfo> meths = methodsInfo.get(string);
    if (meths == null) return null;
    Iterator it = meths.iterator();
    while (it.hasNext()) {
      MethodFileInfo mfi = (MethodFileInfo) it.next();
      if (mfi.getSignature().equals(signature)) return mfi;
    }
    return null;
  }

  /**
   * 
   */
  public Collection<MethodFileInfo> /* methods info */getAllMethods() {
    return methodsInfo.values();
  }

  /**
   * 
   */
  public Collection<FieldFileInfo> getAllFields() {
    return fieldsInfo;
  }

  /**
   * Adds an IOLIstener to this classfile. The listener will be notified of all I/O events occuring in this class file.
   * 
   * @param l
   *          listener to add. May not be null.
   */
  public void addIOListener(ClassFileIOListener l) {
    this.listeners.add(l);
  }

  /**
   * Removes an IOListener from this classfile listeners' set.
   * 
   * @param l
   *          may not be null.
   */
  public void removeListener(ClassFileIOListener l) {
    this.listeners.remove(l);
  }

  /**
   * Notifies all registered listeners of the occurence of an event. This method is made package accessible so that
   * other components of a classfile may access it.
   * 
   * @param e
   *          the event to throw.
   */
  public void dispatch(ClassFileIOEvent e) {
    for (Iterator i = listeners.iterator(); i.hasNext();) {
      ((ClassFileIOListener) i.next()).notify(e);
    }
  }

  /**
   * Return the bytecode generated from this classfile.
   * 
   * @return
   * @throws IOException
   */
  public byte[] getBytes() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    this.write(dos);
    dos.flush();
    dos.close();
    return bos.toByteArray();
  }

  /**
   * Split a signature string into its constituent parts and fills the list argument.
   * 
   * @param sig
   *          java standard signature
   * @param ret
   *          a List where parsed arguments will be stored. May be null.
   * @return a List<String> of arguments' types plus return type. The strings correspond to the standard internal
   *         encoding of the JVM.
   */
  public static List parseSignature(String sig, List ret) {
    if (ret == null) ret = new ArrayList();
    int i = 0;
    int j = 0;
    int ln = sig.length();
    StringBuffer arr = new StringBuffer();
    while (i < ln) {
      switch (sig.charAt(i)) {
        case '(':
          i++;
          break;
        case 'L':
          j = sig.indexOf(';', i);
          arr.append(sig.substring(i, j + 1));
          ret.add(arr.toString());
          arr.delete(0, arr.length());
          i = j + 1;
          break;
        case ')':
          i++;
          break;
        case '[':
          i++;
          arr.append("[");
          break;
        default:
          arr.append(sig.charAt(i));
          ret.add(arr.toString());
          arr.delete(0, arr.length());
          i++;
      }
    }
    return ret;
  }

  public List<AttributeFileInfo> getAttribute(String name) {
    List<AttributeFileInfo> attrs = new ArrayList<AttributeFileInfo>();
    for (AttributeFileInfo at : attributesInfo)
      if (at.getName().equals(name)) attrs.add(at);
    return attrs;
  }

  /**
   * A method for constructing ClassFile instances from class objects. This method extracts the input stream for the
   * given class object from the classpath, parses it and returns the associated ClassFile object. This method may not
   * be safe for inner types.
   * 
   * @param klass
   *          the Class instance to reify.
   * @return a ClassFile object or null.
   * @throws IOException
   */
  public static ClassFile reify(Class<?> klass) throws IOException {
    if (klass.isArray() || klass.isPrimitive()) return null;
    String in = TypeHelper.getInternalName(klass);
    InputStream is = klass.getClassLoader().getResourceAsStream(in.substring(1, in.length() - 1) + ".class");
    if (is == null) return null;
    ClassFile cf = new ClassFile();
    cf.read(new DataInputStream(is));
    return cf;
  }

  /**
   * Static factory method for constructing a classfile from a stream. This method wraps the given stream in input
   * stream, constructs a new class file instance and returns it.
   * 
   * @param is
   *          the stream to parse. May not be null.
   * @return a ClassFile object or null.
   */
  public static ClassFile makeClassFile(InputStream is) {
    ClassFile cf = new ClassFile();
    try {
      cf.read(new DataInputStream(is));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return cf;
  }

  /**
   * @return a string representation of the attributes of this class object (eg. access, abstractness, interface/class).
   */
  public String typeOfClass() {
    StringBuilder sb = new StringBuilder();
    short flags = classFileInfo.getFlags();
    if ((flags & Constants.ACC_PUBLIC) > 0) {
      sb.append("public");
    } else if ((flags & Constants.ACC_PRIVATE) > 0) {
      sb.append("private");
    } else if ((flags & Constants.ACC_PROTECTED) > 0) {
      sb.append("protected");
    }
    sb.append(" ");
    if ((flags & Constants.ACC_STATIC) > 0) sb.append("static");
    sb.append(" ");
    if ((flags & Constants.ACC_FINAL) > 0) sb.append("final");
    sb.append(" ");
    if ((flags & Constants.ACC_INTERFACE) > 0) {
      sb.append("interface");
    } else sb.append("class");
    return sb.toString();
  }
}
