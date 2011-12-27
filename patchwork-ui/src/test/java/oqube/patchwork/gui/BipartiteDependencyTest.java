/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import oqube.bytes.ClassFile;
import salvo.jesus.graph.Edge;

public class BipartiteDependencyTest extends TestCase {

  private ClassFile           from;
  private ClassFile           to;
  private BipartiteDependency deps;

  public BipartiteDependencyTest(String name) throws IOException {
    super(name);
    from = ClassFile.reify(ClassDependencyGraphTest.class);
    to = ClassFile.reify(ClassDependencyGraph.class);
    deps = new BipartiteDependency();
  }

  public void testComputeBipartiteDependenciesBetweenTwoClasses() throws Exception {

    deps.computeDependenciesFromTo(from, to);

    Collection<Edge> edges = deps.getGraph().getAllEdges();
    DependencyGraphTestHelpers.assertNoEdgeFromTo(edges, "oqube/patchwork/gui/ClassDependencyGraphTest", "junit/framework/TestCase");
    DependencyGraphTestHelpers.assertNoEdgeFromTo(edges, "oqube/patchwork/gui/ClassDependencyGraph", "oqube/patchwork/gui/ClassDependencyGraph$1");
    DependencyGraphTestHelpers.assertEdgeFromTo(edges, "oqube/patchwork/gui/ClassDependencyGraphTest", "oqube/patchwork/gui/ClassDependencyGraph");
  }

  public void testDumpBipartiteDependenciesInTextualFormat() throws Exception {
    deps.computeDependenciesFromTo(from, to);
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(bytes);
    deps.dumpDependenciesTo(stream);
    stream.flush();
    String output = bytes.toString();
    assertTrue("expected formatted dependency output, got " + output, output
        .contains("oqube/patchwork/gui,ClassDependencyGraphTest,oqube/patchwork/gui/ClassDependencyGraph"));
  }

  public void testHasMainMethodForCreatingGraphFrom2filesets() throws Exception {
    final List<Double> progressCount = new ArrayList<Double>();
    DependencyComputationProgressListener listener = new DependencyComputationProgressListener() {
      public void progress(int countAnalyzedDependencies, int totalNumberToAnalyze) {
        progressCount.add(((double)countAnalyzedDependencies) / ((double)totalNumberToAnalyze));
      }
    };
    new BipartiteDependency().computeAllDependencies("target/test-classes", "target/classes", listener);
    assertTrue("expected some progress to be reported but got nothing", progressCount.size() > 0);
  }
}
