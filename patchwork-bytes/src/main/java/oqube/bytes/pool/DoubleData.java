package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author bailly
 * @version $Id: DoubleData.java 2 2007-03-12 09:35:36Z arnaud.oqube $
 *  */
public class DoubleData extends ConstantData {

	// this float value
	private Double f;

	private DoubleData(ConstantPool tbl, Double f) {
		this.f = f;
		tbl.add(this);
	}

	DoubleData() {
	}

	public static short create(ConstantPool tbl, Double f) {
		DoubleData data;
		/* verifies the uniqueness of the string */
		if ((data = tbl.getDoubleData(f)) == null)
			// creates a new UTF constant (or retrieves an existing one)
			data = new DoubleData(tbl, f);
		return data.getIndex();
	}

	/**
	 * Constructs a new FloatData object which references the given string
	 * in the given ConstantPool.
	 */
	public static short create(ConstantPool tbl, double fl) {
		return create(tbl, new Double(fl));
	}

	public String toString() {
		return f.toString();
	}

	public Double getDouble() {
		return f;
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
		f = new Double(in.readDouble());
	}

	/**
	 * @see fr.norsys.klass.ClassFileComponent#write(DataOutputStream)
	 */
	public void write(DataOutputStream out) throws IOException {

		out.writeByte(CONSTANT_DOUBLE);
		out.writeLong(Double.doubleToRawLongBits(f.doubleValue()));
	}

  /* (non-Javadoc)
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    return getDouble();
  }

}
