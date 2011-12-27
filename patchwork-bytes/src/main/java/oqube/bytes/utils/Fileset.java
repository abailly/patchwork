/**
 * 
 */
package oqube.bytes.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts all files recursively from a directory. This class list all files
 * matching some criterion recursively from a root directory.
 * 
 * @author nono
 * 
 */
public class Fileset {

  private FileFilter selector;

  /**
   * Create a fileset without selector.
   * 
   * @param selector
   *          a FileFilter. may not be null.
   */
  public Fileset(FileFilter selector) {
    this.selector = selector;
  }

  /**
   * List all files recursively starting from given directory.
   * 
   * @param dir
   *          must be a valid, readable directory.
   * @return a possibly empty list of files contained in this directory and its
   *         subdirs.
   */
  public List<File> files(File dir) {
    List<File> ret = new ArrayList<File>();
    if (!dir.exists() || !dir.canRead())
      return ret;
    File[] files = dir.listFiles();
    int ln = files.length;
    for (int i = 0; i < ln; i++) {
      if (files[i].canRead()) {
        /* recursive exploration */
        if (files[i].isDirectory())
          ret.addAll(files(files[i]));
        /* check selector */
        if (selector.accept(files[i]))
          ret.add(files[i]);
      }
    }
    return ret;
  }
}
