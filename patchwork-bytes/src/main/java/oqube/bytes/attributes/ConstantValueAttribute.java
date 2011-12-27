package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * @author abailly

 */
public class ConstantValueAttribute extends AttributeFileInfo {
	
	/**
	 * String constant designating this attribute in the constant pool
	 */
	public static final String attributeName = "ConstantValue";

	/**
	 * index of the constant entry in the constant pool
	 */
	private short constantIndex;

	/**
	 * Construct a ConstantValueAttribute in the context
	 * of the given class file
	 * 
	 * @param classFile a ClassFile object
	 */
	public	ConstantValueAttribute(ClassFile cf)
	{
		super(cf,attributeName);
	}

	/**
	 * default constructor for use by AttributeFileInfo in 
	 * reading class files
	 */
	public ConstantValueAttribute() {}	
	
	/**
	 * @see fr.norsys.klass.ClassFileComponent#write(DataOutputStream)
	 */
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(nameIndex);
		out.writeInt(length);
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.ATOMIC, "attribute size="+getLength()));
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.START, "ConstantValue"));
		out.writeShort(constantIndex);
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.END, "ConstantValue"));
	}

	/**
	 * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.START, "ConstantValue"));
		constantIndex = in.readShort();
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.END, "ConstantValue"));
	}

}
