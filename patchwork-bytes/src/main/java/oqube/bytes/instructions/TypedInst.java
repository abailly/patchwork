package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.TypeHelper;
import oqube.bytes.pool.ClassData;
import oqube.bytes.pool.IntData;
import oqube.bytes.pool.MethodRefData;

/**
 * This interface is used to produce specialized sequence of codes dependent on
 * the type of the variables. It is implemented by various constructs : load,
 * store, adn the like
 */
public class TypedInst implements Opcodes {

  public static Instruction load(Class c, ClassFile cf,int idx) {
    int opcode;
    if (c.isPrimitive()) {
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
      case 'C':
      case 'Z':
      case 'B':
      case 'S':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_iload_0,cf);
        case 1:
          return new ZeroArgInst(opc_iload_1,cf);
        case 2:
          return new ZeroArgInst(opc_iload_2,cf);
        case 3:
          return new ZeroArgInst(opc_iload_3,cf);
        default:
          return new OneArgInst(opc_iload, cf,(byte) idx);
        }
      case 'J':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_lload_0,cf);
        case 1:
          return new ZeroArgInst(opc_lload_1,cf);
        case 2:
          return new ZeroArgInst(opc_lload_2,cf);
        case 3:
          return new ZeroArgInst(opc_lload_3,cf);
        default:
          return new OneArgInst(opc_lload,cf, (byte) idx);
        }
      case 'F':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_fload_0,cf);
        case 1:
          return new ZeroArgInst(opc_fload_1,cf);
        case 2:
          return new ZeroArgInst(opc_fload_2,cf);
        case 3:
          return new ZeroArgInst(opc_fload_3,cf);
        default:
          return new OneArgInst(opc_fload,cf, (byte) idx);
        }
      case 'D':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_dload_0,cf);
        case 1:
          return new ZeroArgInst(opc_dload_1,cf);
        case 2:
          return new ZeroArgInst(opc_dload_2,cf);
        case 3:
          return new ZeroArgInst(opc_dload_3,cf);
        default:
          return new OneArgInst(opc_fload,cf, (byte) idx);
        }
      }
    } else
      // by default, loads a reference
      switch (idx) {
      case 0:
        return new ZeroArgInst(opc_aload_0,cf);
      case 1:
        return new ZeroArgInst(opc_aload_1,cf);
      case 2:
        return new ZeroArgInst(opc_aload_2,cf);
      case 3:
        return new ZeroArgInst(opc_aload_3,cf);
      default:
        return new OneArgInst(opc_aload,cf, (byte) idx);
      }
    return null;
  }

  public static Instruction store(Class c,ClassFile cf, int idx) {
    int opcode;
    if (c.isPrimitive()) {
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
      case 'C':
      case 'Z':
      case 'B':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_istore_0,cf);
        case 1:
          return new ZeroArgInst(opc_istore_1,cf);
        case 2:
          return new ZeroArgInst(opc_istore_2,cf);
        case 3:
          return new ZeroArgInst(opc_istore_3,cf);
        default:
          return new OneArgInst(opc_istore,cf, (byte) idx);
        }
      case 'J':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_lstore_0,cf);
        case 1:
          return new ZeroArgInst(opc_lstore_1,cf);
        case 2:
          return new ZeroArgInst(opc_lstore_2,cf);
        case 3:
          return new ZeroArgInst(opc_lstore_3,cf);
        default:
          return new OneArgInst(opc_lstore,cf, (byte) idx);
        }
      case 'F':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_fstore_0,cf);
        case 1:
          return new ZeroArgInst(opc_fstore_1,cf);
        case 2:
          return new ZeroArgInst(opc_fstore_2,cf);
        case 3:
          return new ZeroArgInst(opc_fstore_3,cf);
        default:
          return new OneArgInst(opc_fstore,cf, (byte) idx);
        }
      case 'D':
        switch (idx) {
        case 0:
          return new ZeroArgInst(opc_dstore_0,cf);
        case 1:
          return new ZeroArgInst(opc_dstore_1,cf);
        case 2:
          return new ZeroArgInst(opc_dstore_2,cf);
        case 3:
          return new ZeroArgInst(opc_dstore_3,cf);
        default:
          return new OneArgInst(opc_fstore,cf, (byte) idx);
        }
      }
    } else
      // by default, stores a reference
      switch (idx) {
      case 0:
        return new ZeroArgInst(opc_astore_0,cf);
      case 1:
        return new ZeroArgInst(opc_astore_1,cf);
      case 2:
        return new ZeroArgInst(opc_astore_2,cf);
      case 3:
        return new ZeroArgInst(opc_astore_3,cf);
      default:
        return new OneArgInst(opc_astore,cf, (byte) idx);
      }
    return null;
  }

  /**
   * generates an array load instruction based on the base type of the array
   */
  public static Instruction aload(Class c,ClassFile cf) {
    if (c.isPrimitive())
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
        return new ZeroArgInst(opc_iaload,cf);
      case 'Z':
      case 'B':
        return new ZeroArgInst(opc_baload,cf);
      case 'F':
        return new ZeroArgInst(opc_faload,cf);
      case 'D':
        return new ZeroArgInst(opc_daload,cf);
      case 'J':
        return new ZeroArgInst(opc_laload,cf);
      case 'C':
        return new ZeroArgInst(opc_caload,cf);
      case 'S':
        return new ZeroArgInst(opc_saload,cf);
      default:
        // never get there
        return null;
      }
    else
      return new ZeroArgInst(opc_aaload,cf);
  }

  /**
   * generates an array load instruction based on the base type of the array
   */
  public static Instruction astore(Class c,ClassFile cf) {
    if (c.isPrimitive())
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
        return new ZeroArgInst(opc_iastore,cf);
      case 'Z':
      case 'B':
        return new ZeroArgInst(opc_bastore,cf);
      case 'F':
        return new ZeroArgInst(opc_fastore,cf);
      case 'D':
        return new ZeroArgInst(opc_dastore,cf);
      case 'J':
        return new ZeroArgInst(opc_lastore,cf);
      case 'C':
        return new ZeroArgInst(opc_castore,cf);
      case 'S':
        return new ZeroArgInst(opc_sastore,cf);
      default:
        // never get there
        return null;
      }
    else
      return new ZeroArgInst(opc_aastore,cf);
  }

  /**
   * returns a sequence of instruction to encapsulate an object of type type in
   * an Object reference. The data to encapsulate is assumed to lay on top of
   * the stack Needs a ClassFile to generate constant entries
   */
  public static Instruction object(ClassFile cf, Class c) {
    Sequence seq = new Sequence(cf);
    short d, m;
    if (c.isPrimitive()) {
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Integer");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Integer",
            "<init>", "(I)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      // we've got an initialized integer on the stack
      case 'Z':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Boolean");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Boolean",
            "<init>", "(Z)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'F':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Float");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Float",
            "<init>", "(F)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'D':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Double");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Double",
            "<init>", "(D)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x2,cf));
        seq.add(new ZeroArgInst(opc_dup_x2,cf));
        seq.add(new ZeroArgInst(opc_pop,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'J':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Long");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Long",
            "<init>", "(J)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x2,cf));
        seq.add(new ZeroArgInst(opc_dup_x2,cf));
        seq.add(new ZeroArgInst(opc_pop,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'S':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Short");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Short",
            "<init>", "(S)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'C':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Character");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Character",
            "<init>", "(C)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      case 'B':
        d = ClassData.create(cf.getConstantPool(), "java/lang/Byte");
        m = MethodRefData.create(cf.getConstantPool(), "java/lang/Byte",
            "<init>", "(B)V");
        seq.add(new TwoArgInst(opc_new,cf, d));
        seq.add(new ZeroArgInst(opc_dup_x1,cf));
        seq.add(new ZeroArgInst(opc_swap,cf));
        seq.add(new TwoArgInst(opc_invokespecial,cf, m));
        return seq;
      }
    } else
      // nothing to do. The value on top of stack is already a reference
      return null;
    return seq;
  }

  /**
   * Load a constant int on the stack
   */
  public static Instruction constInt(ClassFile cf, int i) {

    switch (i) {
    case -1:
      return new ZeroArgInst(opc_iconst_m1,cf);
    case 0:
      return new ZeroArgInst(opc_iconst_0,cf);
    case 1:
      return new ZeroArgInst(opc_iconst_1,cf);
    case 2:
      return new ZeroArgInst(opc_iconst_2,cf);
    case 3:
      return new ZeroArgInst(opc_iconst_3,cf);
    case 4:
      return new ZeroArgInst(opc_iconst_4,cf);
    case 5:
      return new ZeroArgInst(opc_iconst_5,cf);
    default:
      if ((i > -128) && (i < 128))
        return new OneArgInst(opc_bipush,cf, (byte) i);
      else if ((i > -32768) && (i < 32768))
        return new TwoArgInst(opc_sipush,cf, (short) i);
      else {
        short d = IntData.create(cf.getConstantPool(), i);
        if (d < 256)
          return new OneArgInst(opc_ldc,cf, (byte) d);
        else
          return new TwoArgInst(opc_ldc_w,cf, d);
      }
    }
  }

  /**
   * stores initial default value according to variable type
   */
  public static Instruction init(ClassFile cf, Class c) {
    if (c.isPrimitive()) {
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
      case 'C':
      case 'Z':
      case 'B':
      case 'S':
        return new ZeroArgInst(opc_iconst_0,cf);
      case 'F':
        return new ZeroArgInst(opc_fconst_0,cf);
      case 'D':
        return new ZeroArgInst(opc_dconst_0,cf);
      case 'J':
        return new ZeroArgInst(opc_lconst_0,cf);
      default:
        // never get there
        return null;
      }
    } else
      // nothing to do. The value on top of stack is already a reference
      return new ZeroArgInst(opc_aconst_null,cf);
  }

  /**
   * produces return statement
   */
  public static Instruction returnInst(ClassFile cf, Class c) {
    if (c.isPrimitive()) {
      switch (TypeHelper.getInternalName(c).charAt(0)) {
      case 'I':
      case 'C':
      case 'Z':
      case 'B':
      case 'S':
        return new ZeroArgInst(opc_ireturn,cf);
      case 'F':
        return new ZeroArgInst(opc_freturn,cf);
      case 'D':
        return new ZeroArgInst(opc_dreturn,cf);
      case 'J':
        return new ZeroArgInst(opc_lreturn,cf);
      default:
        // never get there
        return null;
      }
    } else {// nothing to do. The value on top of stack is already a reference
      return new Sequence(cf)._areturn();
    }
  }

}

