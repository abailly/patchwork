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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import oqube.patchwork.report.coverage.CoverageObjective;
import oqube.patchwork.report.coverage.OutputFormatter;
import oqube.patchwork.report.source.SourceMapper;

/**
 * An interface for analyzing a stream of coverage information
 * and producing meaningful information from it.
 * 
 * @author nono
 *
 */
public interface Reporter {

  /**
   * Analyze the content of teh given stream.
   * 
   * @param is an input stream produced by Coverage.
   * @throws IOException 
   */
  void analyze(InputStream is) throws IOException;
  
  /**
   * Output the analysis report to given stream.
   * 
   * @param os an output stream.
   */
  void report(OutputStream os);

  /**
   * Defines the objective to be reached by this reporter.
   * 
   * @param objective
   */
  void setObjective(CoverageObjective objective);

  /**
   * Sets the output formatter.
   * 
   * @param formatter
   */
  void setFormater(OutputFormatter formatter);
  
  /**
   * The sourcemapper to use for ouputting coverage information
   * with source lines.
   * 
   * @param sm a SourceMapper. May be null, which means no information 
   * about lines covered wiil be output.
   */
  void setSourceMapper(SourceMapper sm);
}
