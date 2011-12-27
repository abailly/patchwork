package oqube.bytes.loading;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import oqube.bytes.ClassFile;


/**
 * A ClassFileHandler implementation that load generated classes
 * 
 * This ClassFileHandler object tries to load in the current JVM the 
 * byte array produced by the ClassFile objects it handles.
 * 
 * @author bailly
 * @version $Id: ClassFileLoader.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class ClassFileLoader
	extends ByteArrayClassLoader
	implements ClassFileHandler {

	/**
	 * Constructor for ClassFileLoader.
	 * @param cl
	 */
	public ClassFileLoader(ClassLoader cl) {
		super(cl);
	}

	/**
	 * @see jaskell.compiler.ClassFileHandler#handle(String, ClassFile)
	 */
	public void handle(String name, ClassFile cf) {
		try {
			/* look for class */
			Class cls = findClass(name);
            System.err.println("Found class "+cls);
			return;
		}catch(ClassNotFoundException cnex) {
			System.err.println(cnex);
		/* OK - create class */
		try {
			// create byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			cf.write(dos);
			byte[] cldef = bos.toByteArray();
			dos.close();
			bos.close();
			// try to load this class
			buildClass(name, cldef);
            System.err.println("building class "+name);
		} catch (Exception ex) {
			System.err.println(
				"Error in handling class file "
					+ name
					+ " : "
					+ ex.getMessage());
		}
		}
	}

}
