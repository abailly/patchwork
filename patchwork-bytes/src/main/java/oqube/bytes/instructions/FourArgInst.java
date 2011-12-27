package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent Four-arg instructions, that is
 * instructions that takes Four byte argument The only such instructions
 * currently known in the jwm are : - goto_w - jsr_w - invokeinterface
 */
public class FourArgInst extends Instruction {

    private byte[] args = new byte[4];

    /**
     * Constructor for invokeinterface
     */
    FourArgInst(int opcode, ClassFile c, short ref, byte count) {
        super(opcode, c);
        /* check index validity */
        if(c.getConstantPool().getEntry(ref) == null)
            throw new IllegalArgumentException("Invalid constant pool entry "+ref+ " for invokeinterface instruction ");
        if(count <0)
            throw new IllegalArgumentException("Invalid argument count "+count+ " for invokeinterface instruction ");            
        args[0] = (byte) (ref >> 8);
        args[1] = (byte) (ref & 0xff);
        args[2] = count;
        args[3] = 0;
        this.size = 5;
        this.maxstack = -count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString()).append(' ');
        if (opcode() == opc_invokeinterface && getClassFile() != null)
            sb.append(
                    getClassFile().getConstantPool().getEntry(
                            (short) ((args[0] << 8) | (args[1] & 0xff)))).append(' ')
                    .append(args[2] & 0xff);
        else
            sb.append((args[0] << 24) | (args[1] << 16) | (args[2] << 8)
                    | args[3]);
        return sb.toString();
    }

    /**
     * constructor for goto_w et jsr_w
     */
    FourArgInst(int opcode, ClassFile c, int offset) {
        super(opcode, c);
        args[0] = (byte) (offset >> 24);
        args[1] = (byte) (offset >> 16);
        args[2] = (byte) (offset >> 8);
        args[3] = (byte) (offset & 0xff);
        this.size = 5;
        this.maxstack = 0;
    }

    public byte[] args() {
        return args;
    }

    public void write(java.io.DataOutputStream os) throws java.io.IOException {

        os.writeByte(this.op_code);
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.ATOMIC, "opcode=" + op_code));
        os.write(args);
        if (op_code == opc_invokeinterface) {
            getClassFile().dispatch(
                    new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                            ClassFileIOEvent.ATOMIC, "invokeinterface="
                                    + ((args[0] << 8) | args[1]) + " "
                                    + args[2]));
        } else { // jsr_w ou goto_w
            getClassFile().dispatch(
                    new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                            ClassFileIOEvent.ATOMIC, "jumpw=" + intArg()));
        }
    }

    /**
     * @return
     */
    public int intArg() {
        return (args[0] << 24) | (args[1] << 16) | (args[2] << 8) | args[3];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        FourArgInst inst = (FourArgInst) super.clone();
        inst.args = new byte[args.length];
        System.arraycopy(args, 0, inst.args, 0, args.length);
        return inst;
    }

}