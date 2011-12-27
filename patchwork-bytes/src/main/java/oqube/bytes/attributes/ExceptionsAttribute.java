package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.ClassData;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * @author abailly
 */
public class ExceptionsAttribute extends AttributeFileInfo {

	// list of exceptions index in this attribute
	private List exceptions = new ArrayList();

	/**
	 *  name of this attribute
	 */
	public static final String attributeName = "Exceptions";
	
	/**
	 * default constructor for use by AttributeFileInfo in reading
	 * class files
	 */
	public ExceptionsAttribute() {}
	
	/**
	 * construct an exceptionsAttribute within context of the given
	 * class file
	 */
	public ExceptionsAttribute(ClassFile cf) {
		super(cf,attributeName);
	}
	
	/**
	 * Adds a new exception to this ExceptionsAttribute denoted
	 * by class name
	 * 
	 * @param clname fully qualified class name of this exception
	 */
	public void addException(String clname) {
		short cli = ClassData.create(classFile.getConstantPool(),clname);
		exceptions.add(new Short(cli));
	}

	/**
	 * Retrieve the name of an exception given its position
	 * 
	 * @param index index of exception in ExceptionsAttribute list
	 * @return the fully qualified name of this exception
	 */
	public String getException(int index) {
		short cli = ((Short)exceptions.get(index)).shortValue();
		return classFile.getConstantPool().getEntry(cli).toString();
	}
	
	/**
	 * @see fr.norsys.klass.ClassFileComponent#write(DataOutputStream)
	 */
	public void write(DataOutputStream out) throws IOException {
       out.writeShort(nameIndex);
		out.writeInt(length);
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.ATOMIC, "attribute size="+getLength()));
		int esz = exceptions.size();
         classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                    ClassFileIOEvent.START, "Exceptions"));
            out.writeShort((short)esz);
		Iterator it = exceptions.iterator();
		while(it.hasNext()) {
			short idx = ((Short)it.next()).shortValue();
			out.writeShort(idx);
		}
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.END, "Exceptions"));
	}

	/**
	 * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.START, "Exceptions"));
		short esz = in.readShort();
		for(int i =0;i<esz;i++)
			exceptions.add(new Short(in.readShort()));
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.END, "Exceptions"));
	}

}





