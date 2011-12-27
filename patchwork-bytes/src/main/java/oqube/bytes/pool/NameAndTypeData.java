package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.Constants;

/**
 * This class represents a NameAndType constant pool entry, which points to two
 * UTF8 entries : - one for the name - one for the type (in java standardized
 * form)
 */
public class NameAndTypeData extends ConstantPoolEntry implements Constants {

	// the referenced string of this NameAndTypeData object
	private short nameIndex, typeIndex;

	private NameAndTypeData(ConstantPool tbl, short name, short type) {
		this.nameIndex = name;
		this.typeIndex = type;
		tbl.add(this);

	}

	NameAndTypeData() {
	}

	/**
	 * Constructs a new NameAndTypeData object which references the given string
	 * in the given ConstantPool.
	 */
	static short create(ConstantPool tbl, String name, String type) {
		NameAndTypeData data;
		/* verifies the uniqueness of the string */
		if ((data = tbl.getNameAndTypeData(name + type)) == null)
			data = new NameAndTypeData(tbl, UTFData.create(tbl, name), UTFData
					.create(tbl, type));
		return data.getIndex();
	}

	/**
	 * Returns the referenced string
	 */
	public String getName() {
		Object s = getConstantPool().getEntry(nameIndex);
		if (s == null)
			return "null";
		return s.toString();
	}

	public String getType() {
		Object s = getConstantPool().getEntry(typeIndex);
		if (s == null)
			return "null";
		return s.toString();
	}

	public String toString() {
		return getName() + getType();
	}

	/**
	 * Writes this NameAndTypeData to the given Stream
	 */
	public void write(java.io.DataOutputStream os) throws java.io.IOException {
		os.writeByte(CONSTANT_NAMEANDTYPE);
		os.writeShort(nameIndex);
		os.writeShort(typeIndex);
	}

	/**
	 * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
		nameIndex = in.readShort();
		typeIndex = in.readShort();
	}

	/**
	 * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
	 */
	public void insertInPool(ConstantPool pool) {
		setConstantPool(pool);
		pool.hash(this);
	}
}
