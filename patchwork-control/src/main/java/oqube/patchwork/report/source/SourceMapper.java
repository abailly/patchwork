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
package oqube.patchwork.report.source;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;
import oqube.patchwork.report.CoverageListener;

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

/**
 * A utility class that constructs a map from class names to source URLs using a
 * sourcepath list and that can highlight source code for executed blocks. This
 * class can be used if source code rendering is needed.
 * 
 * @author nono
 * 
 */
public class SourceMapper implements CoverageListener {

  private Logger log = Logger.getLogger(SourceMapper.class.getName());

  private static final String EOL = System.getProperty("line.separator");

  /*
   * renderer for outputting highlighted source code
   */
  private final Renderer defaultRenderer = XhtmlRendererFactory
      .getRenderer("java");

  /*
   * base directory for storing generated files
   */
  private File basedir = new File(".");

  /*
   * control graph builder instance.
   */
  private ControlGraphBuilder graphBuilder;

  /*
   * prefix added to urls for highlighted files
   */
  private String urlPrefix = "";

  /*
   * a map from class names to list of source code segments covered.
   */
  private Map<String, BitSet> hltLines = new HashMap<String, BitSet>();

  /*
   * mapper object from base source names to URLs
   */
  private SourceToURL sourceToURL;

  /*
   * map of line coverage info per method.
   */
  private Map<String, SourceLines> sourceLines = new HashMap<String, SourceLines>();

  /*
   * same information as above, but mapped by methods names.
   */
  private Map<String, LineAndBlocks> methodLines = new HashMap<String, LineAndBlocks>();

  private int hit;

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.CoverageListener#hit()
   */
  public int hit() {
    return hit;
  }

  /**
   * Produce xhtml file for given name. Assume name is a dot or slash separated
   * list of components tha are transformed into directories.
   * 
   * @param name
   *          the base name of file to highlight.
   * @return a link to the newly created file. Maybe null in case of error.
   */
  public String highlight(String name) {
    try {
      URL url = sourceToURL.get(name);
      SourceLines lines = sourceLines.get(name);
      if (url == null || lines == null)
        return null;
      InputStream is = url.openStream();
      /* create output file */
      String fname = basedir.getPath() + File.separator + name + ".html";
      File dir = new File(fname.substring(0, fname
          .lastIndexOf(File.separatorChar)));
      if (!dir.exists())
        dir.mkdirs();
      // format source
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      // makeup renderer
      Renderer renderer = makeRenderer(name);
      log.fine("using renderer " + renderer.getClass().getName());
      renderer.highlight(name, is, bos, "UTF-8", false);
      // add line coverage info
      // create bitset for covered lines
      addCoveredLinesFormat(name, fname, bos.toByteArray(), lines);
      log.fine("done highlighting, returning " + urlPrefix + name + ".html");
      return urlPrefix + name + ".html";
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Renderer makeRenderer(String name) {
    // extract suffix
    int dot = name.lastIndexOf('.');
    String suf = dot == -1 ? "java" : name.substring(dot + 1);
    Renderer r = XhtmlRendererFactory.getRenderer(suf);
    if (r == null)
      return defaultRenderer;
    else
      return r;
  }

  /*
   * add span class around each covered line in source format.
   */
  void addCoveredLinesFormat(String name, String fname, byte[] bs,
      SourceLines lines) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(
        new ByteArrayInputStream(bs)));
    PrintStream pos = new PrintStream(new FileOutputStream(fname));
    String line;
    int ln = 2; // why ?
    Pattern pat = Pattern.compile("^<span class=\"java_.*");
    // match
    while ((line = br.readLine()) != null) {
      if (pat.matcher(line).matches()) {
        int cov = lines.covered(ln);
        int max = lines.blocks(ln);
        String color = makeColor(cov, max);
        // change color according to amount covered
        pos.append("<div style=\"background-color:" + color + ";\">").append(
            line).append(EOL).append("</div>");
        ln++;
      } else
        pos.append(line).append(EOL);
    }
    pos.flush();
    pos.close();
  }

  /*
   * return a string representing transition from red to green according to
   * percent of coverage
   */
  private String makeColor(int cov, int max) {
    if (max == 0)
      return "#fff";
    int r = 15 - ((cov * 15) / max);
    int g = ((cov * 15) / max);
    assert r + g == 15;
    return "#" + Integer.toHexString(r) + Integer.toHexString(g) + "0";
  }

  /**
   * @return Returns the log.
   */
  public Logger getLog() {
    return log;
  }

  /**
   * @param log
   *          The log to set.
   */
  public void setLog(Logger log) {
    this.log = log;
  }

  /**
   * Update coverage information for this source mapper. The given path lists is
   * used to highlight source code by translating executed blocks to their line
   * equivalent.
   * 
   * @param cp
   *          a Map<String, List<int[]>> instance from full method names to
   *          list of blocks.
   */
  public void update(Map cp) {
    for (Iterator i = cp.entrySet().iterator(); i.hasNext();) {
      Map.Entry me = (Map.Entry) i.next();
      String method = (String) me.getKey();
      List blocks = (List) me.getValue();
      if (blocks.isEmpty())
        continue;
      /* extract control flow graph */
      int dot = method.lastIndexOf('.');
      if (dot == -1)
        throw new IllegalArgumentException("Invalid method name " + method
            + ": Must be <class>.<method><signature>");
      int paren = method.lastIndexOf('(');
      if (paren == -1)
        throw new IllegalArgumentException("Invalid signature in " + method
            + ": Must be <class>.<method><signature>");
      String cln = method.substring(0, dot);
      String mn = method.substring(dot + 1, paren);
      String signature = method.substring(paren);
      try {
        ControlGraph cg = graphBuilder.createGraphForMethod(cln, mn, signature);
        // order cg blocks by source lines numbers
        Comparator<BasicBlock> comp = new Comparator<BasicBlock>() {
          public int compare(BasicBlock bb1, BasicBlock bb2) {
            int s1, s2, e1, e2;
            s1 = bb1.getStartLine();
            s2 = bb2.getStartLine();
            e1 = bb1.getEndLine();
            e2 = bb2.getEndLine();
            return s1 < s2 ? -1 : s1 > s2 ? 1 : (e1 < e2 ? -1 : (e1 > e2 ? 1
                : 0));
          }
        };
        List<BasicBlock> l = cg.getBlocks();
        if (l.isEmpty())
          continue;
        Collections.sort(l, comp);
        // laready covered lines
        BitSet cov = (BitSet) hltLines.get(cln);
        if (cov == null) {
          cov = new BitSet();
          hltLines.put(cln, cov);
        }
        // add all covered blocks to this class coverage info
        for (Iterator j = blocks.iterator(); j.hasNext();) {
          int[] path = (int[]) j.next();
          for (int k = 0; k < path.length; k++) {
            BasicBlock bb = (BasicBlock) l.get(path[k] - 1);
            for (int m = bb.getStartLine(); m < bb.getEndLine(); m++) {
              cov.set(m - 1);
            }
          }
        }
      } catch (IOException e) {
        log.severe("Cannot construct graph for method " + method);
        e.printStackTrace();
      }
    }
  }

  /**
   * Online update of covered lines information. This method is inefficient and
   * need to be refactored.
   */
  public void update(int tid, String method, int block) {
    LineAndBlocks lines = methodLines.get(method);
    if (lines == null) {
      ControlGraph cg;
      try {
        cg = graphBuilder.createGraphForMethod(method);
        String sname = cg.getSourceFile();
        if (sname != null) {
          SourceLines sl = sourceLines.get(sname);
          if (sl == null) {
            sl = new SourceLines();
            sourceLines.put(sname, sl);
          }
          assert sl != null;
          sl.put(method, lines = new LineAndBlocks(cg));
          methodLines.put(method, lines);
        } else
          return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }
    assert lines != null;
    hit++;
    lines.update(block);
  }

  /**
   * @return the graphBuilder
   */
  public ControlGraphBuilder getGraphBuilder() {
    return graphBuilder;
  }

  /**
   * @param graphBuilder
   *          the graphBuilder to set
   */
  public void setGraphBuilder(ControlGraphBuilder graphBuilder) {
    this.graphBuilder = graphBuilder;
  }

  /**
   * @return the sourceToURL
   */
  public SourceToURL getSourceToURL() {
    return sourceToURL;
  }

  /**
   * @param sourceToURL
   *          the sourceToURL to set
   */
  public void setSourceToURL(SourceToURL sourceToURL) {
    this.sourceToURL = sourceToURL;
  }

  /**
   * @return the basedir
   */
  public File getBasedir() {
    return basedir;
  }

  /**
   * @param basedir
   *          the basedir to set
   */
  public void setBasedir(File basedir) {
    this.basedir = basedir;
  }

  /**
   * @return the urlPrefix
   */
  public String getUrlPrefix() {
    return urlPrefix;
  }

  /**
   * @param urlPrefix
   *          the urlPrefix to set
   */
  public void setUrlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

}
