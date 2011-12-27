package oqube.patchwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class MainTest extends TestCase {

  private File dir;

  private File jar;

  protected void setUp() throws Exception {
    super.setUp();
    /* cree un repertoire avec un jar */
    /* cree un jar contenant une classe main */
    jar = File.createTempFile("test",".jar");
    jar.deleteOnExit();
    JarOutputStream jout = new JarOutputStream(new FileOutputStream(jar));
    ZipEntry zip = new ZipEntry("oqube/patchwork/Dummy.class");
    InputStream is = getClass().getResourceAsStream("/main.bin");
    jout.putNextEntry(zip);
    byte[] buf = new byte[1024];
    int rd = 0;
    while ((rd = is.read(buf, 0, 1024)) != -1) {
      jout.write(buf, 0, rd);
    }
    jout.closeEntry();
    jout.finish();
    jout.flush();
    jout.close();
    dir = jar.getParentFile();
  }

  /*
   * teste l'absence de la variable d'environnement launcher.libdir
   */
  public void test00NoEnv() {
    /* construit argument */
    String[] args = new String[0];
    Launch l = new Launch();
    l.setArguments(args);
    l.run();
    assertEquals(1, l.getStatus());
  }

  /*
   * test le fonctionnement nominal.
   */
  public void testLaunchDummyMain() {
    /* should be in setUp but is global */
    System.setProperty("launcher.libdir", dir.getAbsolutePath());
    System.setProperty("launcher.main", "oqube.patchwork.Dummy");
    /* construit argument */
    String[] args = new String[0];
    Launch l = new Launch();
    l.setArguments(args);
    l.run();
    assertEquals(0, l.getStatus());
  }

  /*
   * test si le repertoire n'existe pas.
   */
  public void test02DirNotExists() {
    System.setProperty("launcher.libdir", "");
    /* construit argument */
    String[] args = new String[0];
    Launch l = new Launch();
    l.setArguments(args);
    l.run();
    assertEquals(2, l.getStatus());
  }

  /*
   * test si le repertoire n'en est pas un
   */
  public void test03NotADir() {
    System.setProperty("launcher.libdir", jar.getAbsolutePath());
    /* construit argument */
    String[] args = new String[0];
    Launch l = new Launch();
    l.setArguments(args);
    l.run();
    assertEquals(2, l.getStatus());
  }

  /*
   * test le positionnement de la classe main
   */
  public void test04MainClassSet() throws SecurityException,
      IllegalArgumentException, ClassNotFoundException, NoSuchFieldException,
      IllegalAccessException {
    System.setProperty("launcher.libdir", dir.getAbsolutePath());
    System.setProperty("launcher.main", "oqube.patchwork.Dummy");
    /* construit argument */
    String[] args = new String[] { "1.2.3.4", "toto" };
    Launch l = new Launch();
    l.setArguments(args);
    l.run();
    assertEquals("Return code should be 0", 0, l.getStatus());
    checkDummy(l, args);
  }

  private void checkDummy(Launch l, String[] args) throws ClassNotFoundException,
      SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    Class cls = l.getLoader().loadClass("oqube.patchwork.Dummy");
    Field f = (Field) cls.getDeclaredField("args");
    String[] res = (String[]) f.get(null);
    /* check equality of args */
    for (int i = 1; i < args.length; i++) {
      if (!args[i].equals(res[i]))
        throw new AssertionFailedError("Expected " + args[i] + " but found "
            + res[i] + " in passed arguments from Main");
    }

  }

  protected void tearDown() throws Exception {
    super.tearDown();
    jar.delete();
  }

}
