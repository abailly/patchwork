/*
 * Created on Jun 10, 2003
 * Copyright 2003 Arnaud Bailly
 * $Log: SourceFileAttribute.java,v $
 * Revision 1.3  2004/06/29 15:27:00  bailly
 * correction attribut LineNumberTable et SourceFile
 * correction null pointer sur ClassFile
 * correction calcul de la longueur dans CodeAttribute
 *
 * Revision 1.2  2004/05/09 22:09:24  bailly
 * Added frame display capability to ControlGraph display
 * Corrected control graph construction
 * added exceptions block handling, tableswitch and lookupswitch
 * TODO : correct implementation of branch splitting and ordering of blocks
 *
 * Revision 1.1  2003/06/10 05:05:49  bailly
 * Added source file attribute
 *
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.pool.UTFData;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * A class holding informations about the originating sourcefile of a ClassFile
 * 
 * @author bailly
 * @version $Id: SourceFileAttribute.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class SourceFileAttribute extends AttributeFileInfo {

  public static final String attname = "SourceFile";

  private String sourceFile;

  private short index;

  public SourceFileAttribute() {
  }

  /**
   * @param cf
   * @param name
   */
  public SourceFileAttribute(ClassFile cf) {
    super(cf, attname);
  }

  public void setSourceFile(String sfile) {
    this.sourceFile = sfile;
    this.index = UTFData.create(classFile.getConstantPool(), sfile);
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public int getIndex() {
    return index;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#write(java.io.DataOutputStream)
   */
  public void write(DataOutputStream dos) throws IOException {
    dos.writeShort(nameIndex);
    dos.writeInt(2);
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "attribute size=" + getLength()));
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "SourceFile"));
    dos.writeShort(index);
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "SourceFile"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "SourceFile"));
    index = in.readShort();
    UTFData data = (UTFData) classFile.getConstantPool().getEntry(index);
    sourceFile = data.toString();
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "SourceFile"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return 2;
  }

}
