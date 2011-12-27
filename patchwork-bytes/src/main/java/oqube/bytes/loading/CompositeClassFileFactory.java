/**
 * 
 */
package oqube.bytes.loading;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import oqube.bytes.ClassFile;

/**
 * Provide files from several other nested providers. This provider merges
 * several providers. When asked to get some stream, it tries each given
 * provider in turn until it finds one that provides the given class (ie.
 * returns non null input stream).
 * 
 * @author nono
 * 
 */
public class CompositeClassFileFactory implements ClassFileFactory {

	private List<ClassFileFactory> factories = new ArrayList<ClassFileFactory>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.bytes.loading.ClassFileProvider#getStreamFor(java.lang.String)
	 */
	public ClassFile getClassFileFor(String className) {
		ClassFile is = null;
		for (ClassFileFactory cfp : factories)
			try {
				if ((is = cfp.getClassFileFor(className)) != null)
					break;
			} catch (IOException e) {
				// OK ??
			}
		return is;
	}

	public void add(ClassFileFactory cfp1) {
		this.factories.add(cfp1);
	}

}
