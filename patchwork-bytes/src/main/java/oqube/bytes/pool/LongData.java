package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author bailly
 * @version $Id: LongData.java 2 2007-03-12 09:35:36Z arnaud.oqube $
 */
public class LongData extends ConstantData {

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    return l;
  }

  // this float value
  private Long l;

  private LongData(ConstantPool tbl, Long f) {
    this.l = f;
    tbl.add(this);
  }

  LongData() {
  }

  public static short create(ConstantPool tbl, Long f) {
    LongData data;
    /* verifies the uniqueness of the string */
    if ((data = tbl.getLongData(f)) == null)
      // creates a new UTF constant (or retrieves an existing one)
      data = new LongData(tbl, f);
    return data.getIndex();
  }

  /**
   * Constructs a new FloatData object which references the given string in the
   * given ConstantPool.
   */
  public static short create(ConstantPool tbl, long fl) {
    return create(tbl, new Long(fl));
  }

  public String toString() {
    return l.toString();
  }

  public Long getLong() {
    return l;
  }

  /**
   * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
   */
  public void insertInPool(ConstantPool pool) {
    setConstantPool(pool);
    pool.hash(this);
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    l = new Long(in.readLong());
  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#write(DataOutputStream)
   */
  public void write(DataOutputStream out) throws IOException {
    out.writeByte(CONSTANT_LONG);
    out.writeLong(l.longValue());
  }

}
