/**
 * 
 */
package oqube.patchwork.graph;

import java.util.regex.Pattern;

import oqube.bytes.Opcodes;
import oqube.bytes.instructions.FourArgInst;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.TwoArgInst;
import oqube.bytes.pool.AbstractMethodData;
import oqube.bytes.pool.ConstantPool;
import oqube.bytes.pool.NameAndTypeData;

/**
 * A matcher that allows filtering of calls according to class, method name and
 * method signature pattern. This matcher filter calls according to patterns for
 * class names, method names and method signatures. By default, all methods are
 * filtered in (patterns are set to <code>.*</code>).
 * 
 * @author nono
 * 
 */
public class CallMatcher implements InstructionMatcher {

	/*
	 * pattern for class name
	 */
	private Pattern classPattern = Pattern.compile(".*");

	/*
	 * pattern for method name
	 */
	private Pattern methodPattern = Pattern.compile(".*");

	/*
	 * pattern for method's type
	 */
	private Pattern typePattern = Pattern.compile(".*");

	/**
	 * @return Returns the classPattern.
	 */
	public Pattern getClassPattern() {
		return classPattern;
	}

	/**
	 * @param classPattern
	 *            The classPattern to set.
	 */
	public void setClassPattern(Pattern classPattern) {
		this.classPattern = classPattern;
	}

	/**
	 * @return Returns the methodPattern.
	 */
	public Pattern getMethodPattern() {
		return methodPattern;
	}

	/**
	 * @param methodPattern
	 *            The methodPattern to set.
	 */
	public void setMethodPattern(Pattern methodPattern) {
		this.methodPattern = methodPattern;
	}

	/**
	 * @return Returns the typePattern.
	 */
	public Pattern getTypePattern() {
		return typePattern;
	}

	/**
	 * @param typePattern
	 *            The typePattern to set.
	 */
	public void setTypePattern(Pattern typePattern) {
		this.typePattern = typePattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.graph.InstructionMatcher#match(oqube.bytes.Instruction)
	 */
	public boolean match(Instruction inst) {
		// is inst a method invocation ?
		short idx;
		byte[] args;
		switch (inst.opcode()) {
		case Opcodes.opc_invokeinterface:
			args = ((FourArgInst) inst).args();
			break;
		case Opcodes.opc_invokespecial:
		case Opcodes.opc_invokestatic:
		case Opcodes.opc_invokevirtual:
			args = ((TwoArgInst) inst).args();
			break;
		default:
			return false;
		}
		// get string representations
		idx = (short) ((args[0] << 8) | (args[1] & 0xff));
		ConstantPool pool = inst.getClassFile().getConstantPool();
		AbstractMethodData data = (AbstractMethodData) pool.getEntry(idx);
		String cl = pool.getEntry(data.getClassIndex()).toString();
		NameAndTypeData nat = (NameAndTypeData) pool.getEntry(data
				.getNameAndTypeIndex());
		String mn = nat.getName();
		String mt = nat.getType();
		// match ?
		if(classPattern.matcher(cl).matches() &&
				methodPattern.matcher(mn).matches() && 
				typePattern.matcher(mt).matches())
			return true;
		return false;
	}

}
