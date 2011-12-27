package oqube.patchwork.it;

import java.io.File;

import oqube.patchwork.TestRunner;
import junit.framework.TestCase;

/**
 * Test the test runnner configuration.
 * 
 * @author nono
 */
public class TestRunnerTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testTriangle() {
    // command-line
    String args = "-c src/test/resources/test01/classes -t src/test/resources/test01/test-classes -x -s src/test/resources/test01/sources -O target/patchwork-reports";
    // run
    TestRunner.main(args.split(" +"));
    // check
    File out = new File("target/patchwork-reports/Triangle1.java.html");
    assertTrue(out.exists() && out.length() > 0);
  }
}
