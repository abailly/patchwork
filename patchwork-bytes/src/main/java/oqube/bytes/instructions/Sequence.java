package oqube.bytes.instructions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.TypeHelper;
import oqube.bytes.pool.FieldRefData;
import oqube.bytes.pool.InterfaceMethodData;
import oqube.bytes.pool.MethodRefData;
import oqube.bytes.pool.NameAndTypeData;

/**
 * This class represents a sequence of instructions. Any instruction can be part
 * of a sequence, in particular they may be branches and returns, so a Sequence
 * object should not be confused with a basic block.
 * 
 * @author Arnaud Bailly
 * @version $Id: Sequence.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class Sequence extends Instruction {

  class SequenceIterator implements Iterator {

    Iterator it = instructions.iterator();

    Instruction cur;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      size -= cur.size();
      it.remove();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return it.hasNext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    public Object next() {
      cur = (Instruction) it.next();
      return cur;
    }

  }

  private List<Instruction> instructions = new ArrayList<Instruction>();

  public Sequence(ClassFile cf) {
    super(-1, cf);
    this.size = 0;
  }

  /* (non-Javadoc)
   * @see oqube.bytes.Instruction#setClassFile(oqube.bytes.ClassFile)
   */
  public void setClassFile(ClassFile classFile) {
    Iterator it = instructions.iterator();
    while(it.hasNext()) {
      Instruction in  =(Instruction)it.next();
      in.setClassFile(classFile);
    }
  }
  
  public String toString() {
    return instructions.toString();
  }

  public Iterator iterator() {
    return new SequenceIterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    Sequence seq = (Sequence) super.clone();
    seq.instructions = new ArrayList<Instruction>();
    Iterator it = instructions.iterator();
    while (it.hasNext())
      seq.add((Instruction) ((Instruction) it.next()).clone());
    return seq;
  }

  public Sequence add(Instruction i) {
    if (i == null)
      return this;
    // if we get a sequence, we firs linearize it
    if (i instanceof Sequence) {
      Iterator it = ((Sequence) i).iterator();
      while (it.hasNext())
        add((Instruction) it.next());
      return this;
    }
    //i.pc = (short) (this.pc + i.size());
    instructions.add(i);
    size += i.size();
    // calculate stack
    int tmp = curstack;
    curstack += i.curstack;
    if (curstack > maxstack)
      maxstack = curstack;
    // check growing of stack
    if (tmp + i.maxstack > maxstack)
      maxstack = tmp + i.maxstack;
    //        System.err.println(
    //            "Adding "
    //                + i
    //                + " to "
    //                + this
    //                + " stack="
    //                + curstack
    //                + ", max="
    //                + maxstack);
    return this;
  }

  public void write(java.io.DataOutputStream os) throws java.io.IOException {
    java.util.Iterator it = instructions.iterator();
    while (it.hasNext())
      ((Instruction) it.next()).write(os);

  }

  /**
   * Returns the instructions.
   * 
   * @return java.util.List
   */
  public java.util.List<Instruction> getInstructions() {
    return instructions;
  }

  public Sequence _nop() {
    return add(new ZeroArgInst(0,getClassFile()));
  }

  public Sequence _aconst_null() {
    return add(new ZeroArgInst(1,getClassFile()));
  }

  public Sequence _iconst_m1() {
    return add(new ZeroArgInst(2,getClassFile()));
  }

  public Sequence _iconst_0() {
    return add(new ZeroArgInst(3,getClassFile()));
  }

  public Sequence _iconst_1() {
    return add(new ZeroArgInst(4,getClassFile()));
  }

  public Sequence _iconst_2() {
    return add(new ZeroArgInst(5,getClassFile()));
  }

  public Sequence _iconst_3() {
    return add(new ZeroArgInst(6,getClassFile()));
  }

  public Sequence _iconst_4() {
    return add(new ZeroArgInst(7,getClassFile()));
  }

  public Sequence _iconst_5() {
    return add(new ZeroArgInst(8,getClassFile()));
  }

  public Sequence _lconst_0() {
    return add(new ZeroArgInst(9,getClassFile()));
  }

  public Sequence _lconst_1() {
    return add(new ZeroArgInst(10,getClassFile()));
  }

  public Sequence _fconst_0() {
    return add(new ZeroArgInst(11,getClassFile()));
  }

  public Sequence _fconst_1() {
    return add(new ZeroArgInst(12,getClassFile()));
  }

  public Sequence _fconst_2() {
    return add(new ZeroArgInst(13,getClassFile()));
  }

  public Sequence _dconst_0() {
    return add(new ZeroArgInst(14,getClassFile()));
  }

  public Sequence _dconst_1() {
    return add(new ZeroArgInst(15,getClassFile()));
  }

  public Sequence _bipush(byte b) {
    return add(new OneArgInst(16,getClassFile(), b));
  }

  public Sequence _sipush(short s) {
    return add(new TwoArgInst(17,getClassFile(), s));
  }

  public Sequence _ldc(int i) {
    return add(new OneArgInst(18,getClassFile(), (byte) i));
  }

  public Sequence _ldc_w(int i) {
    return add(new TwoArgInst(19,getClassFile(), (short) i));
  }

  public Sequence _ldc2_w(int i) {
    return add(new TwoArgInst(20,getClassFile(), (short) i));
  }

  public Sequence _iload(int b) {
    return add(new OneArgInst(21,getClassFile(), (byte) b));
  }

  public Sequence _lload(int b) {
    return add(new OneArgInst(22,getClassFile(), (byte) b));
  }

  public Sequence _fload(int b) {
    return add(new OneArgInst(23,getClassFile(), (byte) b));
  }

  public Sequence _dload(int b) {
    return add(new OneArgInst(24,getClassFile(), (byte) b));
  }

  public Sequence _aload(int b) {
    return add(new OneArgInst(25,getClassFile(), (byte) b));
  }

  public Sequence _iload_0() {
    return add(new ZeroArgInst(26,getClassFile()));
  }

  public Sequence _iload_1() {
    return add(new ZeroArgInst(27,getClassFile()));
  }

  public Sequence _iload_2() {
    return add(new ZeroArgInst(28,getClassFile()));
  }

  public Sequence _iload_3() {
    return add(new ZeroArgInst(29,getClassFile()));
  }

  public Sequence _lload_0() {
    return add(new ZeroArgInst(30,getClassFile()));
  }

  public Sequence _lload_1() {
    return add(new ZeroArgInst(31,getClassFile()));
  }

  public Sequence _lload_2() {
    return add(new ZeroArgInst(32,getClassFile()));
  }

  public Sequence _lload_3() {
    return add(new ZeroArgInst(33,getClassFile()));
  }

  public Sequence _fload_0() {
    return add(new ZeroArgInst(34,getClassFile()));
  }

  public Sequence _fload_1() {
    return add(new ZeroArgInst(35,getClassFile()));
  }

  public Sequence _fload_2() {
    return add(new ZeroArgInst(36,getClassFile()));
  }

  public Sequence _fload_3() {
    return add(new ZeroArgInst(37,getClassFile()));
  }

  public Sequence _dload_0() {
    return add(new ZeroArgInst(38,getClassFile()));
  }

  public Sequence _dload_1() {
    return add(new ZeroArgInst(39,getClassFile()));
  }

  public Sequence _dload_2() {
    return add(new ZeroArgInst(40,getClassFile()));
  }

  public Sequence _dload_3() {
    return add(new ZeroArgInst(41,getClassFile()));
  }

  public Sequence _aload_0() {
    return add(new ZeroArgInst(42,getClassFile()));
  }

  public Sequence _aload_1() {
    return add(new ZeroArgInst(43,getClassFile()));
  }

  public Sequence _aload_2() {
    return add(new ZeroArgInst(44,getClassFile()));
  }

  public Sequence _aload_3() {
    return add(new ZeroArgInst(45,getClassFile()));
  }

  public Sequence _iaload() {
    return add(new ZeroArgInst(46,getClassFile()));
  }

  public Sequence _laload() {
    return add(new ZeroArgInst(47,getClassFile()));
  }

  public Sequence _faload() {
    return add(new ZeroArgInst(48,getClassFile()));
  }

  public Sequence _daload() {
    return add(new ZeroArgInst(49,getClassFile()));
  }

  public Sequence _aaload() {
    return add(new ZeroArgInst(50,getClassFile()));
  }

  public Sequence _baload() {
    return add(new ZeroArgInst(51,getClassFile()));
  }

  public Sequence _caload() {
    return add(new ZeroArgInst(52,getClassFile()));
  }

  public Sequence _saload() {
    return add(new ZeroArgInst(53,getClassFile()));
  }

  public Sequence _istore(int i) {
    return add(new OneArgInst(54,getClassFile(), (byte) i));
  }

  public Sequence _lstore(int i) {
    return add(new OneArgInst(55,getClassFile(), (byte) i));
  }

  public Sequence _fstore(int i) {
    return add(new OneArgInst(56,getClassFile(), (byte) i));
  }

  public Sequence _dstore(int i) {
    return add(new OneArgInst(57,getClassFile(), (byte) i));
  }

  public Sequence _astore(int i) {
    return add(new OneArgInst(58,getClassFile(), (byte) i));
  }

  public Sequence _istore_0() {
    return add(new ZeroArgInst(59,getClassFile()));
  }

  public Sequence _istore_1() {
    return add(new ZeroArgInst(60,getClassFile()));
  }

  public Sequence _istore_2() {
    return add(new ZeroArgInst(61,getClassFile()));
  }

  public Sequence _istore_3() {
    return add(new ZeroArgInst(62,getClassFile()));
  }

  public Sequence _lstore_0() {
    return add(new ZeroArgInst(63,getClassFile()));
  }

  public Sequence _lstore_1() {
    return add(new ZeroArgInst(64,getClassFile()));
  }

  public Sequence _lstore_2() {
    return add(new ZeroArgInst(65,getClassFile()));
  }

  public Sequence _lstore_3() {
    return add(new ZeroArgInst(66,getClassFile()));
  }

  public Sequence _fstore_0() {
    return add(new ZeroArgInst(67,getClassFile()));
  }

  public Sequence _fstore_1() {
    return add(new ZeroArgInst(68,getClassFile()));
  }

  public Sequence _fstore_2() {
    return add(new ZeroArgInst(69,getClassFile()));
  }

  public Sequence _fstore_3() {
    return add(new ZeroArgInst(70,getClassFile()));
  }

  public Sequence _dstore_0() {
    return add(new ZeroArgInst(71,getClassFile()));
  }

  public Sequence _dstore_1() {
    return add(new ZeroArgInst(72,getClassFile()));
  }

  public Sequence _dstore_2() {
    return add(new ZeroArgInst(73,getClassFile()));
  }

  public Sequence _dstore_3() {
    return add(new ZeroArgInst(74,getClassFile()));
  }

  public Sequence _astore_0() {
    return add(new ZeroArgInst(75,getClassFile()));
  }

  public Sequence _astore_1() {
    return add(new ZeroArgInst(76,getClassFile()));
  }

  public Sequence _astore_2() {
    return add(new ZeroArgInst(77,getClassFile()));
  }

  public Sequence _astore_3() {
    return add(new ZeroArgInst(78,getClassFile()));
  }

  public Sequence _iastore() {
    return add(new ZeroArgInst(79,getClassFile()));
  }

  public Sequence _lastore() {
    return add(new ZeroArgInst(80,getClassFile()));
  }

  public Sequence _fastore() {
    return add(new ZeroArgInst(81,getClassFile()));
  }

  public Sequence _dastore() {
    return add(new ZeroArgInst(82,getClassFile()));
  }

  public Sequence _aastore() {
    return add(new ZeroArgInst(83,getClassFile()));
  }

  public Sequence _bastore() {
    return add(new ZeroArgInst(84,getClassFile()));
  }

  public Sequence _castore() {
    return add(new ZeroArgInst(85,getClassFile()));
  }

  public Sequence _sastore() {
    return add(new ZeroArgInst(86,getClassFile()));
  }

  public Sequence _pop() {
    return add(new ZeroArgInst(87,getClassFile()));
  }

  public Sequence _pop2() {
    return add(new ZeroArgInst(88,getClassFile()));
  }

  public Sequence _dup() {
    return add(new ZeroArgInst(89,getClassFile()));
  }

  public Sequence _dup_x1() {
    return add(new ZeroArgInst(90,getClassFile()));
  }

  public Sequence _dup_x2() {
    return add(new ZeroArgInst(91,getClassFile()));
  }

  public Sequence _dup2() {
    return add(new ZeroArgInst(92,getClassFile()));
  }

  public Sequence _dup2_x1() {
    return add(new ZeroArgInst(93,getClassFile()));
  }

  public Sequence _dup2_x2() {
    return add(new ZeroArgInst(94,getClassFile()));
  }

  public Sequence _swap() {
    return add(new ZeroArgInst(95,getClassFile()));
  }

  public Sequence _iadd() {
    return add(new ZeroArgInst(96,getClassFile()));
  }

  public Sequence _ladd() {
    return add(new ZeroArgInst(97,getClassFile()));
  }

  public Sequence _fadd() {
    return add(new ZeroArgInst(98,getClassFile()));
  }

  public Sequence _dadd() {
    return add(new ZeroArgInst(99,getClassFile()));
  }

  public Sequence _isub() {
    return add(new ZeroArgInst(100,getClassFile()));
  }

  public Sequence _lsub() {
    return add(new ZeroArgInst(101,getClassFile()));
  }

  public Sequence _fsub() {
    return add(new ZeroArgInst(102,getClassFile()));
  }

  public Sequence _dsub() {
    return add(new ZeroArgInst(103,getClassFile()));
  }

  public Sequence _imul() {
    return add(new ZeroArgInst(104,getClassFile()));
  }

  public Sequence _lmul() {
    return add(new ZeroArgInst(105,getClassFile()));
  }

  public Sequence _fmul() {
    return add(new ZeroArgInst(106,getClassFile()));
  }

  public Sequence _dmul() {
    return add(new ZeroArgInst(107,getClassFile()));
  }

  public Sequence _idiv() {
    return add(new ZeroArgInst(108,getClassFile()));
  }

  public Sequence _ldiv() {
    return add(new ZeroArgInst(109,getClassFile()));
  }

  public Sequence _fdiv() {
    return add(new ZeroArgInst(110,getClassFile()));
  }

  public Sequence _ddiv() {
    return add(new ZeroArgInst(111,getClassFile()));
  }

  public Sequence _irem() {
    return add(new ZeroArgInst(112,getClassFile()));
  }

  public Sequence _lrem() {
    return add(new ZeroArgInst(113,getClassFile()));
  }

  public Sequence _frem() {
    return add(new ZeroArgInst(114,getClassFile()));
  }

  public Sequence _drem() {
    return add(new ZeroArgInst(115,getClassFile()));
  }

  public Sequence _ineg() {
    return add(new ZeroArgInst(116,getClassFile()));
  }

  public Sequence _lneg() {
    return add(new ZeroArgInst(117,getClassFile()));
  }

  public Sequence _fneg() {
    return add(new ZeroArgInst(118,getClassFile()));
  }

  public Sequence _dneg() {
    return add(new ZeroArgInst(119,getClassFile()));
  }

  public Sequence _ishl() {
    return add(new ZeroArgInst(120,getClassFile()));
  }

  public Sequence _lshl() {
    return add(new ZeroArgInst(121,getClassFile()));
  }

  public Sequence _ishr() {
    return add(new ZeroArgInst(122,getClassFile()));
  }

  public Sequence _lshr() {
    return add(new ZeroArgInst(123,getClassFile()));
  }

  public Sequence _iushr() {
    return add(new ZeroArgInst(124,getClassFile()));
  }

  public Sequence _lushr() {
    return add(new ZeroArgInst(125,getClassFile()));
  }

  public Sequence _iand() {
    return add(new ZeroArgInst(126,getClassFile()));
  }

  public Sequence _land() {
    return add(new ZeroArgInst(127,getClassFile()));
  }

  public Sequence _ior() {
    return add(new ZeroArgInst(128,getClassFile()));
  }

  public Sequence _lor() {
    return add(new ZeroArgInst(129,getClassFile()));
  }

  public Sequence _ixor() {
    return add(new ZeroArgInst(130,getClassFile()));
  }

  public Sequence _lxor() {
    return add(new ZeroArgInst(131,getClassFile()));
  }

  public Sequence _iinc(int idx, int val) {
    return add(new TwoArgInst(132,getClassFile(), (byte) idx, (byte) val));
  }

  public Sequence _i2l() {
    return add(new ZeroArgInst(133,getClassFile()));
  }

  public Sequence _i2f() {
    return add(new ZeroArgInst(134,getClassFile()));
  }

  public Sequence _i2d() {
    return add(new ZeroArgInst(135,getClassFile()));
  }

  public Sequence _l2i() {
    return add(new ZeroArgInst(136,getClassFile()));
  }

  public Sequence _l2f() {
    return add(new ZeroArgInst(137,getClassFile()));
  }

  public Sequence _l2d() {
    return add(new ZeroArgInst(138,getClassFile()));
  }

  public Sequence _f2i() {
    return add(new ZeroArgInst(139,getClassFile()));
  }

  public Sequence _f2l() {
    return add(new ZeroArgInst(140,getClassFile()));
  }

  public Sequence _f2d() {
    return add(new ZeroArgInst(141,getClassFile()));
  }

  public Sequence _d2i() {
    return add(new ZeroArgInst(142,getClassFile()));
  }

  public Sequence _d2l() {
    return add(new ZeroArgInst(143,getClassFile()));
  }

  public Sequence _d2f() {
    return add(new ZeroArgInst(144,getClassFile()));
  }

  public Sequence _i2b() {
    return add(new ZeroArgInst(145,getClassFile()));
  }

  public Sequence _i2c() {
    return add(new ZeroArgInst(146,getClassFile()));
  }

  public Sequence _i2s() {
    return add(new ZeroArgInst(147,getClassFile()));
  }

  public Sequence _lcmp() {
    return add(new ZeroArgInst(148,getClassFile()));
  }

  public Sequence _fcmpl() {
    return add(new ZeroArgInst(149,getClassFile()));
  }

  public Sequence _fcmpg() {
    return add(new ZeroArgInst(150,getClassFile()));
  }

  public Sequence _dcmpl() {
    return add(new ZeroArgInst(151,getClassFile()));
  }

  public Sequence _dcmpg() {
    return add(new ZeroArgInst(152,getClassFile()));
  }

  /* branching instructions */
  public Sequence _ifeq(int i) {
    return add(new TwoArgInst(153,getClassFile(), (short) i));
  }

  public Sequence _ifne(int i) {
    return add(new TwoArgInst(154,getClassFile(), (short) i));
  }

  public Sequence _iflt(int i) {
    return add(new TwoArgInst(155,getClassFile(), (short) i));
  }

  public Sequence _ifge(int i) {
    return add(new TwoArgInst(156,getClassFile(), (short) i));
  }

  public Sequence _ifgt(int i) {
    return add(new TwoArgInst(157,getClassFile(), (short) i));
  }

  public Sequence _ifle(int i) {
    return add(new TwoArgInst(158,getClassFile(), (short) i));
  }

  public Sequence _if_icmpeq(int i) {
    return add(new TwoArgInst(159,getClassFile(), (short) i));
  }

  public Sequence _if_icmpne(int i) {
    return add(new TwoArgInst(160,getClassFile(), (short) i));
  }

  public Sequence _if_icmplt(int i) {
    return add(new TwoArgInst(161,getClassFile(), (short) i));
  }

  public Sequence _if_icmpge(int i) {
    return add(new TwoArgInst(162,getClassFile(), (short) i));
  }

  public Sequence _if_icmpgt(int i) {
    return add(new TwoArgInst(163,getClassFile(), (short) i));
  }

  public Sequence _if_icmple(int i) {
    return add(new TwoArgInst(164,getClassFile(), (short) i));
  }

  public Sequence _if_acmpeq(int i) {
    return add(new TwoArgInst(165,getClassFile(), (short) i));
  }

  public Sequence _if_acmpne(int i) {
    return add(new TwoArgInst(166,getClassFile(), (short) i));
  }

  public Sequence _goto(int i) {
    return add(new TwoArgInst(167,getClassFile(), (short) i));
  }

  public Sequence _jsr(int i) {
    return add(new TwoArgInst(168,getClassFile(), (short) i));
  }

  public Sequence _ret(int i) {
    return add(new OneArgInst(169,getClassFile(), (byte) i));
  }

  public Sequence _lookupswitch(int pc, int def, int npairs, int[][] pairs) {
    return add(new VarArgInst(pc,getClassFile(), (pc+1)% 4 != 0 ? 4 - ((pc+1) % 4) : 0, def, npairs, pairs));
  }

  public Sequence _tableswitch(int pc, int def, int low, int high,
      int[] offsets) {
    return add(new VarArgInst(pc,getClassFile(),  (pc+1) % 4 != 0 ? 4 - ((pc+1) % 4) : 0, def, low, high, offsets));
  }

  /* return instructions */
  public Sequence _ireturn() {
    return add(new ZeroArgInst(172,getClassFile()));
  }

  public Sequence _lreturn() {
    return add(new ZeroArgInst(173,getClassFile()));
  }

  public Sequence _freturn() {
    return add(new ZeroArgInst(174,getClassFile()));
  }

  public Sequence _dreturn() {
    return add(new ZeroArgInst(175,getClassFile()));
  }

  public Sequence _areturn() {
    return add(new ZeroArgInst(176,getClassFile()));
  }

  public Sequence _return() {
    return add(new ZeroArgInst(177,getClassFile()));
  }

  /* end of branching */
  public Sequence _getstatic(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(178,getClassFile(), (short) mref);
    FieldRefData field = (FieldRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) field.getNameAndTypeIndex())).getType();
    char ret = tname.charAt(0);
    switch (ret) {
    case 'V':
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(2);
      break;
    default:
      inst.setMaxStack(1);
    }
    return add(inst);
  }

  public Sequence _putstatic(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(179,getClassFile(), (short) mref);
    FieldRefData field = (FieldRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) field.getNameAndTypeIndex())).getType();
    char ret = tname.charAt(0);
    switch (ret) {
    case 'V':
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(1);
      break;
    default:
      inst.setMaxStack(0);
    }
    return add(inst);
  }

  public Sequence _getfield(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(180,getClassFile(), (short) mref);
    FieldRefData field = (FieldRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) field.getNameAndTypeIndex())).getType();
    char ret = tname.charAt(0);
    switch (ret) {
    case 'V':
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(1);
      break;
    default:
      inst.setMaxStack(0);
    }
    return add(inst);
  }

  public Sequence _putfield(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(181,getClassFile(), (short) mref);
    FieldRefData field = (FieldRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) field.getNameAndTypeIndex())).getType();
    char ret = tname.charAt(0);
    switch (ret) {
    case 'V':
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(-3);
      break;
    default:
      inst.setMaxStack(-2);
    }
    return add(inst);
  }

  /**
   * Invoke a virtual method
   * 
   * This method generates in the sequence instructions for invoking a virtual
   * method referred to by its index in constant pool. The maxstack is modified
   * according to number of arguments and return type.
   * 
   * @param mref
   *          index in constant pool of method ref
   * @param cf
   *          ClassFile object used for pool reference
   */
  public Sequence _invokevirtual(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(182,getClassFile(), (short) mref);
    MethodRefData meth = (MethodRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) meth.getNameAndTypeIndex())).getType();
    // calculate number of arguments and add one for 'this' reference
    int nargs = TypeHelper.countArguments(tname) + 1;
    // if return type is not void, add one to stack
    char ret = tname.substring(tname.indexOf(')') + 1).charAt(0);
    switch (ret) {
    case 'V':
      inst.setMaxStack(-nargs);
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(2 - nargs);
      break;
    default:
      inst.setMaxStack(1 - nargs);
    }
    return add(inst);
  }

  /**
   * Invoke a special method (e.g. a constructor)
   * 
   * This method generates in the sequence instructions for invoking a special
   * method referred to by its index in constant pool. The maxstack is modified
   * according to number of arguments and return type.
   * 
   * @param mref
   *          index in constant pool of method ref
   * @param cf
   *          ClassFile object used for pool reference
   */
  public Sequence _invokespecial(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(183,getClassFile(), (short) mref);
    MethodRefData meth = (MethodRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) meth.getNameAndTypeIndex())).getType();
    // calculate number of arguments and add one for 'this' reference
    int nargs = TypeHelper.countArguments(tname) + 1;
    // if return type is not void, add one to stack
    inst.setMaxStack(-nargs);
    return add(inst);
  }

  /**
   * Invoke a static method
   * 
   * This method generates in the sequence instructions for statically invoking
   * a method referred to by its index. The maxstack is modified according to
   * number of arguments and return type.
   * 
   * @param mref
   *          index in constant pool of method ref
   * @param cf
   *          ClassFile object used for pool reference
   */
  public Sequence _invokestatic(int mref, ClassFile cf) {
    Instruction inst = new TwoArgInst(184,getClassFile(), (short) mref);
    MethodRefData meth = (MethodRefData) cf.getConstantPool().getEntry(
        (short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) meth.getNameAndTypeIndex())).getType();
    // calculate number of arguments
    int nargs = TypeHelper.countArguments(tname);
    // if return type is not void, add one to stack
    char ret = tname.substring(tname.indexOf(')') + 1).charAt(0);
    switch (ret) {
    case 'V':
      inst.setMaxStack(-nargs);
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(2 - nargs);
      break;
    default:
      inst.setMaxStack(1 - nargs);
    }
    return add(inst);
  }

  /**
   * Invoke an interface method
   * 
   * This method generates in the sequence instructions for invoking a method
   * through an interface reference designated by its constant poolindex. The
   * maxstack is modified according to number of arguments and return type.
   * 
   * @param mref
   *          index in constant pool of method ref
   * @param arg
   *          number of arguments
   * @param cf
   *          ClassFile object used for pool reference
   */
  public Sequence _invokeinterface(int mref, int arg, ClassFile cf) {
    Instruction inst = new FourArgInst(185,getClassFile(), (short) mref, (byte) arg);
    InterfaceMethodData meth = (InterfaceMethodData) cf.getConstantPool()
        .getEntry((short) mref);
    String tname = ((NameAndTypeData) cf.getConstantPool().getEntry(
        (short) meth.getNameAndTypeIndex())).getType();
    // calculate number of arguments and add one for 'this' reference
    int nargs = arg;
    // if return type is not void, add one to stack
    char ret = tname.substring(tname.indexOf(')') + 1).charAt(0);
    switch (ret) {
    case 'V':
      inst.setMaxStack(-nargs);
      break;
    case 'D':
    case 'J':
      inst.setMaxStack(2 - nargs);
      break;
    default:
      inst.setMaxStack(1 - nargs);
    }
    return add(inst);
  }

  public Sequence _xxxunusedxxx() {
    return add(new ZeroArgInst(186,getClassFile()));
  }

  public Sequence _new(int i) {
    Instruction inst = new TwoArgInst(187,getClassFile(), (short) i);
    return add(inst);
  }

  public Sequence _newarray() {
    return add(new ZeroArgInst(188,getClassFile()));
  }

  public Sequence _anewarray(short tref) {
    return add(new TwoArgInst(189,getClassFile(),tref));
  }

  public Sequence _arraylength() {
    return add(new ZeroArgInst(190,getClassFile()));
  }

  /* branching */
  public Sequence _athrow() {
    return add(new ZeroArgInst(191,getClassFile()));
  }

  /* end of branching */
  public Sequence _checkcast(int i) {
    return add(new TwoArgInst(192,getClassFile(), (short) i));
  }

  public Sequence _instanceof(int i) {
    return add(new TwoArgInst(193,getClassFile(), (short) i));
  }

  public Sequence _monitorenter() {
    return add(new ZeroArgInst(194,getClassFile()));
  }

  public Sequence _monitorexit() {
    return add(new ZeroArgInst(195,getClassFile()));
  }

  public Sequence _wide() {
    return add(new ZeroArgInst(196,getClassFile()));
  }

  public Sequence _multianewarray(int tref,int dim) {
    return add(new ThreeArgInst(197,getClassFile(),(short)tref,(byte)dim));
  }

  /* branching */
  public Sequence _ifnull(int i) {
    return add(new TwoArgInst(198,getClassFile(), (short) i));
  }

  public Sequence _ifnonnull(int i) {
    return add(new TwoArgInst(199,getClassFile(), (short) i));
  }

  public Sequence _goto_w() {
    return add(new ZeroArgInst(200,getClassFile()));
  }

  public Sequence _jsr_w() {
    return add(new ZeroArgInst(201,getClassFile()));
  }

  /* end of branching */
  public Sequence _breakpoint() {
    return add(new ZeroArgInst(202,getClassFile()));
  }

}
