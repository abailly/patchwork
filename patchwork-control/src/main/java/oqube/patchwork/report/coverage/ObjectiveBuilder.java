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
import java.util.Collection;

/**
 * This interface is used to build coverage objective reporters for 
 * a set of classes.
 * Implementations of this interface are used normally by test harness
 * to report coverage information for a set of classes. It is up to 
 * the actual builder to construct the necessary informations for 
 * effective reporting of coverage information.
 * 
 * @author nono
 *
 */
public interface ObjectiveBuilder {

  /**
   * Build an instance of coverage objective for the given
   * set of classes.
   * 
   * @param classes collection of class names. These classes are looked up in the
   * current classpath.
   * @return a CoverageObjective instance.
   * @throws IOException 
   */
  CoverageObjective build(Collection<String> classes) throws IOException;
  
}
