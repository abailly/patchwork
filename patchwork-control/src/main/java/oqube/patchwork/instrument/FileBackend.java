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
 * Created on Jan 3, 2006
 *
 */
package oqube.patchwork.instrument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * A base class for storing the content of a coverage report into a file.
 * <p>
 * This class stores a coverage report information into a file.
 * 
 * @author nono
 * @version $Id: FileBackend.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class FileBackend implements CoverageBackend {

  /*
   * the file where coverage data is stored.
   */
  private File             file;

  private FileOutputStream stream;

  private File             basedir;

  /**
   * Create a backend storing data files in the given directory.
   * 
   * @param rootdir
   *          the root directory to store data files into. Must be a writable
   *          directory.
   */
  public FileBackend(File rootdir) {
    this.basedir = rootdir;
  }

  public FileBackend() {
    this(new File("."));
  }

  /**
   * @param dir The directory where output file will be stored.
   * @param fileName the name of the file to store coverage info in.
   */
  public FileBackend(File dir, String fileName) {
    this.basedir = dir;
    this.file = new File(dir, fileName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageReporter#getCoverageStream()
   */
  public OutputStream getCoverageStream() {
    if(file == null)
      makeUniqueFile();
    System.err.println("Outputting data to " + file);
    try {
      return stream = new FileOutputStream(file);
    } catch(FileNotFoundException e) {
      throw new Error("Cannot open output file");
    }
  }

  private void makeUniqueFile() {
    long timestamp = new Date().getTime();
    this.file = new File(this.basedir, "patchwork" + timestamp);
  }

  public void start() {
  }

  public void done() {
    try {
      if(stream != null) {
        stream.flush();
        stream.close();
      }
    } catch(IOException e) {
      // IGNORED ?
    }
  }

}
