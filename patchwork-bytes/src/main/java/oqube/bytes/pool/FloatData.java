package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

public class FloatData extends ConstantData {

	/* (non-Javadoc)
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    return f;
  }

  // this float value
	private Float f;

	private FloatData(ConstantPool tbl, Float f) {
		this.f = f;
		tbl.add(this);
	}

	FloatData() {
	}

	public static short create(ConstantPool tbl, Float f) {
		FloatData data;
		/* verifies the uniqueness of the string */
		if ((data = tbl.getFloatData(f)) == null)
			// creates a new UTF constant (or retrieves an existing one)
			data = new FloatData(tbl, f);
		return data.getIndex();
	}

	/**
	 * Constructs a new FloatData object which references the given string
	 * in the given ConstantPool.
	 */
	public static short create(ConstantPool tbl, float fl) {
		return create(tbl, new Float(fl));
	}

	public String toString() {
		return f.toString();
	}

	public Float getFloat() {
		return f;
	}

	/**
	 * Writes this FloatData to the given Stream
	 */
	public void write(java.io.DataOutputStream os) throws java.io.IOException {
		os.writeByte(CONSTANT_FLOAT);
		os.writeFloat(f.floatValue());
	}
	/**
	 * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
		f = new Float(in.readFloat());
	}

	/**
	 * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
	 */
	public void insertInPool(ConstantPool pool) {
		setConstantPool(pool);
		pool.hash(this);
	}
}
