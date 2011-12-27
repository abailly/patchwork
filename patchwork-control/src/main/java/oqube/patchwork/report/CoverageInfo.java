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
package oqube.patchwork.report;

/**
 * Interface for populating coverage data (classes names and methods)
 * in the {@link oqube.patchwork.report.Coverage} class.
 * This interface is normally implemented by generated class for a set
 * of instrumented classfiles.
 * 
 * @author nono
 * @see oqube.patchwork.instrument.CoverageGenerator
 */
public interface CoverageInfo {

  /**
   * 
   * @return the array of class names covered.
   */
  String[] getClasses();
  
  /**
   * 
   * @return a two dimensional array of methods: each line is a class,
   * each column a method in the class.
   */
  String[][] getMethods();
  
}
