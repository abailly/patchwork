package oqube.bytes.loading;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.lifl.utils.SimpleClassLoader;

/**
 * Cette classe definit un ClassLoader permettant de definir des classes a la
 * volee a partir de tableaux d'octets
 * 
 * @author Arnaud Bailly
 * @version 11082002
 */
public class ByteArrayClassLoader extends ClassLoader implements
    SimpleClassLoader {

  // private map for classes built through this ClassLoader
  private Map builtClasses = new HashMap();

  /* map for unbuilt classes */
  private Map unbuiltClasses = new HashMap();

  /*
   * root directory for output of class files
   */
  private String rootDir = ".";

  /*
   * write mode ?
   */
  private boolean write = false;

  ByteArrayClassLoader() {
  }

  public ByteArrayClassLoader(ClassLoader cl) {
    super(cl);
  }

  /**
   * Build all classes stored in this classloader.
   * 
   * @return array of Class Objects defined in this loader.
   */
  public Class[] buildAll() {
    List l = new ArrayList();
    for (Iterator i = unbuiltClasses.entrySet().iterator(); i.hasNext();) {
      Map.Entry me = (Map.Entry) i.next();
      try {
        l.add(findClass((String) me.getKey()));
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        System.err.println("Cannot find class " + me.getKey());
      }
    }
    return (Class[]) l.toArray(new Class[l.size()]);
  }

  public Class buildClass(String name, byte[] bytes) {
    /*
     * replace internal form to external one it used to work without this
     */
    String dname = name;
    dname = dname.replace('/', '.');
    Class cls = null;
    try {
      cls = defineClass(dname, bytes, 0, bytes.length);
    } catch (Throwable t) {
      System.err.println("Error while defining " + name);
      t.printStackTrace();
      return null;
    }
    // resolveClass(cls);
    /*
     * classe is stored with slashes instead of dots
     */
    builtClasses.put(name, cls);
    /* write class content */
    if (write)
      writeclass(name, bytes);
    return cls;
  }

  protected void writeclass(String name, byte[] bytes) {
    try {
      String fname = rootDir + "/" + name.replace('.', '/') + ".class";
      // create directory structure
      File dir = new File(fname.substring(0, fname.lastIndexOf('/')));
      if (!dir.exists())
        dir.mkdirs();
      // create file
      FileOutputStream fos = new FileOutputStream(fname);
      fos.write(bytes);
      fos.flush();
      fos.close();
      System.err.println("Done writing class " + name + " to " + fname);
    } catch (IOException ioex) {
      System.err.println("Error in writing class file " + name + " : "
          + ioex.getMessage());
    }
  }

  public byte[] getBytes(String name) {
    return (byte[]) unbuiltClasses.get(name);
  }

  public void putBytes(String name, byte[] bytes) {
    unbuiltClasses.put(name, bytes);
  }

  public Class findClass(String cname) throws ClassNotFoundException {
    Class cls = (Class) builtClasses.get(cname);
    if (cls == null) {
      /* try to find it in unbuilt classes */
      byte[] bytes = getBytes(cname);
      if (bytes == null)
        throw new ClassNotFoundException("ByteArrayClassLoader : class "
            + cname + " not defined here");
      cls = buildClass(cname, bytes);
    }
    resolveClass(cls);
    return cls;
  }

  public void resolve(Class cls) {
    resolveClass(cls);
  }

  /**
   * @return Returns the rootDir.
   */
  public String getRootDir() {
    return rootDir;
  }

  /**
   * @param rootDir
   *          The rootDir to set.
   */
  public void setRootDir(String rootDir) {
    this.rootDir = rootDir;
  }

  /**
   * @return Returns the write.
   */
  public boolean isWrite() {
    return write;
  }

  /**
   * @param write
   *          The write to set.
   */
  public void setWrite(boolean write) {
    this.write = write;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
   */
  @Override
  public InputStream getResourceAsStream(String name) {
    // if ressource is a locally defined class, return its bytes
    if (!name.endsWith(".class")) {
      return super.getResourceAsStream(name);
    }
    // normalize name
    String cln = name.substring(0, name.indexOf(".class")).replace('/', '.');
    while (cln.startsWith("."))
      cln = cln.substring(0);
    // check it isdefined here
    byte[] bytes = getBytes(cln);
    if (bytes == null)
      return super.getResourceAsStream(name);
    return new ByteArrayInputStream(bytes);
  }

  public byte[] loadClassData(String arg0) {
    return getBytes(arg0);
  }

}