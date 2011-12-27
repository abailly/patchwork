package oqube.bytes.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class FilesetTest extends TestCase {

  public void testFileSelector() {
    Fileset fs = new Fileset(new FileFilter() {

      public boolean accept(File pathname) {
        return pathname.getName().contains("Fileset");
      }

    });
    List<File> files = fs.files(new File("src/test/java"));
    assertTrue("Files should contain filesettest.java", files

        .contains(new File(
            "src/test/java/oqube/bytes/utils/FilesetTest.java")));
  }

  public void testSelectorWithRegexp() {
    Fileset fs = new Fileset(new FileFilter() {
      public boolean accept(File pathname) {
        return Pattern.compile(".*Test.java").matcher(pathname.getName())
            .matches()
            && !Pattern.compile("^$").matcher(pathname.getName()).matches();
      }
    });
    List<File> files = fs.files(new File("src/test/java"));
    assertTrue("Files should contain filesettest.java", files
        .contains(new File(
            "src/test/java/oqube/bytes/utils/FilesetTest.java")));
  }
}
