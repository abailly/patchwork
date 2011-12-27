/**
 * 
 */
package oqube.patchwork.graph;

import oqube.bytes.instructions.Instruction;

/**
 * Instances of this interface are used to select some 
 * instructions.
 *
 * @author nono
 */
public interface InstructionMatcher {

	/**
	 * a matcher that matches any instruction
	 */
	public static final InstructionMatcher TRUE = new InstructionMatcher() {
		
		public boolean match(Instruction inst) {
			return true;
		}
	
	};

	/**
	 * Returns true if given instruction matches this matcher.
	 * 
	 * @param  inst the Instruction to match; May not be null.
	 * @return true or false.
	 */
	public boolean match(Instruction inst);
}
