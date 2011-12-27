package oqube.bytes.instructions;

import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * this class represents a JVM instruction, eventually including various
 * operands
 */
public abstract class Instruction implements Opcodes, Cloneable {

	/**
	 * Singleton defining a pseudo instruction for start of a method.
	 */
	public static final Instruction START = new StartIns();

	private static class StartIns extends Instruction {

		public StartIns() {
			super(-1, null);
		}

		@Override
		public int hashCode() {
			return 6991;
		}

		@Override
		public boolean equals(Object arg0) {
			return arg0 == START;
		}

		@Override
		public void write(DataOutputStream os) throws IOException {
			// NEVER WRITTEN TO STREAM
		}

		@Override
		public String toString() {
			return "START";
		}

	};

	public static final Instruction END = new EndIns();

	private static class EndIns extends Instruction {

		public EndIns() {
			super(-1, null);
		}

		@Override
		public int hashCode() {
			return 6997;
		}

		@Override
		public boolean equals(Object arg0) {
			return arg0 == END;
		}

		@Override
		public void write(DataOutputStream os) throws IOException {
			// NEVER WRITTEN TO STREAM
		}

		@Override
		public String toString() {
			return "END";
		}

	};

	// this instruction's op_code
	public int op_code;

	// the size of the instruction (in bytes)
	public int size = -1;

	// the amount by which the stack is affected
	public int maxstack = 0;

	// this instructions pc
	public short pc;

	protected int curstack = 0;

	private ClassFile classFile;

	public int size() {
		return size;
	}

	public int opcode() {
		return op_code;
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int maxstack() {
		return maxstack;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(opcNames[op_code]);
		return sb.toString();
	}

	public Instruction(int opcode, ClassFile cf) {
		this.op_code = opcode;
		this.classFile = cf;
		try {
			this.size = opcLengths[opcode];
			this.curstack = this.maxstack = opcStack[opcode];
		} catch (Exception e) {
		} // to catch array out of bounds exceptions
	}

	public static Instruction read(int pc, java.io.DataInputStream os,
			ClassFile cf) throws java.io.IOException {
		int inst = os.readUnsignedByte();
		cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
				ClassFileIOEvent.ATOMIC, "opcode=" + inst));
		// construct instruction according to size
		switch (opcLengths[inst]) {
		case 0:
			return null;
		case 1: // 
			return new ZeroArgInst(inst, cf);
		case 2:
			byte arg = os.readByte();
			cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
					ClassFileIOEvent.ATOMIC, "1 arg=" + arg));
			return new OneArgInst(inst, cf, arg);
		case 3:
			short arg2 = os.readShort();
			cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
					ClassFileIOEvent.ATOMIC, "2 arg=" + arg2));
			return new TwoArgInst(inst, cf, arg2);
		case 4:
			short arg3 = os.readShort();
			byte barg3 = os.readByte();
			cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
					ClassFileIOEvent.ATOMIC, "3 arg=" + arg3 + " " + barg3));
			return new ThreeArgInst(inst, cf, arg3, barg3);
		case 5: // distinguish between different instructions
			if (inst == opc_invokeinterface) {
				short arg4 = os.readShort();
				byte barg4 = os.readByte();
				os.readByte();
				cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
						ClassFileIOEvent.ATOMIC, "invokeinterface=" + arg4
								+ " " + barg4));
				return new FourArgInst(inst, cf, arg4, (byte) barg4);
			} else { // jsr_w ou goto_w
				int arg4 = os.readInt();
				cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
						ClassFileIOEvent.ATOMIC, "jumpw=" + arg4));
				return new FourArgInst(inst, cf, arg4);
			}
		default: // must be a variable length instruction
			// read dummy bytes
			cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
					ClassFileIOEvent.START, "switch"));
			int dum = 3 - pc % 4;
			for (int i = 0; i < dum; i++)
				os.readByte();
			// read default
			int def = os.readInt();
			if (inst == opc_lookupswitch) {
				// read npairs count
				int npairs = os.readInt();
				// read pairs
				int[][] pairs = new int[npairs][2];
				for (int i = 0; i < npairs; i++) {
					pairs[i][0] = os.readInt();
					pairs[i][1] = os.readInt();
				}
				cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
						ClassFileIOEvent.END, "switch"));
				// construct lookupswitch
				return new VarArgInst(pc, cf, dum, def, npairs, pairs);
			} else if (inst == opc_tableswitch) { // tableswitch
				int low = os.readInt();
				int high = os.readInt();
				int[] offsets = new int[high - low + 1];
				for (int i = 0; i < high - low + 1; i++)
					offsets[i] = os.readInt();
				cf.dispatch(new ClassFileIOEvent(cf, ClassFileIOEvent.READ,
						ClassFileIOEvent.END, "switch"));
				return new VarArgInst(pc, cf, dum, def, low, high, offsets);
			} else
				throw new IOException("Unhandled opcode " + opcNames[inst]);

		}
	}

	public abstract void write(java.io.DataOutputStream os)
			throws java.io.IOException;

	/**
	 * Method setMaxStack.
	 * 
	 * @param i
	 */
	public void setMaxStack(int i) {
		this.curstack = this.maxstack = i;
	}

	/**
	 * returns true if given opcode is a branching one
	 * 
	 * @return true if opcode is branching (if_xxx, return, jsr, throw)
	 */
	public boolean isBranching() {
		if ((op_code < 0) || (op_code >= opc_ifeq && op_code <= opc_return)
				|| (op_code >= opc_ifnull && op_code <= opc_jsr_w)
				|| (op_code == opc_athrow))
			return true;
		return false;
	}

	/**
	 * @param ins
	 * @return
	 */
	public boolean isConditional() {
		switch (op_code) {
		/* three byte instructions */
		case opc_ifeq:
		case opc_ifne:
		case opc_iflt:
		case opc_ifge:
		case opc_ifgt:
		case opc_ifle:
		case opc_if_icmpeq:
		case opc_if_icmpne:
		case opc_if_icmplt:
		case opc_if_icmpge:
		case opc_if_icmpgt:
		case opc_if_icmple:
		case opc_if_acmpeq:
		case opc_if_acmpne:
		case opc_ifnull:
		case opc_ifnonnull:
		case opc_jsr:
		case opc_jsr_w:
			return true;
		}
		return false;
	}

	/**
	 * compute the offset in code where this branching instruction target
	 * 
	 * @param ins
	 * @return
	 */
	public int targetPc() {
		switch (op_code) {
		/* three byte instructions */
		case opc_ifeq:
		case opc_ifne:
		case opc_iflt:
		case opc_ifge:
		case opc_ifgt:
		case opc_ifle:
		case opc_if_icmpeq:
		case opc_if_icmpne:
		case opc_if_icmplt:
		case opc_if_icmpge:
		case opc_if_icmpgt:
		case opc_if_icmple:
		case opc_if_acmpeq:
		case opc_if_acmpne:
		case opc_goto:
		case opc_ifnull:
		case opc_jsr:
		case opc_ifnonnull:
			byte[] args = ((TwoArgInst) this).args();
			return args[0] << 8 | (args[1] & 0xff);
			/* special cases */
		case opc_ret:
		case opc_tableswitch:
		case opc_lookupswitch:
			return Integer.MIN_VALUE;
			/* return case */
		case opc_ireturn:
		case opc_lreturn:
		case opc_freturn:
		case opc_dreturn:
		case opc_areturn:
		case opc_return:
		case opc_athrow:
			return Integer.MAX_VALUE;
		case opc_jsr_w:
		case opc_goto_w:
			return ((FourArgInst) this).intArg();
		}
		return 0;
	}

	/**
	 * Sets the offset of this branching instruction to given value.
	 * 
	 * @param offset
	 *            absolute offset
	 */
	public void setTargetPc(int offset) {
		switch (op_code) {
		/* three byte instructions */
		case opc_ifeq:
		case opc_ifne:
		case opc_iflt:
		case opc_ifge:
		case opc_ifgt:
		case opc_ifle:
		case opc_if_icmpeq:
		case opc_if_icmpne:
		case opc_if_icmplt:
		case opc_if_icmpge:
		case opc_if_icmpgt:
		case opc_if_icmple:
		case opc_if_acmpeq:
		case opc_if_acmpne:
		case opc_goto:
		case opc_ifnull:
		case opc_jsr:
		case opc_ifnonnull:
			byte[] args = ((TwoArgInst) this).args();
			args[0] = (byte) (offset >> 8);
			args[1] = (byte) (offset & 0xff);
			/* special cases */
		case opc_ret:
		case opc_tableswitch:
		case opc_lookupswitch:
			return;
			/* return case */
		case opc_ireturn:
		case opc_lreturn:
		case opc_freturn:
		case opc_dreturn:
		case opc_areturn:
		case opc_return:
		case opc_athrow:
			return;
		case opc_jsr_w:
		case opc_goto_w:
			args = ((FourArgInst) this).args();
			args[0] = (byte) (offset >> 24);
			args[1] = (byte) (offset >> 16);
			args[2] = (byte) (offset >> 8);
			args[3] = (byte) (offset & 0xff);
		}
	}

	/**
	 * @return
	 */
	public int getPc() {
		return pc;
	}

	/**
	 * @return
	 */
	public boolean isStore() {
		return op_code >= 54 && op_code <= 78;
	}

	/**
	 * @return
	 */
	public boolean isLoad() {
		return op_code >= 21 && op_code <= 45;
	}

	/**
	 * @return Returns the classFile.
	 */
	public ClassFile getClassFile() {
		return classFile;
	}

	/**
	 * @param classFile
	 *            The classFile to set.
	 */
	public void setClassFile(ClassFile classFile) {
		this.classFile = classFile;
	}

	public static void dumpOpcodes() {
		for (int i = 0; i < opcNames.length; i++) {
			System.out.print("[" + i + "] ");
			System.out.println(opcNames[i] + " " + opcLengths[i] + " "
					+ opcStack[i]);
		}
	}
}
