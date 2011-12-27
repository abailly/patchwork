package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.TypeHelper;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.pool.ClassData;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * This class implements the AttributeFileInfo abstract class for the code
 * attibute found in java method
 */
public class CodeAttribute extends AttributeFileInfo implements Opcodes {

  // max stack size
  private short maxstack = 0;

  // max number of locals - default = 2 for this and caller
  private short maxlocals = 0;

  // length of code
  private int size = 0;

  // instructions
  private List<Instruction> instructions = new LinkedList<Instruction>();

  // local variables map
  // stores string-index couples
  private Map vars = new HashMap();

  // current local variable index
  private short idx = 0;

  // exceptions - mapped from start instruction
  private List exceptions = new LinkedList();

  // attributes
  private List attributes = new LinkedList();

  public class ExceptionTableEntry {
    // instructions where the exception starts, end and where
    // handler begins
    short start, end, jump;

    // class index
    short classIndex;

    /* for byte reading */
    private ExceptionTableEntry() {
      /* empty */
    }

    /**
     * @return
     */
    public short getClassIndex() {
      return classIndex;
    }

    /**
     * @return
     */
    public short getEnd() {
      return end;
    }

    /**
     * @return
     */
    public short getJump() {
      return jump;
    }

    /**
     * @return
     */
    public short getStart() {
      return start;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return "["
          + start
          + ","
          + end
          + "]: "
          + (classIndex == 0 ? " any" : classFile.getConstantPool().getEntry(
              classIndex).toString()) + " -> " + jump;
    }
  }

  // returns the current size of this code fragment
  public int size() {
    return size;
  }

  public int maxlocals() {
    return maxlocals;
  }

  /**
   * Constructs a CodeAttribute object with given number of args and class file
   * objects
   */
  public CodeAttribute(ClassFile cf, short args) {
    super(cf, "Code");
    this.maxlocals = this.idx = args;
  }

  public CodeAttribute(ClassFile cf) {
    super(cf, "Code");
  }

  public CodeAttribute() {
  }

  /**
   * Adds a single Instruction to the list
   */
  public void add(Instruction i) {
    if (i instanceof Sequence) {
      Iterator it = ((Sequence) i).iterator();
      while (it.hasNext())
        add((Instruction) it.next());
      return;
    }
    instructions.add(i);
    i.pc = (short) size;
    size += i.size();
    if (maxstack + i.maxstack > maxstack)
      maxstack += i.maxstack;
  }

  /**
   * Adds a group of instructions to the list
   */
  public void add(Instruction[] i) {
    int max = i.length;
    for (int j = 0; j < max; j++)
      add(i);
  }

  /**
   * Adds a value to the stack
   */
  public void incStack(int i) {
    maxstack += i;
  }

  /**
   * Adds a local variable
   */
  public void incLocals() {
    if (idx > maxlocals)
      maxlocals = (short) idx;
  }

  /**
   * Add a local variale name
   */
  public short addLocalVar(String name) {
    vars.put(name, new Short(idx++));
    incLocals();
    return idx;
  }

  /**
   * Add a local variale name and type
   */
  public int addLocalVar(String name, Class type) {
    switch (TypeHelper.getInternalName(type).charAt(0)) {
    case 'J':
    case 'D': // these take two slots
      vars.put(name, new Short(idx));
      idx += 2;
      break;
    default:
      vars.put(name, new Short(idx++));
      break;
    }
    incLocals();
    return idx;
  }

  /**
   * retract off number of local variables from this code
   */
  public short retractVars(int off) {
    this.idx -= off;
    return idx;
  }

  /**
   * Get a local variable index from name returns -1 if unknown
   */
  public int getLocalVar(String name) {
    Object o;
    // if index is >= idx, then this variable has been retracted
    if (((o = vars.get(name)) != null) && (((Short) o).shortValue() < idx))
      return ((Short) o).shortValue();
    return -1;
  }

  /**
   * Allocates a slot for a temporary variable
   */
  public short newTemp() {
    maxlocals++;
    return ++idx;
  }

  // /**
  // * This method calculates the maximum stack value of
  // * this method. If the algorithm fails to determine a safe maximum,
  // * or a stack underflow is found, a ClassFormatException is thrown.
  // *
  // * @param insts an array of bytecodes
  // * @return the maximum value of the stack
  // */
  // private int getMaximumStack() {
  // Iterator it = inst.iterator();
  // Map st = new HashMap(); // map from instructions to stack value
  // LinkedHashMap ad = new LinkedHashMap(); // map from addresses to
  // instruction
  // int runstack = 0;
  // int addr = 0;
  // // store addresses of instructions
  // while(it.hasNext()) {
  // Instruction inst = (Instruction) it.next();
  // ad.put(new Integer(runstack),inst);
  // runstack += inst.size();
  // }
  // // iterate over addresses map
  // it = ad.keySet().iterator();
  // while(it.hasNext()) {
  // Integer objaddr = ((Integer)it.next());
  // int addr = objaddr.intValue();
  // Instruction inst = ad.get(objaddr);
  // int opcode = inst.opcode();
  // runstack += inst.maxstack();
  // int storedstack = -1;
  // try {
  // storedstack = ((Integer)st.get(inst)).intValue();
  // if(storedstack < runstack)
  // throw new ClassFormatError("Stack overflow for instruction "+inst);
  // else if(storedstack > runstack)
  // throw new ClassFormatError("Stack underflow for instruction "+inst);
  // else // continue
  // continue;
  // }catch(NullPointerException ex) {
  // storedstack = runstack;
  // st.put(inst,new Integer(storedstack));
  // }
  //	    
  // if(opcode
  // }

  public void write(java.io.DataOutputStream dos) throws java.io.IOException {
    int j = 0;
    dos.writeShort(nameIndex);
    dos.writeInt(getLength());
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "attribute size=" + getLength()));
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.START, "CodeAttribute"));
    Iterator it = instructions.iterator();
    dos.writeShort(maxstack);
    dos.writeShort(maxlocals);
    dos.writeInt(size);
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.ATOMIC, "code size=" + size));
    while (it.hasNext()) {
      Instruction is = (Instruction) it.next();
      is.write(dos);
      j += is.size();
    }
    // write exception table
    dos.writeShort(exceptions.size());
    it = exceptions.iterator();
    while (it.hasNext()) {
      ExceptionTableEntry entry = (ExceptionTableEntry) it.next();
      dos.writeShort(entry.start);
      dos.writeShort(entry.end);
      dos.writeShort(entry.jump);
      dos.writeShort(entry.classIndex);
    }
    // write attributes table
    dos.writeShort(attributes.size());
    it = attributes.iterator();
    while (it.hasNext()) {
      AttributeFileInfo afi = (AttributeFileInfo) it.next();
      afi.write(dos);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.WRITE,
        ClassFileIOEvent.END, "CodeAttribute"));

  }

  /**
   * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.START, "CodeAttribute"));
    Map instMap = new HashMap();
    maxstack = in.readShort();
    maxlocals = in.readShort();
    size = in.readInt();
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.ATOMIC, "code size=" + size));
    // read instructions
    int i = 0;
    for (i = 0; i < size;) {
      Instruction inst = Instruction.read(i, in, classFile);
      inst.pc = (short) i;
      instMap.put(new Integer(i), inst);
      instructions.add(inst);
      i += inst.size();
    }
    if (i != size) {
      throw new IOException("Incorrect instructions size : expected " + size
          + ", found " + i);
    }
    // read exceptions
    int esz = in.readShort();
    exceptions = new LinkedList();
    for (i = 0; i < esz; i++) {
      ExceptionTableEntry entry = new ExceptionTableEntry();
      entry.start = in.readShort();
      entry.end = in.readShort();
      entry.jump = in.readShort();
      entry.classIndex = in.readShort();
      exceptions.add(entry);
    }
    // read attributes
    esz = in.readShort();
    attributes = new LinkedList();
    for (i = 0; i < esz; i++) {
      AttributeFileInfo afi = AttributeFileInfo.read(classFile, in);
      attributes.add(afi);
    }
    classFile.dispatch(new ClassFileIOEvent(classFile, ClassFileIOEvent.READ,
        ClassFileIOEvent.END, "CodeAttribute"));
  }

  /**
   * Sets the number of local variables used
   */
  public void setMaxlocals(short i) {
    this.maxlocals = i;
  }

  /**
   * Returns a list of all instructions in this code attribute This list
   * contains only basic instructions, not sequences which are flattened
   */
  public List getAllInstructions() {
    List l = new ArrayList();
    Iterator it = instructions.iterator();
    while (it.hasNext()) {
      Instruction ins = (Instruction) it.next();
      if (ins instanceof Sequence)
        l.addAll(((Sequence) ins).getInstructions());
      else
        l.add(ins);
    }
    return l;
  }

  /**
   * @return
   */
  public List getExceptions() {
    return exceptions;
  }

  public void makeExceptionEntry(String exname, short start, short end,
      short jump) {
    ExceptionTableEntry entry = new ExceptionTableEntry();
    entry.start = start;
    entry.end = end;
    entry.jump = jump;
    short ename = ClassData.create(getClassFile().getConstantPool(), exname);
    entry.classIndex = ename;
    exceptions.add(entry);
  }

  public void makeExceptionEntry(short eidx, short start, short end, short jump) {
    ExceptionTableEntry entry = new ExceptionTableEntry();
    entry.start = start;
    entry.end = end;
    entry.jump = jump;
    entry.classIndex = eidx;
    exceptions.add(entry);
  }

  public void addAttribute(AttributeFileInfo attr) {
    if (attributes == null)
      attributes = new ArrayList();
    attributes.add(attr);
  }

  /**
   * Returns the current length of this atttribute
   * 
   */
  public int getLength() {
    int ln = 0;
    ln += 12; /* constants */
    ln += getCodeLength();
    /* add exceptions number */
    ln += exceptions.size() * 8;
    Iterator it = attributes.iterator();
    while (it.hasNext()) {
      AttributeFileInfo afi = (AttributeFileInfo) it.next();
      ln += afi.getLength() + 6;
    }
    return ln;
  }

  /**
   * Return length of bytecode array
   * 
   * @return
   */
  public int getCodeLength() {
    int ln = 0;
    /* add instruction size */
    Iterator it = instructions.iterator();
    while (it.hasNext()) {
      Instruction ins = (Instruction) it.next();
      ln += ins.size();
    }
    return ln;
  }

  /**
   * @param string
   * @return
   */
  public AttributeFileInfo getAttribute(String string) {
    for (Iterator i = attributes.iterator(); i.hasNext();) {
      AttributeFileInfo attr = (AttributeFileInfo) i.next();
      if (attr.getName().equals(string))
        return attr;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    final String EOL = System.getProperty("line.separator");
    for (Instruction ins : instructions) {
      sb.append(ins.pc).append(' ').append(ins).append(EOL);
    }
    return sb.append(EOL).append(exceptions).append(EOL).append(vars)
        .toString();
  }

}