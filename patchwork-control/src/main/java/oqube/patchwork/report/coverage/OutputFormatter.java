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

 Created 12 sept. 2006
 */
package oqube.patchwork.report.coverage;

import java.io.PrintWriter;

import oqube.patchwork.report.source.SourceMapper;

/**
 * Interface for formatting control flow coverage objectives.
 * 
 * @author nono
 * 
 */
public interface OutputFormatter extends ObjectiveVisitor {

  /**
   * Sets the output writer where data is formatted.
   * 
   * @param writer
   */
  void setOut(PrintWriter writer);

  /**
   * called when work has been done.
   * 
   */
  void done();

  void start();

  void setSourceMap(SourceMapper sm);

  /**
   * Sets low percentage threshold for reporting.
   * 
   * @param low
   *          the coverage percentage at which some alert should be triggered.
   *          Defaults to 50%.
   */
  void setLow(double low);

  /**
   * Sets high percentage threshold for reporting.
   * 
   * @param high
   *          the coverage percentage to reach. Defaults to 90%.
   */
  void setHigh(double high);
}
