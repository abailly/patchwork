/**
 * 
 */
package oqube.bytes.loading;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import oqube.bytes.ClassFile;

/**
 * A factory that creates classFile objects from the given ClassLoader.
 * 
 * @author nono
 * 
 */
public class ClassLoaderClassFileFactory implements ClassFileFactory {

	private ClassLoader loader;

	/**
	 * Constructor with default class loader.
	 * This constructor uses either this classes class loader or 
	 * the thread's context class loader.
	 *
	 */
	public ClassLoaderClassFileFactory() {
		loader = getClass().getClassLoader();
		if (loader == null)
			loader = Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Constructor with a specific class loader.
	 * 
	 * @param loader the class loader to load classes from. May not be null.
	 */
	public ClassLoaderClassFileFactory(ClassLoader loader) {
		this.loader = loader;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.bytes.loading.ClassFileFactory#getClassFileFor(java.lang.String)
	 */
	public ClassFile getClassFileFor(String className) throws IOException {
		String fname = className.replace('.', '/') + ".class";
		InputStream is = loader.getResourceAsStream(fname);
		if (is != null) {
			ClassFile cf = new ClassFile();
			try {
				cf.read(new DataInputStream(is));
				return cf;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

}
