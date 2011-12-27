package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.Constants;

public class MethodRefData extends AbstractMethodData implements Constants {

	private MethodRefData(ConstantPool tbl, short cli, short nti) {
		this.classIndex = cli;
		this.nameAndTypeIndex = nti;
		tbl.add(this);

	}

	MethodRefData() {
	}

	/**
	 * Constructs a new TypeAndNameData object which references the given string
	 * in the given ConstantPool.
	 */
	public static short create(ConstantPool tbl, String cl, String name,
			String type) {
		MethodRefData data;
		/* verifies the uniqueness of the string */
		if ((data = tbl.getMethodRefData(cl + '_' + name + '_' + type)) == null) {
			short dt = ClassData.create(tbl, cl);
			short nt = NameAndTypeData.create(tbl, name, type);
			data = new MethodRefData(tbl, dt, nt);
		}
		return data.getIndex();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Object o = getConstantPool().getEntry(classIndex);
		if (o == null)
			sb.append("??");
		else
			sb.append(o.toString());
		o = getConstantPool().getEntry(nameAndTypeIndex);
		sb.append("_");
		if (o == null)
			sb.append("??");
		else
			sb.append(o.toString());
		return sb.toString();
	}

	/**
	 * Writes this TypeAndNameData to the given Stream
	 */
	public void write(java.io.DataOutputStream os) throws java.io.IOException {
		os.writeByte(CONSTANT_METHOD);
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

}
