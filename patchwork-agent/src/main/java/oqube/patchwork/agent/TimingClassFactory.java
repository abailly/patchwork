/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.attributes.LineNumberTableAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.loading.ClassFactory;
import oqube.bytes.loading.IncludeExclude;
import oqube.bytes.pool.FieldRefData;
import oqube.bytes.pool.InterfaceMethodData;
import oqube.bytes.pool.StringData;
import oqube.bytes.struct.MethodFileInfo;

public class TimingClassFactory implements ClassFactory {

  private Map<String, ClassFile> generated = new HashMap<String, ClassFile>();
  private final IncludeExclude includeExclude;

  public TimingClassFactory(TimingObserver notification, IncludeExclude includeExclude) {
    Observers.observers = notification;
    this.includeExclude = includeExclude;
  }

  public void addTodo(ClassFile input) {
    // TODO TimingClassFactory.addTodo method to implement
    throw new UnsupportedOperationException("TimingClassFactory.addTodo method not implemented");
  }

  public Map<String, ClassFile> getGenerated() {
    return generated;
  }

  public Map<String, ClassFile> getTodo() {
    // TODO TimingClassFactory.getTodo method to implement
    throw new UnsupportedOperationException("TimingClassFactory.getTodo method not implemented");
  }

  public ClassFile instrument(ClassFile cf) {
    if(isInterface(cf))
      return cf;
    /* lookup in generated classes cache */
    ClassFile instrumented = generated.get(cf.getClassFileInfo().getName());
    if(instrumented == null) {
      /* create new instrumented classfile as a copy of cf */
      instrumented = initializeFrom(cf);
      doInstrument(instrumented);
      // store
      generated.put(cf.getClassFileInfo().getName(), instrumented);
    }
    return instrumented;
  }

  private boolean isInterface(ClassFile cf) { // check class is concrete
    return (cf.getClassFileInfo().getFlags() & Constants.ACC_INTERFACE) > 0;
  }

  private ClassFile initializeFrom(ClassFile cf) {
    try {
      ClassFile inst = new ClassFile();
      /* write to a byte array the class file and read it back in new object */
      /* write all data */
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(bos);
      cf.write(dos);
      dos.flush();
      dos.close();
      bos.close();
      /* read all data */
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      DataInputStream dis = new DataInputStream(bis);
      inst.read(dis);
      return inst;
    } catch(IOException io) {
      throw new RuntimeException("Unexpected IOException :", io);
    }
  }

  private void doInstrument(ClassFile cf) {
    Collection<MethodFileInfo> allMethods = cf.getAllMethods();
    for(MethodFileInfo methodFileInfo : allMethods) {
      if(includeExclude.accept(methodFileInfo.getName()))
        instrumentMethod(cf, methodFileInfo);
    }
  }

  private void instrumentMethod(ClassFile cf, MethodFileInfo mfi) {
    CodeAttribute oldCode = mfi.getCodeAttribute();
    CodeAttribute newcode = new CodeAttribute(cf, (short)oldCode.maxlocals());
    if(mfi != null) {
      Sequence seq = new Sequence(cf);
      generateStartMethodCall(seq, cf, mfi.getName());
      List<Instruction> instructions = oldCode.getAllInstructions();
      Map<Instruction, Short> originalOffsets = extractInstructionsOffset(instructions);
      Map<Short, Instruction> reverseOriginalOffsets = reverseMap(originalOffsets);
      for(Instruction instruction : instructions) {
        switch(instruction.opcode()) {
        case Opcodes.opc_areturn:
        case Opcodes.opc_dreturn:
        case Opcodes.opc_ireturn:
        case Opcodes.opc_athrow:
        case Opcodes.opc_freturn:
        case Opcodes.opc_lreturn:
        case Opcodes.opc_return:
          generateEndMethodCall(seq, cf, mfi.getName());
          break;
        }
        seq.add(instruction);
      }
      newcode.add(seq);
      List<Instruction> newInstructions = newcode.getAllInstructions();
      Map<Instruction, Short> newOffsets = extractInstructionsOffset(newInstructions);
      fixJumps(newInstructions, reverseOriginalOffsets, newOffsets);
      fixExceptions(oldCode, newcode, reverseOriginalOffsets, newOffsets);
      fixLineNumbers(oldCode, newcode, reverseOriginalOffsets, newOffsets);
      mfi.setCode(newcode);
    }
  }

  private void fixLineNumbers(CodeAttribute code, CodeAttribute newcode, Map<Short, Instruction> reverseOriginalOffsets,
      Map<Instruction, Short> newOffsets) {
    LineNumberTableAttribute lines = (LineNumberTableAttribute)code.getAttribute(LineNumberTableAttribute.attname);
    if(lines == null)
      return;
    LineNumberTableAttribute nlines = new LineNumberTableAttribute(newcode.getClassFile());
    for(int[] line : lines.getLineInfo()) {
      int newLine = newOffsets.get(reverseOriginalOffsets.get((short)line[0]));
      nlines.addLineInfo(newLine, line[1]);
    }
    newcode.addAttribute(nlines);
  }

  private void fixExceptions(CodeAttribute code, CodeAttribute newcode, Map<Short, Instruction> reverseOriginalOffsets,
      Map<Instruction, Short> newOffsets) {
    for(Iterator i = code.getExceptions().iterator(); i.hasNext();) {
      CodeAttribute.ExceptionTableEntry exc = (CodeAttribute.ExceptionTableEntry)i.next();
      int newStart = newOffsets.get(reverseOriginalOffsets.get((short)exc.getStart()));
      int newEnd = newOffsets.get(reverseOriginalOffsets.get((short)exc.getEnd()));
      int newJump = newOffsets.get(reverseOriginalOffsets.get((short)exc.getJump()));
      newcode.makeExceptionEntry(exc.getClassIndex(), (short)newStart, (short)newEnd, (short)newJump);
    }
  }

  private void fixJumps(List<Instruction> newInstructions, Map<Short, Instruction> reverseOriginalOffsets, Map<Instruction, Short> newOffsets) {
    for(Instruction instruction : newInstructions) {
      if(instruction.isBranching() && instruction.targetPc() != Integer.MAX_VALUE && instruction.targetPc() != -1) {
        instruction.setTargetPc(newOffsets.get(reverseOriginalOffsets.get((short)instruction.targetPc())));
      }
    }
  }

  private Map<Short, Instruction> reverseMap(Map<Instruction, Short> originalOffsets) {
    Map<Short, Instruction> reverse = new HashMap<Short, Instruction>();
    for(Map.Entry<Instruction, Short> entry : originalOffsets.entrySet())
      reverse.put(entry.getValue(), entry.getKey());
    return reverse;
  }

  private Map<Instruction, Short> extractInstructionsOffset(List<Instruction> instructions) {
    Map<Instruction, Short> offsets = new HashMap<Instruction, Short>();
    short offset = 0;
    for(Instruction instruction : instructions) {
      offsets.put(instruction, offset);
      offset += instruction.size();
    }
    return offsets;
  }

  private void generateStartMethodCall(Sequence seq, ClassFile cf, String name) {
    short listener = FieldRefData.create(cf.getConstantPool(), "oqube/patchwork/agent/Observers", "observers",
        "Loqube/patchwork/agent/TimingObserver;");
    short mMethodStart = InterfaceMethodData.create(cf.getConstantPool(), "oqube/patchwork/agent/TimingObserver", "methodStart",
        "(Ljava/lang/String;Ljava/lang/String;)V");
    short classNameRef = StringData.create(cf.getConstantPool(), cf.getClassFileInfo().getName());
    short methodNameRef = StringData.create(cf.getConstantPool(), name);
    seq._getstatic(listener, cf)._ldc(classNameRef)._ldc(methodNameRef)._invokeinterface(mMethodStart, 3, cf);
  }

  private void generateEndMethodCall(Sequence seq, ClassFile cf, String methodName) {
    short listener = FieldRefData.create(cf.getConstantPool(), "oqube/patchwork/agent/Observers", "observers",
        "Loqube/patchwork/agent/TimingObserver;");
    short mMethodStart = InterfaceMethodData.create(cf.getConstantPool(), "oqube/patchwork/agent/TimingObserver", "methodEnd",
        "(Ljava/lang/String;Ljava/lang/String;)V");
    short classNameRef = StringData.create(cf.getConstantPool(), cf.getClassFileInfo().getName());
    short methodNameRef = StringData.create(cf.getConstantPool(), methodName);
    seq._getstatic(listener, cf)._ldc(classNameRef)._ldc(methodNameRef)._invokeinterface(mMethodStart, 3, cf);
  }

  public void setTodo(Map<String, ClassFile> todo) {
    // TODO TimingClassFactory.setTodo method to implement
    throw new UnsupportedOperationException("TimingClassFactory.setTodo method not implemented");
  }

}
