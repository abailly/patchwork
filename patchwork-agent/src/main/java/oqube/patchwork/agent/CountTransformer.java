/**
 * Copyright 2010 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.patchwork.instrument.CoverageBackend;
import oqube.patchwork.instrument.FileBackend;
import oqube.patchwork.instrument.InstrumentClass;
import oqube.patchwork.report.Coverage;
import oqube.patchwork.report.CoverageInfo;
import oqube.patchwork.report.OnlineBackend;
import oqube.patchwork.report.SimpleReporter;

public class CountTransformer implements ClassFileTransformer, CoverageInfo {

  private final String    filter;
  private InstrumentClass instrumentClass;
  private CoverageBackend backend;
  private SimpleReporter  reporter;

  public CountTransformer(String filter) throws FileNotFoundException {
    this.filter = filter.replace('.', '/'); // use semi-internal format
    this.instrumentClass = new InstrumentClass();
    this.reporter = new SimpleReporter();
    this.backend = new CompositeBackend(new FileBackend(new File("."), "coverage.data"), new OnlineBackend(reporter));
    Coverage.setBackend(this.backend);
    Coverage.setCoverageInfo(this);
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer) throws IllegalClassFormatException {
    try {
      if(className.startsWith(filter)) {
        ClassFile cf = new ClassFile();
        ByteArrayInputStream bis = new ByteArrayInputStream(classfileBuffer);
        cf.read(new DataInputStream(bis));
        System.err.println("Done instrumenting " + className);
        byte[] bytes = instrumentClass.instrument(cf).getBytes();
        save(className, bytes);
        return bytes;
      } else
        return classfileBuffer;
    } catch(IOException e) {
      throw new IllegalClassFormatException("Cannot analyze class file format for " + className);
    }
  }

  private void save(String className, byte[] bytes) throws IOException {
    File f = new File(className.replace('/', '_'));
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(bytes);
    fos.flush();
    fos.close();
  }

  public String[] getClasses() {
    List<ClassFile> classes = instrumentClass.getClasses();
    int ln = classes.size();
    String[] ret = new String[ln];
    for(int i = 0; i < ln; i++)
      ret[i] = classes.get(i).getClassFileInfo().getName();
    return ret;
  }

  public String[][] getMethods() {
    List<List<String>> classes = instrumentClass.getMethods();
    int lnc = classes.size();
    String[][] cls = new String[lnc][];
    for(int i = 0; i < lnc; i++) {
      List<String> methods = classes.get(i);
      int lnm = methods.size();
      String[] meths = new String[lnm];
      for(int j = 0; j < lnm; j++)
        meths[j] = methods.get(j);
      cls[i] = meths;
    }
    return cls;
  }

  public Thread getFinalizer() {
    return new Thread() {
      public void run() {
        Coverage.closeOutput();
      }
    };
  }
}
