/**
 * 
 */
package oqube.patchwork.gui;

import oqube.bytes.ClassFile;

/**
 * Represents a callee. This type of MethodInfo represents the start of some
 * method. An instance if uniquely identified by the class where it is defined,
 * its name and its signature.
 * 
 * @author nono
 * 
 */
public class MethodStartInfo extends MethodInfo {

	private String klass;

	private String name;

	private String signature;

	private ClassFile classFile;

	/**
	 * Construct a method start from a class name, a name and a signature.
	 * 
	 * @param cls
	 *            The class name.
	 * @param name
	 *            the method's name.
	 * @param signature
	 *            the method's signature.
	 */
	public MethodStartInfo(String cls, String name, String signature) {
		this.klass = cls;
		this.name = name;
		this.signature = signature;
		setDisplayName(ClassDependencyGraph.simplify(klass) + "." + name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.gui.CodeInfo#getName()
	 */
	public String getName() {
		return klass + "." + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.gui.CodeInfo#isReference()
	 */
	public boolean isReference() {
		return classFile == null;
	}

	public ClassFile getClassFile() {
		return classFile;
	}

	public void setClassFile(ClassFile classFile) {
		this.classFile = classFile;
	}

	public String getKlass() {
		return klass;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof MethodStartInfo) || arg0 == null)
			return false;
		MethodStartInfo msi = (MethodStartInfo) arg0;
		return msi.klass.equals(klass) && msi.name.equals(name)
				&& msi.signature.equals(signature);
	}

	@Override
	public int hashCode() {
		return (klass.hashCode() << 7) ^ (name.hashCode() << 13)
				^ (signature.hashCode());
	}

}
