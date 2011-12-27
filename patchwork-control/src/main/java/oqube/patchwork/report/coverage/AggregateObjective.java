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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class that aggregates values of several other objectives. This class sums
 * all the objective values of its aggregated objectives.
 * 
 * @author nono
 * 
 */
public class AggregateObjective implements CoverageObjective {

  private String name;

  private List<CoverageObjective> objectives = new ArrayList<CoverageObjective>();

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#name()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#coverage(java.util.Map)
   */
  public double coverage() {
    double sum = 0;
    for (CoverageObjective obj : objectives)
      sum += obj.coverage();
    return sum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#low()
   */
  public double low() {
    double sum = 0;
    for (CoverageObjective obj : objectives)
      sum += obj.low();
    return sum;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#high()
   */
  public double high() {
    double sum = 0;
    for (CoverageObjective obj : objectives)
      sum += obj.high();
    return sum;
  }

  /**
   * @return Returns the objectives.
   */
  public List getObjectives() {
    return objectives;
  }

  /**
   * @param objectives
   *          The objectives to set.
   */
  public void setObjectives(List objectives) {
    this.objectives = objectives;
  }

  /**
   * Add another objective to this aggregate.
   * 
   * @param obj
   */
  public void addObjective(CoverageObjective obj) {
    objectives.add(obj);
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name) {
    this.name = name;
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
   * @see oqube.patchwork.report.coverage.CoverageObjective#reset()
   */
  public void reset() {
    for (CoverageObjective obj : objectives)
      obj.reset();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#update(java.util.Map)
   */
  public void update(Map codepaths) {
    for (CoverageObjective obj : objectives)
      obj.update(codepaths);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.CoverageObjective#update(int,
   *      java.lang.String, int)
   */
  public void update(int tid, String method, int block) {
    for (CoverageObjective obj : objectives)
      obj.update(tid, method, block);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageListener#hit()
   */
  public int hit() {
    int hit = 0;
    for (CoverageObjective obj : objectives)
      hit += obj.hit();
    return hit;
  }

}
