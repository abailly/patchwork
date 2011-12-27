/**
 * 
 */
package oqube.patchwork.gui;

/**
 * Represents a call site of some method. A call site is defined from a caller
 * method info and a label which uniquely identifies this call site within the
 * caller code.
 * 
 * @author nono
 * 
 */
public class MethodCallInfo extends MethodInfo {

	private MethodInfo caller;

	private String label;

	/**
	 * Construct a call site for given caller and label.
	 * 
	 * @param caller
	 *            the method info this call site is part fo. May not be null.
	 * @param label
	 *            UID within method of this call site. May not be null.
	 */
	public MethodCallInfo(MethodInfo caller, String label) {
		this.caller = caller;
		this.label = label;
		setDisplayName(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.gui.CodeInfo#getName()
	 */
	public String getName() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.gui.CodeInfo#isReference()
	 */
	public boolean isReference() {
		return false;
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof MethodCallInfo) || arg0 == null)
			return false;
		MethodCallInfo call = (MethodCallInfo) arg0;
		return (call.caller == null ? caller == null : call.caller
				.equals(caller))
				&& (call.label == null ? label == null : call.label
						.equals(label));
	}

	@Override
	public int hashCode() {
		return ((caller == null ? 0 : caller.hashCode()) << 7)
				^ ((label == null ? -1 : label.hashCode()));
	}

}
