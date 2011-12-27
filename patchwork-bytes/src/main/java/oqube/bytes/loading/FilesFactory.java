/**
 * 
 */
package oqube.bytes.loading;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import oqube.bytes.ClassFile;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.utils.Fileset;

/**
 * A provider that can load classes from arbitrary files and directories. When
 * asked for a class name, this provider lookup the classname within all its
 * registered classes and returns corresponding stream. Classes are registered
 * when a file is added to this provider through the method
 * {@link #addFile(File)}: The file is loaded (or scanned for .class if it is a
 * directory), its content analyzed and the defined class registered in this
 * provider.
 * <p>
 * This factory is different from {@link FilesetDirectoriesFactory} in that it
 * does not enforce correct directory structure (ie. package directory
 * structure).
 * 
 * @author nono
 * 
 */
public class FilesFactory implements ClassFileFactory {

  private static final Fileset   fileset  = new Fileset(new FileFilter() {

                                            public boolean accept(File arg0) {
                                              return (arg0.isFile() && arg0.canRead() && arg0.getName().endsWith(".class") || arg0.isDirectory()
                                                  && arg0.canRead());
                                            }

                                          });

  private Map<String, ClassFile> classMap = new HashMap<String, ClassFile>();

  public void add(File f) throws IOException {
    if(f.isDirectory()) {
      List<File> files = fileset.files(f);
      for(File file : files)
        add(file);
    } else if(isJarFile(f)) {
      extractFromJar(f);
    } else {
      assert f.isFile();
      final FileInputStream in = new FileInputStream(f);
      extractClassFile(in);
    }

  }

  private boolean isJarFile(File f) {
    return f.getName().endsWith(".jar") || f.getName().endsWith(".zip");
  }

  private void extractFromJar(File f) throws IOException {
    JarFile jar = new JarFile(f);
    for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
      JarEntry entry = e.nextElement();
      if(entry.getName().endsWith(".class")) {
        extractClassFile(jar.getInputStream(entry));
      }
    }
  }

  private ClassFile extractClassFile(final InputStream in) throws IOException {
    ClassFile cf = new ClassFile();
    cf.read(new DataInputStream(in));
    ClassFileInfo cfi = cf.getClassFileInfo();
    classMap.put(cfi.getName(), cf);
    return cf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.loading.ClassFileProvider#getStreamFor(java.lang.String)
   */
  public ClassFile getClassFileFor(String className) {
    return classMap.get(className);
  }

  /**
   * Return a collection of all classfile objects defined int this factory.
   * 
   * @return a collection of ClassFile instanceS. Maybe empty but not null.
   */
  public Collection<ClassFile> getAllDefinedClassFiles() {
    return classMap.values();
  }

  @Override
  public String toString() {
    return FilesFactory.class.getName() + " [" + classMap.size() + " class(es)]";
  }

}
