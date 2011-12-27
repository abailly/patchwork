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

import oqube.patchwork.report.CoverageListener;

/**
 * Generic interface for assessing coverage status of some piece of instrumented
 * software.
 * 
 * This interface assumes that coverage measure is a continuous and increasing
 * function of the set of paths executed: If more path are added, coverage must
 * not decrease.
 * 
 * @author nono
 * 
 */
public interface CoverageObjective extends CoverageListener {

  /**
   * Returns the name of this objective.
   * 
   * @return
   */
  String getName();

  /**
   * Reset this objective coverage status.
   * 
   */
  void reset();

  /**
   * Return current coverage metrics for this objective.
   * 
   * @return
   */
  double coverage();

  /**
   * Gets the low threshold for this objective. This number should be related to
   * the value returned by coverage in a consistent way.
   * 
   * @return a positive number representing the minimum threshold of coverage to
   *         have for considering this objective to be reached.
   */
  double low();

  /**
   * Gets the high threshold for this objective. This number should be related
   * to the value returned by coverage in a consistent way.
   * 
   * @return a positive number representing the maximum threshold of coverage to
   *         have for considering this objective to be reached.
   */
  double high();

  /**
   * Traversal method for the visitor pattern.
   * 
   * @param vis
   */
  void visit(ObjectiveVisitor vis);
}
