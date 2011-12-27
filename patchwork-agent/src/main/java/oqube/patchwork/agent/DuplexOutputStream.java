/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.io.IOException;
import java.io.OutputStream;

public class DuplexOutputStream extends OutputStream {

  private final OutputStream stream1;
  private final OutputStream stream2;

  public DuplexOutputStream(OutputStream stream1, OutputStream stream2) {
    this.stream1 = stream1;
    this.stream2 = stream2;
  }

  @Override
  public void write(int b) throws IOException {
    System.err.print(b);
    IOException e1 = null;
    try {
      stream1.write(b);
    } catch(IOException e) {
      e1 = e;
    }
    stream2.write(b);
    if(e1 != null) {
      throw e1;
    }
  }

}
