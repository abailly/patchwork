package oqube.bytes.instructions;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;

/**
 * This class inherits Instruction to represent zero-arg instructions, that is
 * instructions that takes no args
 */
public class ZeroArgInst extends Instruction {

  public ZeroArgInst(int opcode,ClassFile cf) {
    super(opcode,cf);
    size = 1;
  }

  public void write(java.io.DataOutputStream dos) throws java.io.IOException {
    dos.writeByte(op_code);
    getClassFile().dispatch(new ClassFileIOEvent(getClassFile(), ClassFileIOEvent.WRITE,
            ClassFileIOEvent.ATOMIC, "opcode=" + op_code));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    return super.clone();
  }

}