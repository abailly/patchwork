/**
 * 
 */
package oqube.patchwork.graph;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.loading.CachedClassFileFactory;
import oqube.bytes.loading.ClassFileFactory;
import oqube.bytes.loading.ClassLoaderClassFileFactory;
import oqube.bytes.struct.MethodFileInfo;

/**
 * A caching control graph builder. This builder caches the control graphs it
 * creates and may extract its data from an encapsulated class loader.
 * 
 * @author nono
 */
public class CachedControlGraphBuilder implements ControlGraphBuilder {

	/*
	 * the class provider to use
	 */
	private ClassFileFactory factory;

	/*
	 * the cached control graphs.
	 */
	private Map<String, ControlGraph> cache = new HashMap<String, ControlGraph>();

	/**
	 * Create graph builder using custom class loader.
	 * 
	 * @param instLoader
	 *            the class loader to use. May not be null.
	 */
	public CachedControlGraphBuilder(final ClassLoader instLoader) {
		this();
		((CachedClassFileFactory) this.factory)
				.add(new ClassLoaderClassFileFactory(instLoader));
	}

	/**
	 * Default constructor. USes this class's class loader.
	 * 
	 */
	public CachedControlGraphBuilder() {
		this.factory = new CachedClassFileFactory();
	}

	/**
	 * Cosntruct a control graph loader that loads class files from given
	 * provider.
	 * 
	 * @param p
	 *            a provider for class files.
	 */
	public CachedControlGraphBuilder(ClassFileFactory cf) {
		this.factory = cf;
	}

	/**
	 * @deprecated use {@link #createAllGraphs(ClassFile)} instead.
	 */
	public Map<String, ControlGraph> createAllGraphs(InputStream is)
			throws IOException {
		ClassFile cf = new ClassFile();
		try {
			cf.read(new DataInputStream(is));
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return createAllGraphs(cf);
	}

	/**
	 * Create all control graphs in the given class file.
	 * 
	 * @param cf
	 *            the ClassFIle data.
	 * @return
	 * @throws IOException
	 */
	public Map<String, ControlGraph> createAllGraphs(ClassFile cf)
			throws IOException {
		String cln = cf.getClassFileInfo().getName();
		Map<String, ControlGraph> map = new HashMap<String, ControlGraph>();
		Iterator it = cf.getAllMethods().iterator();
		while (it.hasNext()) {
			MethodFileInfo mfi = (MethodFileInfo) it.next();
			String mname = mfi.getName() + mfi.getSignature();
			String fulln = cln + "." + mname;
			ControlGraph cgraph = cache.get(fulln);
			if (cgraph == null) {
				CodeAttribute code = mfi.getCodeAttribute();
				if (code == null) // abstract method
					continue;
				try {
					cgraph = new ControlGraph(mfi);
					cache.put(fulln, cgraph);
				} catch (Exception e1) {
					e1.printStackTrace();
					throw new IOException("I/O error while reading method "
							+ cf.getClassFileInfo().getName() + "." + mname
							+ " : " + e1.getMessage());
				}
			}
			assert cgraph != null;
			map.put(fulln, cgraph);
		}
		return map;
	}

	/**
	 * Factory method for creating graph for a single method in a class. This
	 * method loads the class from the loader this builder is attached to,
	 * locates the requested method and returns the control flow graph
	 * constructed if it exists.
	 * 
	 * @param cls
	 *            class name
	 * @param method
	 *            a method name
	 * @param signature
	 *            signature of method
	 * @return a control graph for given method or null.
	 * @throws IOException
	 *             if cannot find information. This may comes from a problem in
	 *             classpath or in names.
	 */
	public ControlGraph createGraphForMethod(String cls, String method,
			String signature) throws IOException {
		// lookup in cache
		String fulln = cls.replace('.', '/') + "." + method + signature;
		ControlGraph cg;
		if ((cg = cache.get(fulln)) != null)
			return cg;
		// create all graph for this class
		cg = createGraphsForClass(cls).get(fulln);
		if (cg == null)
			throw new IOException("Unable to create control graph for "
					+ method);
		else
			return cg;
	}

	/**
	 * Factory method for creating all control flow graphs of a given class.
	 * This method extracts a stream from the current classpath and then calls
	 * {@link #createAllGraphs(InputStream)}.
	 * 
	 * @param cls
	 *            name of the class to get graphs for.
	 * @return a Map<String,ControlGraph> from method names (with signatures)
	 *         to their control flow graph.
	 * @throws IOException
	 */
	public Map<String, ControlGraph> createGraphsForClass(String cls)
			throws IOException {
		// get stream
		ClassFile cf = factory.getClassFileFor(cls);
		// class name
		if (cf == null)
			throw new IOException("No data in classfile for " + cls);
		return createAllGraphs(cf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.graph.ControlGraphBuilder#createGraphForMethod(java.lang.String)
	 */
	public ControlGraph createGraphForMethod(String method) throws IOException {
		ControlGraph cg;
		if ((cg = cache.get(method)) != null)
			return cg;
		int dot = method.lastIndexOf('.');
		int paren = method.lastIndexOf('(');
		String cln = method.substring(0, dot);
		String mn = method.substring(dot + 1, paren);
		String signature = method.substring(paren);
		return createGraphForMethod(cln, mn, signature);
	}

	public void setFactory(ClassFileFactory factory2) {
		this.factory = factory2;
	}

}
