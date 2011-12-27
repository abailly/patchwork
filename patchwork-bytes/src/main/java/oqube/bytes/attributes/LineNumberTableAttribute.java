/*
 * Created on Jun 10, 2003
 * Copyright 2003 Arnaud Bailly
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * @author bailly
 * @version $Id: LineNumberTableAttribute.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class LineNumberTableAttribute extends AttributeFileInfo {

  public static final String attname = "LineNumberTable";

  /*
   * These are sorted in ascending pc then line order.
   */
  class PcAndLine implements Comparable {
    short pc;

    short line;

    PcAndLine(int pc, int line) {
      this.pc = (short) pc;
      this.line = (short) line;
    }

    public int compareTo(Object arg0) {
      PcAndLine ln = (PcAndLine) arg0;
      if (ln.pc > pc)
        return -1;
      else if (ln.pc < pc)
        return 1;
      else
        return (ln.line < line) ? 1 : (ln.line > line) ? -1 : 0;
    }
  }

  private ArrayList lines = new ArrayList();

  public LineNumberTableAttribute() {
  }

  /**
   * @param cf
   * @param name
   */
  public LineNumberTableAttribute(ClassFile cf) {
    super(cf, attname);
  }

  public void addLineInfo(int pc, int line) {
    lines.add(new PcAndLine(pc, line));
  }

  /**
   * @return a n x 2 two dimensional array representing this line table
   *         information.
   */
  public int[][] getLineInfo() {
    Collections.sort(lines);
    int[][] ret = new int[lines.size()][2];
    int j = 0;
    for (Iterator i = lines.iterator(); i.hasNext(); j++) {
      PcAndLine pc = (PcAndLine) i.next();
      ret[j][0] = pc.pc;
      ret[j][1] = pc.line;
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#write(java.io.DataOutputStream)
   */
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(nameIndex);
    out.writeInt(2 + lines.size() * 4);
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "attribute size=" + getLength()));
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "LineNumber"));
    out.writeShort(lines.size());
    Iterator it = lines.iterator();
    while (it.hasNext()) {
      PcAndLine pc = (PcAndLine) it.next();
      out.writeShort(pc.pc);
      out.writeShort(pc.line);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "LineNumber"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "LineNumber"));
    short num = in.readShort();
    for (; num > 0; num--) {
      short pc = in.readShort();
      short line = in.readShort();
      lines.add(new PcAndLine(pc, line));
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "LineNumber"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.AttributeFileInfo#getLength()
   */
  public int getLength() {
    return lines.size() * 4 + 2;
  }

}
/*
 * $Log: LineNumberTableAttribute.java,v $ Revision 1.3 2004/06/29 15:27:00
 * bailly correction attribut LineNumberTable et SourceFile correction null
 * pointer sur ClassFile correction calcul de la longueur dans CodeAttribute
 * 
 * Revision 1.2 2004/05/09 22:09:24 bailly Added frame display capability to
 * ControlGraph display Corrected control graph construction added exceptions
 * block handling, tableswitch and lookupswitch TODO : correct implementation of
 * branch splitting and ordering of blocks
 * 
 * Revision 1.1 2003/06/10 04:50:42 bailly Added LineNumberTableAttribute class
 * 
 */
