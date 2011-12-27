/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.gui;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oqube.bytes.ClassFile;
import oqube.bytes.loading.FilesFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.algorithm.GraphFilter;

/**
 * Computes all dependencies between 2 sets of classes.
 * 
 * @author abailly
 */
public class BipartiteDependency extends AbstractDependencyGraph {

  private static Log           logger          = LogFactory.getLog(BipartiteDependency.class);

  private ClassDependencyGraph dependencyGraph = new ClassDependencyGraph(true);
  private Set<ClassInfo>       fromSet;
  private Set<ClassInfo>       toSet;

  public BipartiteDependency() {
    this.fromSet = new HashSet<ClassInfo>();
    this.toSet = new HashSet<ClassInfo>();
    dependencyGraph.addFilter(dependenciesFromTo(fromSet, toSet));
  }

  private static GraphFilter dependenciesFromTo(final Set<ClassInfo> fromSet, final Set<ClassInfo> toSet) {
    return new GraphFilter() {

      public boolean filter(Edge arg0) {
        return fromSet.contains(arg0.getVertexA()) && toSet.contains(arg0.getVertexB());
      }

      public boolean filter(Object arg0) {
        ClassInfo cls = ((ClassInfo)arg0);
        return fromSet.contains(cls) || toSet.contains(cls);
      }
    };
  }

  public static void main(String[] args) {
    if(args.length != 2) {
      usage();
      return;
    }
    try {
      DependencyComputationProgressListener listener = new DependencyComputationProgressListener() {
        double currentRatio = 0.;

        public void progress(int countAnalyzedDependencies, int totalNumberToAnalyze) {
          final double ratio = ((double)countAnalyzedDependencies) / ((double)totalNumberToAnalyze);
          if((ratio - currentRatio) > .1) {
            currentRatio = ratio;
            logger.info(String.format("%.0f%% ", currentRatio * 100));
          }
        }
      };

      new BipartiteDependency().computeAllDependencies(args[0], args[1], listener).dumpDependenciesTo(System.out);
    } catch(Exception e) {
      logger.error("error computing dependencies between " + args[0] + " and " + args[1], e);
    }
  }
  private static String EOL = System.getProperty("line.separator");

  private static void usage() {
    System.out.println("Usage: java -cp <classpath> oqube.patchwork.gui.BipartiteDependency <fileset1> <fileset2>" + EOL
        + "Computes all dependencies between two sets of files." + EOL + "" + EOL
        + "<fileset1>,<fileset2>: may be jar files, single .class files or directories containing class files");
  }

  /**
   * Update this graph with the dependencies between the two given {@link ClassFile} objects.
   * 
   * @param from
   * @param to
   * @throws Exception 
   */
  public void computeDependenciesFromTo(ClassFile from, ClassFile to) throws Exception {
    fromSet.add(ClassDependencyGraph.makeInfo(from, from.getClassFileInfo().getName()));
    toSet.add(ClassDependencyGraph.makeInfo(to, to.getClassFileInfo().getName()));
    dependencyGraph.add(from);
    dependencyGraph.add(to);
  }

  public DirectedGraph getGraph() {
    return dependencyGraph.getGraph();
  }

  public Map<String, Object> getInfomap() {
    return dependencyGraph.getInfomap();
  }

  public void dumpDependenciesTo(PrintStream stream) {
    stream.println();
    for(Edge edge : getGraph().getAllEdges()) {
      stream.println(formatSingleDependency(edge));
    }
  }

  /**
   * Compute dependencies between all class files partitioned in 2 filesets.
   * 
   * @param fromPath
   * @param toPath
   * @param progressListener if non null, an object which will be notified of computation progress. 
   * @return an updated graph containing all found dependencies between 2 sets. 
   * @throws Exception 
   */
  public BipartiteDependency computeAllDependencies(String fromPath, String toPath, DependencyComputationProgressListener progressListener)
      throws Exception {
    int countAnalyzedDependencies = 0;
    int totalNumberToAnalyze = 0;
    FilesFactory fromFiles = new FilesFactory();
    fromFiles.add(new File(fromPath));
    FilesFactory toFiles = new FilesFactory();
    toFiles.add(new File(toPath));
    totalNumberToAnalyze = toFiles.getAllDefinedClassFiles().size() * fromFiles.getAllDefinedClassFiles().size();
    for(ClassFile from : fromFiles.getAllDefinedClassFiles())
      for(ClassFile to : toFiles.getAllDefinedClassFiles()) {
        countAnalyzedDependencies++;
        computeDependenciesFromTo(from, to);
        if(progressListener != null) {
          progressListener.progress(countAnalyzedDependencies, totalNumberToAnalyze);
        }
      }
    return this;
  }

  private String formatSingleDependency(Edge edge) {
    final ClassInfo from = (ClassInfo)edge.getVertexA();
    final ClassInfo to = (ClassInfo)edge.getVertexB();
    return String.format("%s,%s,%s,%s,%s", ClassDependencyGraph.packageName(from), ClassDependencyGraph.className(from), to.getName(), edge
        .getData()
        .toString(),
        to.getClassFile() != null ? to.getClassFile().typeOfClass() : "class");
  }

}
