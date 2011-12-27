package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

public class IntData extends ConstantData {

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    return f;
  }

  // this Int value
  private int f;

  private IntData(ConstantPool tbl, int f) {
    this.f = f;
    tbl.add(this);
  }

  IntData() {
  }

  public static short create(ConstantPool tbl, int f) {
    IntData data;
    /* verifies the uniqueness of the string */
    if ((data = tbl.getIntData(new Integer(f))) == null)
      // creates a new UTF constant (or retrieves an existing one)
      data = new IntData(tbl, f);
    return data.getIndex();
  }

  public String toString() {
    return "" + f;
  }

  public Integer getInt() {
    return new Integer(f);
  }

  /**
   * Writes this IntData to the given Stream
   */
  public void write(java.io.DataOutputStream os) throws java.io.IOException {
    os.writeByte(CONSTANT_INTEGER);
    os.writeInt(f);
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    f = in.readInt();
  }

  /**
   * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
   */
  public void insertInPool(ConstantPool pool) {
    setConstantPool(pool);
    pool.hash(this);
  }
}
