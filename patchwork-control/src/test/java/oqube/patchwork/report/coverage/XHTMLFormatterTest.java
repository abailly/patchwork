package oqube.patchwork.report.coverage;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

public class XHTMLFormatterTest extends TestCase {

  public void testMiddle() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 5);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-middle"));
  }

  private void formatCoverage(StringWriter sw, final double cov) {
    PrintWriter pw = new PrintWriter(sw);
    XHTMLFormatter frm = new XHTMLFormatter();
    frm.setHigh(80);
    frm.setLow(10);
    frm.setOut(pw);
    AllEdgesObjective obj = new AllEdgesObjective() {
      public double high() {
        return 10;
      }

      public double low() {
        return 0;
      }

      public double coverage() {
        return cov;
      }

      public String getMethodName() {
        return "toto";
      }
    };
    // work
    obj.visit(frm);
  }

  public void testLowOn() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 1);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-low"));
  }

  public void testLowIn() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 0);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-low"));
  }

  public void testLowOff() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 1.0000000001);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-middle"));
  }

  public void testHighOn() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 8);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-high"));
  }

  public void testHighIn() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 10);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-high"));
  }

  public void testHighOff() {
    StringWriter sw = new StringWriter();
    formatCoverage(sw, 7.99999999999);
    // check output
    assertTrue("Incorrect style for percent", sw.toString().contains(
        "edge-coverage-middle"));
  }

}
