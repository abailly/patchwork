package oqube.bytes.pool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract parent class of references to methods in constant pool.
 * This class mutualizes class and method references 
 * for interface entry and method entry.
 * 
 * @author nono
 *
 */
public abstract class AbstractMethodData extends ConstantPoolEntry {

	protected short classIndex;
	protected short nameAndTypeIndex;

	/**
	 * Returns the classIndex.
	 * 
	 * @return short
	 */
	public short getClassIndex() {
		return classIndex;
	}

	/**
	 * Returns the nameAndTypeIndex.
	 * 
	 * @return short
	 */
	public short getNameAndTypeIndex() {
		return nameAndTypeIndex;
	}

}
