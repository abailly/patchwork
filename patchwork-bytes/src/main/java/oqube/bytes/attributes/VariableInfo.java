/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Variable information for debuggers.
 * This structure is used both in {@link oqube.bytes.attributes.LocalVariableTableAttribute} 
 * and in {@link oqube.bytes.attributes.LocalVariableTypeTableAttribute} with different
 * meaning for <code>descriptor_index</code>.
 * 
 * @author nono
 *
 */
public class VariableInfo {

  static int LENGTH = 10;
  
    short start_pc;

    short length;

    short name_index;

    short descriptor_index;

    short index;

    public short getDescriptor_index() {
        return descriptor_index;
    }

    public short getIndex() {
        return index;
    }

    public short getLength() {
        return length;
    }

    public short getName_index() {
        return name_index;
    }

    public short getStart_pc() {
        return start_pc;
    }

    /**
     * @param in
     */
    public void read(DataInputStream in) throws IOException {
        start_pc = in.readShort();
        length = in.readShort();
        name_index = in.readShort();
        descriptor_index = in.readShort();
        index = in.readShort();
    }

    /**
     * @param out
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(start_pc);
        out.writeShort(length);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
        out.writeShort(index);
    }

	/**
	 * @param descriptor_index The descriptor_index to set.
	 */
	public void setDescriptor_index(short descriptor_index) {
		this.descriptor_index = descriptor_index;
	}

	/**
	 * @param index The index to set.
	 */
	public void setIndex(short index) {
		this.index = index;
	}

	/**
	 * @param length The length to set.
	 */
	public void setLength(short length) {
		this.length = length;
	}

	/**
	 * @param name_index The name_index to set.
	 */
	public void setName_index(short name_index) {
		this.name_index = name_index;
	}

	/**
	 * @param start_pc The start_pc to set.
	 */
	public void setStart_pc(short start_pc) {
		this.start_pc = start_pc;
	}

}