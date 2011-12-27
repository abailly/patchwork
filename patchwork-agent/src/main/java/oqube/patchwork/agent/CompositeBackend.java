/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.agent;

import java.io.OutputStream;

import oqube.patchwork.instrument.CoverageBackend;
import oqube.patchwork.instrument.FileBackend;
import oqube.patchwork.report.Coverage;
import oqube.patchwork.report.OnlineBackend;

/**
 * Multiplexes data from {@link Coverage} into two other backend instances.
 *
 */
public class CompositeBackend implements CoverageBackend {

  private final CoverageBackend left;
  private final CoverageBackend right;

  public CompositeBackend(CoverageBackend left, CoverageBackend right) {
    this.left = left;
    this.right = right;
  }

  public void done() {
    this.left.done();
    this.right.done();
  }

  public OutputStream getCoverageStream() {
    return new DuplexOutputStream(left.getCoverageStream(), right.getCoverageStream());
  }

  public void start() {
    this.left.start();
    this.right.start();
  }

}
