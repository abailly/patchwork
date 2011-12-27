/**
 * 
 */
package oqube.patchwork.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An interface for building graphs from various data.
 * 
 * @author nono
 */
public interface ControlGraphBuilder {

  /**
   * Factory method that creates all control graphs for all methods in the
   * bytecode encoded input streoam.
   * 
   * @param is
   *          a stream encoding a classfile
   * @return a map from fully qualified method names to their control graphs
   * @throws IOException
   *           if anything gets wrong in reading the class file
   */
  Map<String, ControlGraph> createAllGraphs(InputStream is) throws IOException;

  /**
   * Factory method for creating graph for a single method in a class. This
   * method loads the class from the loader this builder is attached to, locates
   * the requested method and returns the control flow graph constructed if it
   * exists.
   * 
   * @param cls
   *          class name
   * @param method
   *          a method name
   * @param signature
   *          signature of method
   * @return a control graph for given method or null.
   * @throws IOException
   *           if cannot find information. This may comes from a problem in
   *           classpath or in names.
   */
  ControlGraph createGraphForMethod(String cls, String method, String signature)
      throws IOException;

  /**
   * Factory method for creating all control flow graphs of a given class. This
   * method extracts a stream from the current classpath and then calls
   * {@link #createAllGraphs(InputStream)}.
   * 
   * @param cls
   *          name of the class to get graphs for.
   * @return a Map<String,ControlGraph> from method names (with signatures) to
   *         their control flow graph.
   * @throws IOException
   */
  Map<String, ControlGraph> createGraphsForClass(String cls) throws IOException;

  /**
   * Factory method for creating graph for a single method in a class, given its
   * full name. 
   * This  method loads the class from the loader this builder is attached to, locates
   * the requested method and returns the control flow graph constructed if it
   * exists.
   * 
   * @param method
   *          a full method name: classname dot methodname signature
   * @return a control graph for given method or null.
   * @throws IOException
   *           if cannot find information. This may comes from a problem in
   *           classpath or in names.
   */
  ControlGraph createGraphForMethod(String method) throws IOException;

}
