/**
 * 
 */
package oqube.patchwork.gui;

/**
 * Provides basic information on code elements displayed: maybe classes 
 * or packages.
 * 
 * @author nono
 */
public interface CodeInfo {

	String getName();
	
	/**
	 * A CodeInfo is a reference if it is not part of the analyzed 
	 * set of class files but is not filtered out. 
	 * 
	 * @return true if this info node is a reference element. 
	 */
	boolean isReference();
}
