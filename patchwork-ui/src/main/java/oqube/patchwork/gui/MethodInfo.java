/**
 * 
 */
package oqube.patchwork.gui;

/**
 * A class representing nodes in a call graph.
 * This class has various subclasses representing different nodes 
 * in a call graph: Method start and call sites.
 *  
 * @author nono
 *
 */
public abstract class MethodInfo implements CodeInfo {

	private String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
}
