package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent One-arg instructions, that is
 * instructions that takes one byte argument
 */
public class OneArgInst extends Instruction {

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    return super.clone();
  }

  private byte arg;

  public OneArgInst(int opcode, ClassFile cf, byte arg) {
    super(opcode, cf);
    this.arg = arg;
    size = 2;
  }

  public byte arg() {
    return arg;
  }

  public void write(java.io.DataOutputStream os) throws java.io.IOException {
    os.writeByte(op_code);
    getClassFile().dispatch(new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
            ClassFileIOEvent.ATOMIC, "opcode=" + op_code));
    os.writeByte(arg);
    getClassFile().dispatch(new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
            ClassFileIOEvent.ATOMIC, "1 arg=" + arg));
  }

  /*
   * (non-Javadoc) [188] newarray 2 0 T_BOOLEAN 4 T_CHAR 5 T_FLOAT 6 T_DOUBLE 7
   * T_BYTE 8 T_SHORT 9 T_INT 10 T_LONG 11
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(super.toString()).append(' ');
    if (opcode() == opc_newarray) {
      switch (arg) {
      case Constants.T_BOOLEAN:
        sb.append("[boolean");
        break;
      case Constants.T_CHAR:
        sb.append("[char");
        break;
      case Constants.T_FLOAT:
        sb.append("[float");
        break;
      case Constants.T_DOUBLE:
        sb.append("[double");
        break;
      case Constants.T_BYTE:
        sb.append("[byte");
        break;
      case Constants.T_SHORT:
        sb.append("[short");
        break;
      case Constants.T_INT:
        sb.append("[int");
        break;
      case Constants.T_LONG:
        sb.append("[long");
        break;
      }
    } else 
      sb.append(arg);
    return sb.toString();
  }

}