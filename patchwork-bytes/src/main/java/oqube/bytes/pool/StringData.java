package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.Constants;

/**
 * This class represents a String constant data, that is an entry
 * of type String in the Constant Pool which references a UTF8 string
 */
public class StringData extends ConstantData implements Constants {

	/* (non-Javadoc)
   * @see oqube.bytes.pool.ConstantData#getValue()
   */
  @Override
  public Object getValue() {
    return toString();
  }

  // index of UTFData referenced from this String constant
	private short utfIndex;

	private StringData(ConstantPool tbl, short utfIndex) {
		this.utfIndex = utfIndex;
		tbl.add(this);

	}

	StringData() {
	}

	/**
	 * Constructs a new StringData object which references the given string
	 * in the given ConstantPool.
	 */
	public static short create(ConstantPool tbl, String s) {
		StringData data;
		/* verifies the uniqueness of the string */
		if ((data = tbl.getStringData(s)) == null)
			// creates a new UTF constant (or retrieves an existing one)
			data = new StringData(tbl, UTFData.create(tbl, s));
		return data.getIndex();
	}

	public String toString() {
		return getConstantPool().getEntry(utfIndex).toString();
	}

	/**
	 * Writes this StringData to the given Stream
	 */
	public void write(java.io.DataOutputStream os) throws java.io.IOException {
		os.writeByte(CONSTANT_STRING);
		os.writeShort(utfIndex);
	}
	/**
	 * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
		// read index of utf data
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
