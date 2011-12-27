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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import oqube.patchwork.report.source.SourceMapper;

/**
 * Generate an XHMTL format of coverage information. This formatter creates a
 * single XHTML file for outputting coverage information of a complete tree of
 * coverage objectives. Optionally, it generates links to decorated source
 * files.
 * 
 * 
 * @author nono
 * 
 */
public class XHTMLFormatter extends SimpleTextOutput {

  private double high;

  private double low;

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#setHigh(double)
   */
  @Override
  public void setHigh(double high) {
    this.high = high / 100;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#setLow(double)
   */
  @Override
  public void setLow(double low) {
    this.low = low / 100;
  }

  private NumberFormat nums = new DecimalFormat("0");

  private NumberFormat perc = new DecimalFormat("0.00 %");

  private SourceMapper sourceMap;

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AggregateObjective)
   */
  public void visit(AggregateObjective obj) {
    double percent = (obj.coverage() / obj.high());
    /* extract reference */
    String ref = null;
    if (sourceMap != null)
      ref = sourceMap.highlight(obj.getName());
    /* output header */
    assert obj != null;
    assert obj.getName() != null;
    out.append("<table id=\"").append(URLEncoder.encode(obj.getName())).append(
        "\" >").println();
    out.append("<tr><th>");
    if (ref != null)
      out.append("<a href=\"").append(ref).append("\" >");
    out.append(obj.getName());
    if (ref != null)
      out.append("</a>");
    out.append("</th>");
    /* aggregate data */
    out.append("<th class=\"hit\" >").append(nums.format(obj.hit())).append(
        "</th>");
    out.append("<th class=\"high\" >").append(nums.format(obj.high())).append(
        "</th>");
    out.append("<th class=\"coverage\" >").append(nums.format(obj.coverage()))
        .append("</th>");
    out.append("<th class=\"percent\" >").append(perc.format(percent)).append(
        "</th></tr>"); /* output body */
    for (Iterator i = obj.getObjectives().iterator(); i.hasNext();)
      ((CoverageObjective) i.next()).visit(this);
    out.append("</table>").println();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AllNodesObjective)
   */
  public void visit(AllNodesObjective obj) {
    format("node-coverage", obj);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#visit(oqube.patchwork.report.coverage.CompoundObjective)
   */
  @Override
  public void visit(CompoundObjective obj) {
    format("compound-coverage", obj);
  }

  /**
   * @param obj
   */
  private void format(String cls, MethodObjective obj) {
    double percent = (obj.coverage() / obj.high());
    if (percent <= low)
      cls += "-low";
    else if (percent >= high)
      cls += "-high";
    else
      cls += "-middle";
    String name = obj.getMethodName();
    if (name.startsWith("<")) // special methods
      name = name.replace("<", "&lt;").replaceAll(">", "&gt;");
    out.append("<tr class=\"").append(cls).append("\" id=\"").append(
        URLEncoder.encode(obj.getName())).append(
        "\"><td class=\"method-name\" >").append(name).append("</td>");
    out.append("<td class=\"hit\" >").append(nums.format(obj.hit())).append(
        "</td>");
    out.append("<td class=\"high\" >").append(nums.format(obj.high())).append(
        "</td>");
    out.append("<td class=\"coverage\" >").append(nums.format(obj.coverage()))
        .append("</td>");
    out.append("<td class=\"percent\" >").append(perc.format(percent)).append(
        "</td></tr>");
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveVisitor#visit(oqube.patchwork.report.coverage.AllEdgesObjective)
   */
  public void visit(AllEdgesObjective obj) {
    format("edge-coverage", obj);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#done()
   */
  public void done() {
    out.append("</body></html>");
    super.done();
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#start()
   */
  public void start() {
    super.start();
    out.append("<html><head><title>Coverage report</title>");
    // add css
    BufferedReader br = new BufferedReader(new InputStreamReader(getClass()
        .getResourceAsStream("/base.css")));
    out.append("<style type=\"text/css\">");
    String line;
    try {
      while ((line = br.readLine()) != null)
        out.append(line).println();
    } catch (IOException e) {
      // IGNORE ?
    }
    out.append("</style>");
    out.append("</head><body>");
    out.append("low-threshold:").append(perc.format(low)).append(
        ", high-threshold: ").append(perc.format(high)).append("<br />");
  }

  /**
   * @return Returns the sourceMap.
   */
  public SourceMapper getSourceMap() {
    return sourceMap;
  }

  /**
   * @param sourceMap
   *          The sourceMap to set.
   */
  public void setSourceMap(SourceMapper sourceMap) {
    this.sourceMap = sourceMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.SimpleTextOutput#visit(oqube.patchwork.report.coverage.AllDUPairsObjective)
   */
  @Override
  public void visit(AllDUPairsObjective obj) {
    format("all-du-pairs", obj);
  }
}
