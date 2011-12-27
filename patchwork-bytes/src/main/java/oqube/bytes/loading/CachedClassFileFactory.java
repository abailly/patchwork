/**
 * 
 */
package oqube.bytes.loading;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import oqube.bytes.ClassFile;

/**
 * A factory that caches the created ClassFile objects. The created ClassFile
 * objects are cached into a LRU queue whose size can be defined at factory
 * construction (defaults to 1000). This factory loads class file data from its
 * class loader or the context class loader of its creating thread, using the
 * class
 * 
 * @author nono
 * 
 */
public class CachedClassFileFactory extends CompositeClassFileFactory {

	private Map<String, ClassFile> classMap;

	private ClassLoader loader;

	public CachedClassFileFactory() {
		add(new ClassLoaderClassFileFactory());
		this.classMap = new HashMap<String, ClassFile>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.bytes.loading.ClassFileFactory#makeClassFile(java.lang.String)
	 */
	public ClassFile getClassFileFor(String clname){
		ClassFile cf = classMap.get(clname);
		if (cf != null)
			return cf;
		cf = super.getClassFileFor(clname);
		if (cf != null)
			classMap.put(clname, cf);
		return cf;
	}

}
