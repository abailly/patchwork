/**
 * 
 */
package oqube.patchwork.report.source;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import oqube.bytes.utils.Fileset;

/**
 * A utility class for mapping raw source file names to concrete urls. This file
 * is used to associate a source file name, as stored in a class file, to a real
 * URL, given some sourcepath. A sourcepath is a list of File objects denoting
 * either directories, jar or zip files.
 * 
 * @author nono
 * 
 */
public class SourceToURL {

  /*
   * map from base source name (without dirs) and URL
   */
  private Map<String, URL> sourceMap = new HashMap<String, URL>();

  /**
   * Create an instance with given sourcepath.
   * 
   * @param cp a non null list of files.
   */
  public SourceToURL(List<File> cp) {
    addSourcepath(cp);
  }

  /**
   * Default constructor.
   * Creates this map with empty sourcepath.
   */
  public SourceToURL() {
  }

  /**
   * Add a sourcepath entry to this sourceURL mapping.
   * 
   * 
   * @param file
   *          The entry may be either a directory or a jar/zip file. Other type
   *          of files are simply ignored.
   */
  public void addSourcepath(File file) {
    if (file.isDirectory())
      addDirectory(file);
    else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))
      addJarOrZip(file);
  }

  private void addJarOrZip(File file) {
    try {
      ZipFile jar = new ZipFile(file);
      for (Enumeration<? extends ZipEntry> enums = jar.entries(); enums
          .hasMoreElements();) {
        ZipEntry entry = enums.nextElement();
        String s = entry.getName();
        String e = null;
        // assume '/' separated entries
        int sl = s.lastIndexOf('/');
        if (sl == -1)
          e = s;
        else
          e = s.substring(sl + 1);
        sourceMap.put(e, new URL("jar:" + file.toURL() + "!/" + s));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void addDirectory(File file) {
    for (File f : new Fileset(new FileFilter() {

      public boolean accept(File pathname) {
        return pathname.isFile() && pathname.canRead();
      }

    }).files(file)) {
      String base = f.getName();
      try {
        sourceMap.put(base, f.toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
  }

  public URL get(String string) {
    return sourceMap.get(string);
  }

  /**
   * Add a list of sourcepath item to this source mapping.
   * 
   * @param cp a non null list of File items.
   */
  public void addSourcepath(List<File> cp) {
    for(File f: cp) 
      addSourcepath(f);
  }

}
