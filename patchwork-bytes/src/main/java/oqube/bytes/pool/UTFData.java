package oqube.bytes.pool;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.Constants;

/**
 * This class represents a String constant data, that is an entry of type String
 * in the Constant Pool which references a UTF8 string
 */
public class UTFData extends ConstantData implements Constants {

    // the referenced string of this UTFData object
    private String ref;

    private UTFData(ConstantPool tbl, String s) {
        if (s == null)
            throw new NullPointerException("Adding a null string as UTFData");
        this.ref = s;
        if (ref == null)
            throw new NullPointerException("Cannot create null UTF constant");
        tbl.add(this); // adds a new UTF8 constant to the constant pool
    }

    /**
     * Parameter less constructor should only be used by ConstantPool to read
     * new constants from a data source
     */
    UTFData() {
    }

    /**
     * Constructs a new UTFData object which references the given string in the
     * given ConstantPool.
     */
    public static short create(ConstantPool tbl, String s) {
        UTFData data;
        /* verifies the uniqueness of the string */
        if ((data = tbl.getUTFData(s)) == null) data = new UTFData(tbl, s);
        return data.getIndex();
    }

    /**
     * Returns the referenced string
     */
    public String toString() {
        return ref;
    }

    /**
     * Writes this UTFData to the given Stream
     */
    public void write(DataOutputStream os) throws java.io.IOException {
        os.writeByte(CONSTANT_UTF8);
        // transform string into utf
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeUTF(ref);
        } catch (IOException ex) {

        }
        os.write(bos.toByteArray());
    }

    /**
     * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
     */
    public void read(DataInputStream in) throws IOException {
        // assumes CONSTANT_UTF8 already read
        this.ref = in.readUTF();
    }

    /**
     * @see fr.norsys.klass.ConstantPoolEntry#insertInPool(ConstantPool)
     */
    public void insertInPool(ConstantPool pool) {
        setConstantPool(pool);
        pool.hash(this);
    }

    @Override
    public Object getValue() {
        return ref;
    }

}
