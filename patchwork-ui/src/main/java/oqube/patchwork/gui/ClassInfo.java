/**
 * 
 */
package oqube.patchwork.gui;

import java.net.URL;

import oqube.bytes.ClassFile;

/**
 * A class for storing information about an analyzed class during dependency
 * analysis. ClassInfo objects are totally ordered using the lexicographic
 * ordering on their full name.
 * 
 * @author nono
 * 
 */
public class ClassInfo implements CodeInfo, Comparable<ClassInfo> {

	private String name;

	private String displayName;

	private int linkTo;

	private int linkFrom;

	private URL classURL;

	private ClassFile classFile;

	public ClassInfo(String string) {
		this.name = string;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getLinkFrom() {
		return linkFrom;
	}

	public void setLinkFrom(int linkFrom) {
		this.linkFrom = linkFrom;
	}

	public int getLinkTo() {
		return linkTo;
	}

	public void setLinkTo(int linkTo) {
		this.linkTo = linkTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void incrementLinkTo() {
		linkTo++;
	}

	public void incrementLinkFrom() {
		linkFrom++;
	}

	@Override
	public boolean equals(Object arg0) {
		ClassInfo ci = (ClassInfo) arg0;
		return ci.name == null ? name == null : ci.name.equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return displayName;
	}

	public ClassFile getClassFile() {
		return classFile;
	}

	public void setClassFile(ClassFile classFile) {
		this.classFile = classFile;
	}

	public URL getClassURL() {
		return classURL;
	}

	public void setClassURL(URL classURL) {
		this.classURL = classURL;
	}

	public boolean isReference() {
		return classFile == null;
	}

	public int compareTo(ClassInfo arg0) {
		return this.name.compareTo(arg0.name);
	}

}
