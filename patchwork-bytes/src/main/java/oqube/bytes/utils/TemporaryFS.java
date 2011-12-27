/**
 * 
 */
package oqube.bytes.utils;

import java.io.File;

/**
 * A class for temporary directory management. This class allows user to create
 * a directory, write files and directories in it, then delete everything.
 * 
 * @author nono
 * 
 */
public class TemporaryFS {

  private File root;

  /**
   * Create the temporary FS with given name.
   * 
   * @param string
   *          the root directory. Must be a relative or absolute path.
   * 
   */
  public TemporaryFS(String string) {
    this(new File(string));
  }

  /**
   * Create the temporary FS in given dir. If root does not exist, it is
   * created. If it exists but is not file, an error is thrown.
   * 
   * @param root
   *          a File object, which may or may not exists.
   */
  public TemporaryFS(File root) {
    this.root = root;
    if (!root.exists())
      root.mkdirs();
    if (root.isFile())
      throw new IllegalArgumentException(root
          + " exists and is not a directory");
  }

  /**
   * Remove this temporary directory and everythig in it.
   * 
   * 
   */
  public void clean() {
     delete(root);
  }

  private void delete(File temp2) {
    if (!temp2.isDirectory())
      temp2.delete();
    else {
      for (File f : temp2.listFiles())
        delete(f);
      temp2.delete();
    }
  }

  /**
   * Get the root directory of this temporary.
   * 
   * @return a directory.
   */
  public File root() {
    return root;
  }

}
