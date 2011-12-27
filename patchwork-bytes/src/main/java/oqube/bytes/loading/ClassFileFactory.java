/**
 * 
 */
package oqube.bytes.loading;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import oqube.bytes.ClassFile;

/**
 * An interface for creating ClassFile objects from class names. Instances of
 * this interface provide useful methods for loading ClassFile definitions from
 * their fully qualified class names.
 * 
 * @author nono
 */
public interface ClassFileFactory {

	/**
	 * Create a ClassFile from the class name.
	 * 
	 * @param clname
	 *            class name as defined in the Java Language Specification.
	 * @return a ClassFile constructed from the bytecode definition of this
	 *         class.
	 * @throws IOException
	 *             if bytecode cannot be read.
	 */
	ClassFile getClassFileFor(String clname) throws IOException;
}
