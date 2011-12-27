package oqube.patchwork.report.coverage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import oqube.patchwork.report.source.SourceToURL;

import junit.framework.TestCase;

public class SourceToURLTest extends TestCase {

  private SourceToURL surl;

  protected void setUp() throws Exception {
    super.setUp();
    this.surl = new SourceToURL();
  }

  public void testMappingFromDirectory() {
    surl.addSourcepath(new File("src/test/java"));
    // check file in dir
    URL url = surl.get("SourceMapperTest.java");
    assertNotNull(url);
  }

  public void testMappingFromJar() {
    surl.addSourcepath(new File("src/test/resources/test.jar"));
    URL url = surl.get("SourceMapper.java");
    assertNotNull(url);
  }

  public void testMappingFromZip() {
    surl.addSourcepath(new File("src/test/resources/test.zip"));
    URL url = surl.get("SourceMapper.java");
    assertNotNull(url);
  }

  public void testMappingFromList() {
    List<File> cp = new ArrayList<File>();
    cp.add(new File("src/test/resources/test.jar"));
    cp.add(new File("src/test/java"));
    surl.addSourcepath(cp);
    // check file in dir
    URL url = surl.get("SourceMapperTest.java");
    assertNotNull(url);
    url = surl.get("SourceMapper.java");
    assertNotNull(url);
  }

  public void testMappingFromListInCtor() {
    List<File> cp = new ArrayList<File>();
    cp.add(new File("src/test/resources/test.jar"));
    cp.add(new File("src/test/java"));
    surl = new SourceToURL(cp);
    // check file in dir
    URL url = surl.get("SourceMapperTest.java");
    assertNotNull(url);
    url = surl.get("SourceMapper.java");
    assertNotNull(url);
  }
}
