/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * Attribute for enclosing method in local classes and anonymous classes. Local
 * classes are classes defined within the body of a method, anonymous classes
 * are classe defined through anonymous subclassing construct.
 * 
 * @author nono
 * 
 */
public class EnclosingMethodAttribute extends AttributeFileInfo implements
		Constants {

	/**
	 * String constant designating this attribute in the constant pool
	 */
	public static final String attributeName = "EnclosingMethod";

	/*
	 * index for enclosing class
	 */
	private short classIndex;

	/*
	 * index for enclosing method name and type maybe 0
	 */
	private short methodIndex;

	public EnclosingMethodAttribute(ClassFile cf) {
		super(cf, attributeName);
	}

	public EnclosingMethodAttribute() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.bytes.AttributeFileInfo#write(java.io.DataOutputStream)
	 */
	@Override
	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(nameIndex);
		dos.writeInt(getLength());
		dos.writeShort(classIndex);
		dos.writeShort(methodIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.bytes.ClassFileComponent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
		classIndex = in.readShort();
		methodIndex = in.readShort();
	}

	/**
	 * @return Returns the classIndex.
	 */
	public short getClassIndex() {
		return classIndex;
	}

	/**
	 * @param classIndex
	 *            The classIndex to set.
	 */
	public void setClassIndex(short classIndex) {
		this.classIndex = classIndex;
	}

	/**
	 * @return Returns the methodIndex.
	 */
	public short getMethodIndex() {
		return methodIndex;
	}

	/**
	 * @param methodIndex
	 *            The methodIndex to set.
	 */
	public void setMethodIndex(short methodIndex) {
		this.methodIndex = methodIndex;
	}

  /* (non-Javadoc)
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return 4;
  }

}
