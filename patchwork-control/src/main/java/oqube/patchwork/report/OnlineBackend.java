/**
 * 
 */
package oqube.patchwork.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import oqube.patchwork.instrument.CoverageBackend;

/**
 * An coverage backend that consumes report events immediately and dispatches
 * them to a reporter. This online backend is effectively a simple pipe that
 * transmits data from {@link Coverage} class to a {@link Reporter} instance.
 * 
 * @author nono
 * 
 */
public class OnlineBackend implements CoverageBackend {

  /*
   * the reporter where data is sent to.
   */
  private Reporter reporter;

  private PipedInputStream reporterStream;

  private PipedOutputStream coverageStream;

  /**
   * Creates a online backend with given reporter.
   * 
   * @param reporter2 a non null Reporter instance.
   */
  public OnlineBackend(Reporter reporter2) {
    this.reporter = reporter2;
  }

  public OnlineBackend() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.instrument.CoverageBackend#done()
   */
  public void done() {
    try {
      coverageStream.flush();
      // wait for reporter thread
      while ((reporterStream.available()) > 0) {
        System.err.println("yielding");
        Thread.yield();
      }
      coverageStream.close();
      reporterStream.close();
    } catch (IOException e) {
      // ???
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.instrument.CoverageBackend#getCoverageStream()
   */
  public OutputStream getCoverageStream() {
    return coverageStream;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.instrument.CoverageBackend#start()
   */
  public void start() {
    // setup streams
    reporterStream = new PipedInputStream();
    coverageStream = new PipedOutputStream();
    try {
      reporterStream.connect(coverageStream);
    } catch (IOException e1) {
      // dont know what to do
      e1.printStackTrace();
    }
    // calls asynchronously reporter to analyse stream
    new Thread() {
      public void run() {
        try {
          reporter.analyze(reporterStream);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  public void setReporter(Reporter rep) {
    this.reporter = rep;
  }

}
