package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.Constants;

/**
 * this class represents a FieldRef constant entry which references a Class
 * entry (for its container class) and a NameAndType entry for its signature
 */
public class FieldRefData extends ConstantPoolEntry implements Constants {

  // the referenced string of this TypeAndNameData object
  private short classIndex;

  private short nameAndTypeIndex;

  private FieldRefData(ConstantPool tbl, short cli, short nti) {
    this.classIndex = cli;
    this.nameAndTypeIndex = nti;
    tbl.add(this);

  }

  FieldRefData() {
  }

  /**
   * Constructs a new TypeAndNameData object which references the given string
   * in the given ConstantPool.
   */
  public static short create(ConstantPool tbl, String cl, String name,
      String type) {
    FieldRefData data;
    /* verifies the uniqueness of the string */
    if ((data = tbl.getFieldRefData(cl + '_' + name + '_' + type)) == null) {
      short dt = ClassData.create(tbl, cl);
      short nt = NameAndTypeData.create(tbl, name, type);
      data = new FieldRefData(tbl, dt, nt);
    }
    return data.getIndex();
  }

  public String toString() {
    return getConstantPool().getEntry(classIndex).toString() + ' '
        + getConstantPool().getEntry(nameAndTypeIndex).toString();
  }

  /**
   * Writes this TypeAndNameData to the given Stream
   */
  public void write(java.io.DataOutputStream os) throws java.io.IOException {
    os.writeByte(CONSTANT_FIELD);
    os.writeShort(classIndex);
    os.writeShort(nameAndTypeIndex);
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classIndex = in.readShort();
    nameAndTypeIndex = in.readShort();
  }

  /**
   * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
   */
  public void insertInPool(ConstantPool pool) {
    setConstantPool(pool);
    pool.hash(this);
  }

  /**
   * Method getNameAndTypeIndex.
   */
  public short getNameAndTypeIndex() {
    return nameAndTypeIndex;
  }

}

