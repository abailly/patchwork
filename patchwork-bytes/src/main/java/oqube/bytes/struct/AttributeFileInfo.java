package oqube.bytes.struct;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.UTFData;

/**
 * This interface groups class file attributes : code, exceptions, constant
 * values,... Each subclass should represent a single attribute, writable to
 * class file
 */
public abstract class AttributeFileInfo implements ClassFileComponent {

    private static final Logger log    = Logger.getLogger(AttributeFileInfo.class.getName());
    // the length of this attribute
    protected int               length = 0;

    // the name of this attribute
    protected short             nameIndex;

    // the class file
    protected ClassFile         classFile;

    public String getName() {
        return classFile.getConstantPool().getEntry(nameIndex).toString();
    }

    public AttributeFileInfo(ClassFile cf, String name) {
        this.classFile = cf;
        this.nameIndex = UTFData.create(cf.getConstantPool(), name);
    }

    public AttributeFileInfo() {
    }

    public void setLength(int l) {
        length = l;
    }

    public abstract void write(java.io.DataOutputStream dos)
            throws java.io.IOException;

    /**
     * this methods creates an AttributeFileInfo sub-class instance according to
     * the name of the read attribute. By convention , attributes class names
     * are the attribute name, with the first letter upper-cased and with the
     * suffix "Attribute" appended. Attribute names not containing dots are
     * assumed to be defined in the JVM spec and their classes are defined in
     * this package. Attributes names containing dots are assumed to be fully
     * qualified class names (without "Attribute" suffix) are used asis.
     * 
     * @param in
     *            the input stream to read data from
     * @return an initialized attribute
     */
    public static AttributeFileInfo read(ClassFile cf, DataInputStream in)
            throws IOException {
        // read name and length
        short nameIndex = in.readShort();
        int len = in.readInt();
        cf.dispatch(new ClassFileIOEvent(cf,
                                         ClassFileIOEvent.READ,
                                         ClassFileIOEvent.ATOMIC,
                                         "attribute size=" + len));
        Class cls = null;
        String attname = null;
        try {
            attname = cf.getConstantPool().getEntry(nameIndex).toString();
            // try to find a class for this attribute
            AttributeFileInfo info;
            if (attname.indexOf('.') == -1)
                attname = "oqube.bytes.attributes." + attname + "Attribute";
            cls = Class.forName(attname);
            info = (AttributeFileInfo) cls.newInstance();
            info.setClassFile(cf);
            info.setNameIndex(nameIndex);
            info.setLength(len);
            info.read(in);
            return info;
        } catch (ClassNotFoundException ex) {
            log.warning("Unknown attirbute " + attname + " in class "
                    + cf.getClassFileInfo().getName() + ", skipping " + len
                    + " bytes");
            // attribute unknown - skip len bytes in stream
            in.skipBytes(len);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error constructing attribute :"
                    + ex.getMessage() + " in class "
                    + cf.getClassFileInfo().getName());
            throw new IOException();
        }
    }

    /**
     * Returns the length.
     * 
     * @return int
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the classFile.
     * 
     * @return ClassFile
     */
    public ClassFile getClassFile() {
        return classFile;
    }

    /**
     * Returns the nameIndex.
     * 
     * @return short
     */
    public short getNameIndex() {
        return nameIndex;
    }

    /**
     * Sets the classFile.
     * 
     * @param classFile
     *            The classFile to set
     */
    public void setClassFile(ClassFile classFile) {
        this.classFile = classFile;
    }

    /**
     * Sets the nameIndex.
     * 
     * @param nameIndex
     *            The nameIndex to set
     */
    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

}
