package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent Var-arg instructions, that is
 * instructions that takes a variable number byte argument The only such
 * instructions currently known in the jwm are : - lookupswitch - tableswitch
 */
public class VarArgInst extends Instruction {

    private byte[] args;

    private int pad;

    /**
     * Constructor for lookupswitch
     * 
     * @param pc
     *          program counter where the instruction starts
     * @param def
     *          default value
     * @param npairs
     *          count of jump pairs
     * @param offsets
     *          table of value-jump pairs
     */
    VarArgInst(int pc, ClassFile cf, int pad, int def, int npairs, int[][] pairs) {
        super(opc_lookupswitch, cf);
        this.pad = pad;
        args = new byte[pad + 8 + pairs.length * 8];
        int i = 0;
        for (; i < pad; i++)
            args[i] = (byte) 0;
        args[i++] = (byte) (def >> 24);
        args[i++] = (byte) (def >> 16);
        args[i++] = (byte) (def >> 8);
        args[i++] = (byte) (def & 0xff);
        args[i++] = (byte) (npairs >> 24);
        args[i++] = (byte) (npairs >> 16);
        args[i++] = (byte) (npairs >> 8);
        args[i++] = (byte) (npairs & 0xff);
        for (int j = i; i < args.length; i++) {
            int of = (i - j) / 8;
            args[i++] = (byte) (pairs[of][0] >> 24);
            args[i++] = (byte) (pairs[of][0] >> 16);
            args[i++] = (byte) (pairs[of][0] >> 8);
            args[i++] = (byte) (pairs[of][0] & 0xff);
            args[i++] = (byte) (pairs[of][1] >> 24);
            args[i++] = (byte) (pairs[of][1] >> 16);
            args[i++] = (byte) (pairs[of][1] >> 8);
            args[i] = (byte) (pairs[of][1] & 0xff);
        }
        this.size = 1 + args.length;
    }

    /**
     * Constructs a new tableswitch instruction
     * 
     * @param pc
     *          program counter where the instruction starts
     * @param pad
     *          number of bytes of padding
     * @param def
     *          default value
     * @param low
     *          low boundary for table
     * @param high
     *          high boundary for table
     * @param offsets
     *          table of jump offsets
     */
    VarArgInst(int pc, ClassFile cf, int pad, int def, int low, int high,
            int[] offsets) {
        super(opc_tableswitch, cf);
        this.pad = pad;
        args = new byte[pad + 12 + offsets.length * 4];
        int i = 0;
        for (; i < pad; i++)
            args[i] = (byte) 0;
        args[i++] = (byte) (def >> 24);
        args[i++] = (byte) (def >> 16);
        args[i++] = (byte) (def >> 8);
        args[i++] = (byte) (def & 0xff);
        args[i++] = (byte) (low >> 24);
        args[i++] = (byte) (low >> 16);
        args[i++] = (byte) (low >> 8);
        args[i++] = (byte) (low & 0xff);
        args[i++] = (byte) (high >> 24);
        args[i++] = (byte) (high >> 16);
        args[i++] = (byte) (high >> 8);
        args[i++] = (byte) (high & 0xff);
        for (int j = i; i < args.length; i++) {
            int of = (i - j) / 4;
            args[i++] = (byte) (offsets[of] >> 24);
            args[i++] = (byte) (offsets[of] >> 16);
            args[i++] = (byte) (offsets[of] >> 8);
            args[i] = (byte) (offsets[of] & 0xff);
        }
        this.size = 1 + args.length;
    }

    public byte[] args() {
        return args;
    }

    public void write(java.io.DataOutputStream os) throws java.io.IOException {
        os.writeByte(this.op_code);
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.ATOMIC, "opcode=" + op_code));
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.START, "switch"));
        os.write(this.args);
        getClassFile().dispatch(
                new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
                        ClassFileIOEvent.END, "switch"));
    }

    /**
     * Return the default offset for a tableswitch instruction
     * 
     * @return
     */
    public int getDefault() {
        return (args[pad] << 24) | (args[pad + 1] << 16) | (args[pad + 2] << 8)
                | (args[pad + 3] & 0xff);
    }

    /**
     * Sets the default jump value of this tableswitch instruciton.
     * 
     * @param def
     */
    public void setDefault(int def) {
        int i = 0;
        for (; i < pad; i++)
            ;
        args[i++] = (byte) (def >> 24);
        args[i++] = (byte) (def >> 16);
        args[i++] = (byte) (def >> 8);
        args[i++] = (byte) (def & 0xff);
    }

    /**
     * Sets the offsets table for this tableswitch instruciton.
     * The <code>off</code> array length must be equal to 
     * <code>high</code> minus <code>low</code>.
     * 
     * @param offsets array of jump offsets
     */
    public void setOffsets(int[] offsets) {
        byte[] nargs = new byte[pad + 12 + offsets.length * 4];
        int i = 0;
        for (; i < pad; i++)
            ;
        i += 12;
        System.arraycopy(args,0,nargs,0,i);
        for (int j = i; i < nargs.length; i++) {
            int of = (i - j) / 4;
            nargs[i++] = (byte) (offsets[of] >> 24);
            nargs[i++] = (byte) (offsets[of] >> 16);
            nargs[i++] = (byte) (offsets[of] >> 8);
            nargs[i] = (byte) (offsets[of] & 0xff);
        }
        this.args = nargs;
        this.size = 1 + args.length;
    }

    /**
     * Sets the upper and lower bounds of this tableswitch instruction.
     * 
     * @param low must be lower than high
     * @param high must be greater than low
     */
    public void setHiLo(int low, int  high) {
        int i = pad + 4;
        args[i++] = (byte) (low >> 24);
        args[i++] = (byte) (low >> 16);
        args[i++] = (byte) (low >> 8);
        args[i++] = (byte) (low & 0xff);
        args[i++] = (byte) (high >> 24);
        args[i++] = (byte) (high >> 16);
        args[i++] = (byte) (high >> 8);
        args[i++] = (byte) (high & 0xff);            
    }
    
    /**
     * Return the array of offsets from a tableswitch instruction
     * 
     * @return
     */
    public int[] getOffsets() {
        int ret[] = new int[(args.length - pad - 12) / 4];
        for (int i = 0; i < ret.length; i++) {
            int off = pad + 12 + i * 4;
            ret[i] = (args[off] << 24) | (args[off + 1] << 16)
                    | (args[off + 2] << 8) | (args[off + 3] & 0xff);
        }
        return ret;
    }

    public int getLow() {
        return (args[pad + 4] << 24) | (args[pad + 5] << 16)
                | (args[pad + 6] << 8) | (args[pad + 7] & 0xff);
    }

    public int getHigh() {
        return (args[pad + 8] << 24) | (args[pad + 9] << 16)
                | (args[pad + 10] << 8) | (args[pad + 11] & 0xff);
    }

    /**
     * Returns a two dimension array for pairs of match-offset of a lookupswitch
     * instruction
     * 
     * @return
     */
    public int[][] getPairs() {
        int npairs = (args[pad + 4] << 24) | (args[pad + 5] << 16)
                | (args[pad + 6] << 8) | (args[pad + 7] & 0xff);
        int ret[][] = new int[npairs][2];
        for (int i = 0; i < npairs; i++) {
            int off = pad + 8 + i * 8;
            ret[i][0] = (args[off] << 24) | (args[off + 1] << 16)
                    | (args[off + 2] << 8) | (args[off + 3] & 0xff);
            ret[i][1] = (args[off + 4] << 24) | (args[off + 5] << 16)
                    | (args[off + 6] << 8) | (args[off + 7] & 0xff);
        }
        return ret;
    }

    public void  setPairs(int[][] pairs) {
        byte[] nargs = new byte[pad + 8 + pairs.length * 8];
        int npairs = pairs.length;
        System.arraycopy(args,0,nargs,0,pad + 4);
        int i = pad + 4;
        args[i++] = (byte) (npairs >> 24);
        args[i++] = (byte) (npairs >> 16);
        args[i++] = (byte) (npairs >> 8);
        args[i++] = (byte) (npairs & 0xff);
        for (int j = i; i < args.length; i++) {
            int of = (i - j) / 8;
            args[i++] = (byte) (pairs[of][0] >> 24);
            args[i++] = (byte) (pairs[of][0] >> 16);
            args[i++] = (byte) (pairs[of][0] >> 8);
            args[i++] = (byte) (pairs[of][0] & 0xff);
            args[i++] = (byte) (pairs[of][1] >> 24);
            args[i++] = (byte) (pairs[of][1] >> 16);
            args[i++] = (byte) (pairs[of][1] >> 8);
            args[i] = (byte) (pairs[of][1] & 0xff);
        }
        this.size = 1 + args.length;
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        VarArgInst inst = (VarArgInst) super.clone();
        inst.args = new byte[args.length];
        System.arraycopy(args, 0, inst.args, 0, args.length);
        return inst;
    }
}