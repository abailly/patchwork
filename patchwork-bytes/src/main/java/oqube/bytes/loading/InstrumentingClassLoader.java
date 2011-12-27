/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.bytes.loading;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import oqube.bytes.ClassFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class InstrumentingClassLoader<T extends ClassFactory> extends ClassLoader {

  protected Log                log       = LogFactory.getLog(InstrumentingClassLoader.class);
  protected T                  factory;
  protected Map<String, Class> generated = new HashMap<String, Class>();
  protected Pattern            include   = Pattern.compile(".*");
  protected Pattern            exclude   = Pattern.compile("java.*|sun.*");

  public InstrumentingClassLoader() {
    super();
  }

  public InstrumentingClassLoader(ClassLoader parent) {
    super(parent);
  }

  /**
   * @return Returns the exclude.
   */
  public Pattern getExclude() {
    return exclude;
  }

  /**
   * @param exclude
   *          The exclude to set.
   */
  public void setExclude(Pattern exclude) {
    this.exclude = exclude;
  }

  /**
   * @return Returns the include.
   */
  public Pattern getInclude() {
    return include;
  }

  /**
   * @param include
   *          The include to set.
   */
  public void setInclude(Pattern include) {
    this.include = include;
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, true);
  }

  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    String cln = name.replace('.', '/');
    // lookup in loaded classes
    Class<?> cls = generated.get(cln);
    if(cls != null)
      return cls;
    // instrument if include/exclude
    if(include.matcher(name).matches() && !exclude.matcher(name).matches()) {
      try {
        // lookup in factory classfiles
        ClassFile cf = factory.getGenerated().get(cln);
        // if not found, instrument it
        if(cf == null)
          cf = instrument(cln);
        // get bytes from class file
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        cf.write(new DataOutputStream(bos));
        byte[] bytes = bos.toByteArray();
        cls = defineClass(name, bytes, 0, bytes.length);
        generated.put(cln, cls);
        if(resolve)
          resolveClass(cls);
        if(log.isInfoEnabled())
          log.info("Done instrumenting " + cln);
        save(name, bytes);
        return cls;
      } catch(IOException e) {
        throw new ClassNotFoundException("Error while loading class to instrument " + name, e);
      }
    } else {
      return getParent().loadClass(name);
    }
  }

  private void save(String className, byte[] bytes) throws IOException {
    File f = new File(className.replace('/', '_'));
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(bytes);
    fos.flush();
    fos.close();
  }

  private ClassFile instrument(String cln) throws IOException {
    // load class from path as a stream
    InputStream is = getParent().getResourceAsStream(cln + ".class");
    ClassFile orig = new ClassFile();
    orig.read(new DataInputStream(is));
    // instrument it
    return factory.instrument(orig);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String cln = name.replace('.', '/');
    // lookup in loaded classes
    Class<?> cls = generated.get(cln);
    if(cls != null)
      return cls;
    else
      throw new ClassNotFoundException("Cannot find class " + name);
  }

  /**
   * @return Returns the factory.
   */
  public T getFactory() {
    return factory;
  }

  /**
   * @param factory The factory to set.
   */
  public void setFactory(T factory) {
    this.factory = factory;
  }

  /**
   * @return Returns the log.
   */
  public Log getLog() {
    return log;
  }

  /**
   * @param log The log to set.
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * @return Returns the generated.
   */
  public Map<String, Class> getGenerated() {
    return generated;
  }

}