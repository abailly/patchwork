/**
 *  Copyright (C) 2006 - OQube / Arnaud Bailly
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 Created 11 sept. 2006
 */
package oqube.patchwork.report.coverage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import oqube.patchwork.graph.ControlGraph;

/**
 * Root class for all objectives that are based on the crontrol flow graph of a
 * single method.
 * 
 * @author nono
 * 
 */
public abstract class MethodObjective implements CoverageObjective {

  private String className;

  private String methodName;

  private String signature;

  private ControlGraph graph;

  public String getName() {
    return getMethodName();
  }

  /**
   * Extracts display name to use for reporting a coverage objective. This
   * method expects classes to define a static public final field named
   * <code>display</code>.
   * 
   * @param cls
   *          the objective classes.
   * @return a string representing these classes display names concatenated with
   *         comma separating them.
   */
  public static String getDisplayName(List<Class> cls) {
    StringBuffer sb = new StringBuffer();
    for (Iterator<Class> it = cls.iterator(); it.hasNext();) {
      Class kls = it.next();
      try {
        Field f = kls.getField("display");
        sb.append(f.get(null));
        if (it.hasNext())
          sb.append(',');
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return sb.toString();
  }

  /**
   * @return Returns the method.
   */
  public String getMethod() {
    return className + '.' + methodName + signature;
  }

  public void setMethod(String method) throws IOException {
    /* extract control flow graph */
    int dot = method.lastIndexOf('.');
    if (dot == -1)
      throw new IllegalArgumentException("Invalid method name " + method
          + ": Must be <class>.<method><signature>");
    int paren = method.lastIndexOf('(');
    if (paren == -1)
      throw new IllegalArgumentException("Invalid signature in " + method
          + ": Must be <class>.<method><signature>");
    setClassName(method.substring(0, dot));
    setMethodName(method.substring(dot + 1, paren));
    setSignature(method.substring(paren));
  }

  /**
   * @param className
   *          The className to set.
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * @param graph
   *          The graph to set.
   */
  public void setGraph(ControlGraph graph) {
    this.graph = graph;
  }

  /**
   * @param methodName
   *          The methodName to set.
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * @param signature
   *          The signature to set.
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * @return Returns the className.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return Returns the graph.
   */
  public ControlGraph getGraph() {
    return graph;
  }

  /**
   * @return Returns the methodName.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return Returns the signature.
   */
  public String getSignature() {
    return signature;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#low()
   */
  public double low() {
    return high() / 2;
  }

}
