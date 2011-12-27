package oqube.patchwork;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Launch Patchwork application in a platform-independent way.
 * <p>
 * This class extracts from the system variable <code>launcher.libdir</code> A
 * list of archive files to load and then calls the main method of the class
 * whose name is stored in variable <code>launcher.main</code>.
 * </p>
 * <p>
 * A hook is provided as method {@link #handleJar(File)} for subclasses to do
 * something interesting with the loaded classes/jar files.
 * </p>
 * 
 * @author abailly
 */
public class Launch implements Runnable {

  /*
   * the command line arguments array
   */
  private String[]       arguments;

  /*
   * the exit status
   * 
   */
  private int            status = 0;

  /*
   * class loader constructed by this lanceur
   */
  private URLClassLoader loader;

  /*
   * i18 bundle reference for error strings
   */
  PropertyResourceBundle msgs   = (PropertyResourceBundle)ResourceBundle.getBundle("oqube.patchwork.messages");

  public static void main(String[] argv) {
    /* create and run launcher */
    Launch l = new Launch();
    l.setArguments(argv);
    l.run();
  }

  public void run() {
    /* extraction du repertoire */
    String libdir = System.getProperty("launcher.libdir");
    /* for i18 */
    MessageFormat frm = new MessageFormat("");
    if(libdir == null) {
      frm.applyPattern(msgs.getString("main.missinglib"));
      System.err.println(frm.format(null));
      setStatus(1);
      return;
    }
    /* verifications */
    File f = new File(libdir);
    if(!f.exists()) {
      frm.applyPattern(msgs.getString("main.baddir"));
      System.err.println(frm.format(new Object[] { libdir }));
      setStatus(2);
      return;
    }
    if(!f.isDirectory()) {
      frm.applyPattern(msgs.getString("main.notdir"));
      System.err.println(frm.format(new Object[] { libdir }));
      setStatus(2);
      return;
    }
    if(!f.canRead()) {
      frm.applyPattern(msgs.getString("main.cannotread"));
      System.err.println(frm.format(new Object[] { libdir }));
      setStatus(2);
      return;
    }
    /* extraction du nom de la classe principale */
    String clname = System.getProperty("launcher.main");
    if(clname == null) {
      frm.applyPattern(msgs.getString("main.nomain"));
      System.err.println(frm.format(null));
      setStatus(2);
      return;
    }
    /* construction du classpath */
    File[] jars = f.listFiles(new FileFilter() {

      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".zip");
      }

    });
    int ln = jars.length;
    URL[] urls = new URL[ln + 1];
    /* ajout du repertoire courant par defaut */
    try {
      urls[0] = new URL("file://.");
    } catch(MalformedURLException e) {
      frm.applyPattern(msgs.getString("main.urlerror"));
      System.err.println(frm.format(new Object[] { "." }) + e.getLocalizedMessage());
    }
    for(int i = 0; i < ln; i++)
      try {
        handleJar(jars[i]);
        urls[i + 1] = new URL("file:///" + jars[i].getPath());
      } catch(MalformedURLException e) {
        frm.applyPattern(msgs.getString("main.urlerror"));
        System.err.println(frm.format(new Object[] { jars[i] }) + e.getLocalizedMessage());
      }
    /* chargement */
    loader = new URLClassLoader(urls, null);
    try {
      assert clname != null;
      Class cls = loader.loadClass(clname);
      Method main = cls.getDeclaredMethod("main", new Class[] { arguments.getClass() });
      main.invoke(null, new Object[] { arguments });
    } catch(ClassNotFoundException e) {
      displayErrorMessage(frm, clname, e, "main.classnotfound", 3);
    } catch(SecurityException e) {
      displayErrorMessage(frm, clname, e, "main.security", 3);
    } catch(NoSuchMethodException e) {
      displayErrorMessage(frm, clname, e, "main.nosuchmethod", 3);
    } catch(IllegalArgumentException e) {
      displayErrorMessage(frm, clname, e, "main.badarg", 3);
    } catch(IllegalAccessException e) {
      displayErrorMessage(frm, clname, e, "main.access", 3);
    } catch(InvocationTargetException e) {
      setStatus(4);
      e.printStackTrace();
    }
  }

  private void displayErrorMessage(MessageFormat frm, String clname, Throwable e, String messageKey, int statusCode) {
    frm.applyPattern(msgs.getString(messageKey));
    System.err.println(frm.format(new Object[] { clname }) + e.getLocalizedMessage());
    setStatus(statusCode);
  }

  /**
   * This method may be used to do something with the content of each loaded jar
   * file before they are put into the classpath.
   * 
   * @param file
   *          the File object representing a jar or zip file.
   */
  protected void handleJar(File file) {
  // NOP
  }

  public String[] getArguments() {
    return arguments;
  }

  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

  /**
   * @return Returns the status.
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param status
   *          The status to set.
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * @return Returns the loader.
   */
  public URLClassLoader getLoader() {
    return loader;
  }

  /**
   * @param loader
   *          The loader to set.
   */
  public void setLoader(URLClassLoader loader) {
    this.loader = loader;
  }
}
