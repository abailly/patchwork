package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent Three-arg instructions, that is
 * instructions that takes Three byte argument The only suchinstruction
 * currently known in the jwm is multianewarray
 */
public class ThreeArgInst extends Instruction {

    private byte[] args = new byte[3];

    ThreeArgInst(int opcode, ClassFile cf, short ref, byte arg1) {
        super(opcode, cf);
        args[0] = (byte) (ref >> 8);
        args[1] = (byte) (ref & 0xff);
        args[2] = arg1;
        size = 4;
        /* this method consumes arg1 element on stack */
        maxstack = -arg1;
    }

    public byte[] args() {
        return args;
    }

    public void write(java.io.DataOutputStream os) throws java.io.IOException {

        os.writeByte(this.op_code);
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.ATOMIC, "opcode=" + op_code));
        os.writeByte(this.args[0]);
        os.writeByte(this.args[1]);
        os.writeByte(this.args[2]);
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.ATOMIC, "3 arg="
                                + ((args[0] << 8) | args[1]) + " " + args[2]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        ThreeArgInst ti = (ThreeArgInst) super.clone();
        ti.args = new byte[3];
        ti.args[0] = args[0];
        ti.args[1] = args[1];
        ti.args[2] = args[2];
        return ti;
    }

    /*
     * (non-Javadoc) [197] multianewarray 4 -99
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("new ");
        for (int i = 0; i < args[2]; i++)
            sb.append("[");
        if (getClassFile() != null)
            sb.append(getClassFile().getConstantPool().getEntry(
                    (short) ((args[0] << 8) | ((int)args[1] & 0xff))));
        else
            sb.append((short) (args[0] << 8 | ((int)args[1] & 0xff)));
        return sb.toString();
    }
}