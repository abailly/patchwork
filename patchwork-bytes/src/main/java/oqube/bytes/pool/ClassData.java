package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.Constants;

/**
 * This class represents a String constant data, that is an entry of type String
 * in the Constant Pool which references a UTF8 string
 */
public class ClassData extends ConstantData implements Constants {

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    try {
      return Class.forName(getConstantPool().getEntry(utfIndex).toString());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  // index of the utf entry reference by this class entry
  private short utfIndex;

  private ClassData(ConstantPool tbl, short utfIndex) {
    this.utfIndex = utfIndex;
    tbl.add(this);
  }

  ClassData() {
  }

  /**
   * Constructs a new ClassData object which references the given string in the
   * given ConstantPool.
   */
  public static short create(ConstantPool tbl, String s) {
    ClassData data;
    /* verifies the uniqueness of the string */
    if ((data = tbl.getClassData(s)) == null)
      // creates a new UTF constant (or retrieves an existing one)
      data = new ClassData(tbl, UTFData.create(tbl, s));
    return data.getIndex();
  }

  public String toString() {
    // System.err.println("ClassData : utfIndex ="+utfIndex);
    return getConstantPool().getEntry(utfIndex).toString();
  }

  /**
   * Writes this ClassData to the given Stream
   */
  public void write(java.io.DataOutputStream os) throws java.io.IOException {
    os.writeByte(CONSTANT_CLASS);
    os.writeShort(utfIndex);
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    utfIndex = in.readShort();
  }

  /**
   * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
   */
  public void insertInPool(ConstantPool pool) {
    setConstantPool(pool);
    pool.hash(this);
  }

}
