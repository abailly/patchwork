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

import java.io.PrintWriter;
import java.util.Iterator;

import oqube.patchwork.report.source.SourceMapper;

/**
 * A simple class for outputting coverage data in text format. This class simply
 * traverse a hierarchy of objectives and outputs the low, high and achieved
 * coverage information, with percent to high.
 * 
 * @author nono
 * 
 */
public class SimpleTextOutput implements OutputFormatter {

  protected PrintWriter out;

  /**
   * @return Returns the out.
   */
  public PrintWriter getOut() {
    return out;
  }

  /**
   * @param out
   *          The out to set.
   */
  public void setOut(PrintWriter out) {
    this.out = out;
  }

  private void format(CoverageObjective obj) {
    StringBuffer sb = new StringBuffer();
    sb.append(obj.getName()).append(" ; ").append(obj.hit()).append(" ; ").append(obj.low()).append(" ; ")
        .append(obj.high()).append(" ; ").append(obj.coverage()).append(" ; ")
        .append(obj.coverage() / obj.high() * 100);
    out.println(sb);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AggregateObjective)
   */
  public void visit(AggregateObjective obj) {
    format(obj);
    for(Iterator i = obj.getObjectives().iterator();i.hasNext();) 
      ((CoverageObjective)i.next()).visit(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AllNodesObjective)
   */
  public void visit(AllNodesObjective obj) {
    format(obj);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AllEdgesObjective)
   */
  public void visit(AllEdgesObjective obj) {
    format(obj);
  }


  /* (non-Javadoc)
   * @see oqube.patchwork.report.coverage.OutputFormatter#setSourceMap(oqube.patchwork.report.coverage.SourceMapper)
   */
  public void setSourceMap(SourceMapper sm) {
    // NOP
  }

  public void start(){    
  }
  
  public void done() {
    out.flush();
  }

  /* (non-Javadoc)
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AllDUPairsObjective)
   */
  public void visit(AllDUPairsObjective obj) {
    format(obj);    
  }

  public void visit(CompoundObjective obj) {
    format(obj);
  }

  public void setHigh(double high) {
    // NOP
  }

  public void setLow(double low) {
    // NOP
  }

}
