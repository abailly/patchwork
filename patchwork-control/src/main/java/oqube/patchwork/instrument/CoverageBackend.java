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
package oqube.patchwork.instrument;

import java.io.OutputStream;

import oqube.patchwork.report.Coverage;

/**
 * Interface for backends to the coverage reporting.
 * Instances of this interface are used by the {@link Coverage} class
 * to retrieve output stream. This allows fine tuning and customization of
 * the coverage information retrieval process.
 * 
 * @author nono
 *
 */
public interface CoverageBackend {

  /**
   * Returns an output stream where coverage events are to
   * be reported. 
   * 
   * @return a non-null output stream.
   */
  OutputStream getCoverageStream();

  /**
   * Method called by Coverage engine at start of coverage reporting
   * session.
   *
   */
  void start();

  /**
   * Method called by Coverage engine at end of coverage session.
   *
   */
  void done();

  
}
