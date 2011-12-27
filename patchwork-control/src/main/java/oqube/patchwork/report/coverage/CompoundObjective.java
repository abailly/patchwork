/**
 * 
 */
package oqube.patchwork.report.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oqube.patchwork.graph.ControlGraph;

/**
 * A class that can group atomic objectives. A compound objective differs from
 * an aggregate objective in that it is specialized to aggregate single metheds
 * objective measures.
 * 
 * @author nono
 * 
 */
public class CompoundObjective extends MethodObjective {

  private List<MethodObjective> objectives = new ArrayList<MethodObjective>();

  private int hit;

  /**
   * @return the objectives
   */
  public List<MethodObjective> getObjectives() {
    return objectives;
  }

  /**
   * @param objectives
   *          the objectives to set
   */
  public void setObjectives(List<MethodObjective> objectives) {
    this.objectives = objectives;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#coverage()
   */
  public double coverage() {
    double sum = 0;
    for (MethodObjective obj : objectives)
      sum += obj.coverage();
    return sum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#high()
   */
  public double high() {
    double sum = 0;
    for (MethodObjective obj : objectives)
      sum += obj.high();
    return sum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#reset()
   */
  public void reset() {
    for (MethodObjective obj : objectives)
      obj.reset();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#visit(oqube.patchwork.report.coverage.ObjectiveVisitor)
   */
  public void visit(ObjectiveVisitor vis) {
    vis.visit(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageListener#update(java.util.Map)
   */
  public void update(Map codepaths) {
    for (MethodObjective obj : objectives)
      obj.update(codepaths);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageListener#update(int, java.lang.String,
   *      int)
   */
  public void update(int tid, String method, int block) {
    for (MethodObjective obj : objectives)
      obj.update(tid, method, block);
  }

  public void add(MethodObjective objective) {
    this.objectives.add(objective);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setClassName(java.lang.String)
   */
  @Override
  public void setClassName(String className) {
    super.setClassName(className);
    for (MethodObjective obj : objectives)
      obj.setClassName(className);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setGraph(oqube.patchwork.graph.ControlGraph)
   */
  @Override
  public void setGraph(ControlGraph graph) {
    super.setGraph(graph);
    for (MethodObjective obj : objectives)
      obj.setGraph(graph);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setMethod(java.lang.String)
   */
  @Override
  public void setMethod(String method) throws IOException {
    super.setMethod(method);
    for (MethodObjective obj : objectives)
      obj.setMethod(method);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setMethodName(java.lang.String)
   */
  @Override
  public void setMethodName(String methodName) {
    super.setMethodName(methodName);
    for (MethodObjective obj : objectives)
      obj.setMethodName(methodName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.MethodObjective#setSignature(java.lang.String)
   */
  @Override
  public void setSignature(String signature) {
    super.setSignature(signature);
    for (MethodObjective obj : objectives)
      obj.setSignature(signature);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageListener#hit()
   */
  public int hit() {
    return hit;
  }
}
