package oqube.patchwork.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import oqube.patchwork.report.coverage.CoverageObjective;
import oqube.patchwork.report.coverage.OutputFormatter;
import oqube.patchwork.report.source.SourceMapper;

import fr.lifl.utils.Pipe;

import junit.framework.TestCase;

public class OnlineBackendTest extends TestCase {

  class StubReporter implements Reporter {

    private ByteArrayOutputStream bos;

    public void analyze(InputStream is) throws IOException {
      bos = new ByteArrayOutputStream();
      new Pipe(bos, is).pump();
    }

    public void report(OutputStream os) {
    }

    public void setFormater(OutputFormatter formatter) {
    }

    public void setObjective(CoverageObjective objective) {
    }

    public void setSourceMapper(SourceMapper sm) {
    }

  }

  private OnlineBackend back;

  private StubReporter rep;

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.back = new OnlineBackend();
    this.rep = new StubReporter();
    back.setReporter(rep);
  }

  public void testStartAndDone() throws IOException {
    // start then output some data, then close
    this.back.start();
    PrintStream os = new PrintStream(back.getCoverageStream());
    os.print("toto");
    this.back.done();
    // check transmission
    assertEquals("toto", new String(this.rep.bos.toByteArray()));

  }

  public void testNoStart() {
    try {
      PrintStream os = new PrintStream(back.getCoverageStream());
      os.print("toto");
      fail("Expected exception: start not called");
    } catch (Exception e) {
    }

  }
}
