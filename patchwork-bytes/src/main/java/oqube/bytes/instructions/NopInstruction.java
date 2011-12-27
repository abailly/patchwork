package oqube.bytes.instructions;

import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;

/**
 * A no-op instruction. This instruction may be used when instructions
 * are needed by API but not necessary for logical flow of control.
 * 
 * @author bailly
 * @version $Id: NopInstruction.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class NopInstruction extends Instruction {

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    return super.clone();
  }
	/**
	 * Constructor for NopInstruction.
	 */
	public NopInstruction(ClassFile cf) {
		super(Opcodes.opc_nop,cf);
	}

	/**
	 * @see fr.norsys.klass.Instruction#write(DataOutputStream)
	 */
	public void write(DataOutputStream os) throws IOException {
		/* nothing to do */
		os.writeByte(op_code);
	}

}
