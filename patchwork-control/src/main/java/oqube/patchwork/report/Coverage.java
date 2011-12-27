/*______________________________________________________________________________
 * 
 * Copyright 2006 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Jan 2, 2006
 *
 */
package oqube.patchwork.report;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import oqube.patchwork.instrument.CoverageBackend;

/**
 * Coverage engine. This class is at the heart of the coverage recording
 * process: It is called statically by covered code to record events, then
 * encode them as documented in this pacakge's documeantion and sends the event
 * to an output stream produced by a
 * {@link oqube.patchwork.instrument.CoverageBackend} instance for later
 * processing.
 * 
 * @author nono
 * @version $Id: Coverage.java 20 2007-05-29 19:50:34Z arnaud.oqube $
 */
public final class Coverage {

  /*
   * output stream for data
   */
  private static DataOutputStream      out;

  /*
   * in memory buffer
   */
  private static ByteArrayOutputStream data;

  /*
   * the reporter to use.
   */
  private static CoverageBackend       backend;

  /**
   * Default buffer size before flushing to disk. Default value is 1MB.
   */
  public static final int              DEFAULT_BUFFER_SIZE = 1 << 20;

  /*
   * maximum size of buffer
   */
  public static int                    bufferSize          = DEFAULT_BUFFER_SIZE;

  /*
   * table of method names
   */
  private static String[]              classes;

  private static String[][]            methods;

  private static boolean               initialized;

  private static OutputStream          outputStream;

  /*
   * count of bytes written
   */
  private static int                   count               = 0;

  private static CoverageInfo          coverageInfo;

  public static class CoverageIOException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -85009079777891835L;

    /**
     * 
     */
    public CoverageIOException() {
      super();
    }

    /**
     * @param message
     * @param cause
     */
    public CoverageIOException(String message, Throwable cause) {
      super(message, cause);
    }

    /**
     * @param message
     */
    public CoverageIOException(String message) {
      super(message);
    }

    /**
     * @param cause
     */
    public CoverageIOException(Throwable cause) {
      super(cause);
    }

  }

  /**
   * Main method for outputting coverage information. If this Coverage is not
   * instantiated, then we first output header to <code>out</code> stream,
   * which contains number and names for all classes and methods instrumented.
   * 
   * @param cix
   * @param mix
   * @param bix
   */
  public synchronized static void cover(short cix, short mix, short bix) {
    if(!initialized)
      try {
        initialize();
      } catch(IOException e1) {
        e1.printStackTrace();
        return;
      }
    /* get calling thread id (and assume it is a short !!) */
    short tid = (short)Thread.currentThread().getId();
    /* pack everything in a long and write it */
    long val = (((long)tid) << 48) | (((long)cix) << 32) | (((long)mix) << 16) | (((long)bix) & 0x000000000000ffff);
    try {
      if(data.size() + 4 > bufferSize)
        flushBuffer();
      out.writeLong(val);
    } catch(IOException e) {
      throw new CoverageIOException(e);
    }
  }

  /*
   * Writes the content of th in-memory buffer to final output stream.
   */
  private static void flushBuffer() throws IOException {
    count += data.size();
    data.writeTo(outputStream);
    outputStream.flush();
    data.reset();
  }

  /**
   * Put an end marker into the coverage data denoting a new series of tests are
   * being executed from this points on. This method effectively output a -1L
   * byte array into the output stream.
   * 
   */
  public static synchronized void mark() {
    try {
      out.writeLong(-1L);
      flushBuffer();
    } catch(IOException e) {
      throw new CoverageIOException(e);
    }
  }

  /*
   * write header containing class names and method names.
   */
  private static void initialize() throws IOException {
    if(coverageInfo == null) {
      try {
        bootstrap();
      } catch(Exception e) {
        throw new IOException("Error trying to bootstrap Coverage class:" + e.getLocalizedMessage());
      }
    }
    if(backend != null)
      backend.start();
    data = new ByteArrayOutputStream();
    out = new DataOutputStream(data);
    if(backend != null)
      outputStream = backend.getCoverageStream();
    initialized = true;
  }

  private static void outputClassesAndMethods() throws IOException {
    classes = getClasses();
    methods = getMethods();
    int ln = classes.length;
    out.writeInt(ln);
    for(int i = 0; i < ln; i++) {
      out.writeUTF(classes[i]);
      int lnm = methods[i].length;
      out.writeInt(lnm);
      for(int j = 0; j < lnm; j++) {
        out.writeUTF(methods[i][j]);
      }
    }
  }

  /*
   * called if this class is not initialized: We try to create an instance of
   * oqube/patchwork/report/CoverageInfo
   */
  private static void bootstrap() throws ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException {
    Class ci = Coverage.class.getClassLoader().loadClass("oqube.patchwork.report.CoverageInfoImpl");
    CoverageInfo info = (CoverageInfo)ci.newInstance();
    setCoverageInfo(info);
    outputStream = new FileOutputStream("patchwork" + new Date().getTime());
  }

  public static void setCoverageInfo(CoverageInfo coverageInfo) {
    Coverage.coverageInfo = coverageInfo;
  }

  /**
   * Ask this coverage instance to flush and close the output stream used for
   * recording coverage information. This method <strong>must</strong> be
   * called at end of coverage collating session to ensure proper registering of
   * all coverage events.
   * 
   */
  public static void closeOutput() {
    try {
      if(!initialized)
        return;
      mark();
      outputClassesAndMethods();
      out.flush();
      out.close();
      flushBuffer();
      if(backend != null)
        backend.done();
      initialized = false;
    } catch(IOException e) {
      throw new CoverageIOException(e);
    }
  }

  /**
   * @return Returns the bufferSize.
   */
  public static int getBufferSize() {
    return bufferSize;
  }

  /**
   * @param bufferSize
   *          The bufferSize to set.
   */
  public static void setBufferSize(int bufferSize) {
    Coverage.bufferSize = bufferSize;
  }

  /**
   * @return Returns the classes.
   */
  public static String[] getClasses() {
    return coverageInfo.getClasses();
  }

  /**
   * @return Returns the methods.
   */
  public static String[][] getMethods() {
    return coverageInfo.getMethods();
  }

  /**
   * @return Returns the initialized.
   */
  public static boolean isInitialized() {
    return initialized;
  }

  /**
   * @return Returns the reporter.
   */
  public static CoverageBackend getBackend() {
    return backend;
  }

  /**
   * @param reporter
   *          The reporter to set.
   */
  public static void setBackend(CoverageBackend reporter) {
    Coverage.backend = reporter;
  }
}
