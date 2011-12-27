package oqube.bytes.struct;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.UTFData;

/**
 * This class encapsulates the necessary data to generate a field
 * in class file.
 */
public class FieldFileInfo implements Constants, ClassFileComponent {
    // name
    private short nameIndex;

    // type
    private short typeIndex;

    // attributes
    private List attributes;

    // underlying class file
    private ClassFile classFile;

    // access flags
    private short flags = (short) ACC_PRIVATE;

    public FieldFileInfo(ClassFile cf) {
        this.classFile = cf;
    }

    public FieldFileInfo() {
    }

    /**
     * Sets the name of this method 
     */
    public void setName(String name) {
        this.nameIndex = UTFData.create(classFile.getConstantPool(), name);
    }

    /**
     * Sets the type of this method
     */
    public void setType(String sig) {
        this.typeIndex = UTFData.create(classFile.getConstantPool(), sig);
    }

    /**
     * Sets the private flag
     */
    public void setPrivate() {
        flags |= ACC_PRIVATE;
    }

    /**
     * Sets the public flag
     */
    public void setPublic() {
        flags |= ACC_PUBLIC;
    }

    /**
     * Sets the protected flag
     */
    public void setProtected() {
        flags |= ACC_PROTECTED;
    }

    /**
     * Adds an attribute to this method
     */
    public void addAttribute(AttributeFileInfo info) {
        if (info == null)
            return;
        if (attributes == null)
            attributes = new LinkedList();
        attributes.add(info);
    }

    public void write(java.io.DataOutputStream dos) throws java.io.IOException {
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.START, "FieldFileInfo"));
        dos.writeShort(flags);
        dos.writeShort(nameIndex);
        dos.writeShort(typeIndex);
        if (attributes == null || attributes.size() == 0)
            dos.writeShort(0);
        else {
            dos.writeShort(attributes.size());
            Iterator it = attributes.iterator();
            while (it.hasNext())
                ((AttributeFileInfo) it.next()).write(dos);
        }
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
                ClassFileIOEvent.END, "FieldFileInfo"));
    }

    /**
     * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
     */
    public void read(DataInputStream in) throws IOException {
       classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.START, "FieldFileInfo"));
        flags = in.readShort();
        nameIndex = in.readShort();
        typeIndex = in.readShort();
        int acount = in.readShort();
        attributes = new LinkedList();
        for (int i = 0; i < acount; i++)
            addAttribute(AttributeFileInfo.read(classFile, in));
        classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
                ClassFileIOEvent.END, "FieldFileInfo"));
    }

    /**
     * Returns the attributes.
     * @return List
     */
    public List getAttributes() {
        return attributes;
    }

    /**
     * Returns the classFile.
     * @return ClassFile
     */
    public ClassFile getClassFile() {
        return classFile;
    }

    /**
     * Returns the flags.
     * @return short
     */
    public short getFlags() {
        return flags;
    }

    /**
     * Returns the nameIndex.
     * @return short
     */
    public short getNameIndex() {
        return nameIndex;
    }

    /**
     * Returns the typeIndex.
     * @return short
     */
    public short getTypeIndex() {
        return typeIndex;
    }

    /**
     * Sets the attributes.
     * @param attributes The attributes to set
     */
    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    /**
     * Sets the classFile.
     * @param classFile The classFile to set
     */
    public void setClassFile(ClassFile classFile) {
        this.classFile = classFile;
    }

    /**
     * Sets the flags.
     * @param flags The flags to set
     */
    public void setFlags(short flags) {
        this.flags = flags;
    }

    /**
     * Sets the nameIndex.
     * @param nameIndex The nameIndex to set
     */
    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    /**
     * Sets the typeIndex.
     * @param typeIndex The typeIndex to set
     */
    public void setTypeIndex(short typeIndex) {
        this.typeIndex = typeIndex;
    }

    /**
     * 
     */
    public void setStatic() {
        this.flags |= ACC_STATIC;
    }

    /**
     * @return
     */
    public String getName() {
        return classFile.getConstantPool().getEntry(nameIndex).toString();
    }

    public String getType() {
        return classFile.getConstantPool().getEntry(typeIndex).toString();
    }

}
