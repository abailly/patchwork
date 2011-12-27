package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent Two-arg instructions, that is
 * instructions that takes Two byte argument
 */
public class TwoArgInst extends Instruction implements Cloneable {

    private byte[] args = new byte[2];

    public TwoArgInst(int opcode, ClassFile cf, byte arg1, byte arg2) {
        super(opcode, cf);
        args[0] = arg1;
        args[1] = arg2;
        size = 3;
    }

    public TwoArgInst(int opcode, ClassFile cf, short arg) {
        super(opcode, cf);
        args[0] = (byte) (arg >> 8);
        args[1] = (byte) (arg & 0xff);
        size = 3;
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
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.ATOMIC, "2 arg="
                                + ((args[0] << 8) | args[1])));
    }

    /**
     * @param s
     */
    public void setArgs(short s) {
        args[0] = (byte) (s >> 8);
        args[1] = (byte) (s & 0xff);

    }

    /*
     * (non-Javadoc) [178] getstatic 3 2 [179] putstatic 3 -3 [180] getfield 3 2
     * [181] putfield 3 -3 [182] invokevirtual 3 -99 [183] invokespecial 3 -99
     * [184] invokestatic 3 -99 [187] new 3 1 [189] anewarray 3 0 [192] checkcast
     * 3 0 [193] instanceof 3 0 [185] 
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString()).append(' ');
        if (opcode() >= 178 && opcode() <= 193 && getClassFile() != null) {
            sb.append(getClassFile().getConstantPool().getEntry(
                    (short) ((args[0] << 8) | ((int) (args[1] & 0xff)))));
        } else if (opcode() == opc_iinc)
            sb.append((int) args[0]).append(' ').append((int) args[1]);
        else
            sb.append((args[0] << 8) | args[1]);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        TwoArgInst ti = (TwoArgInst) super.clone();
        ti.args = new byte[2];
        ti.args[0] = args[0];
        ti.args[1] = args[1];
        return ti;
    }

    /**
     * Retrieve the arguments to this instruction as a short value.
     *  
     * @return
     */        
    public short shortArg() {
      return (short) ((args[0] << 8) | ((int) (args[1] & 0xff)));
    }
}