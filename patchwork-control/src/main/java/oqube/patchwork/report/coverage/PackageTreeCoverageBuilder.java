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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oqube.bytes.Constants;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;

import org.apache.commons.logging.Log;

/**
 * This class builds a tree of coverage objectives using aggregate nodes for
 * package and class level aggregation and AllNodes or All-Edges coverage for
 * each method in these classes.
 * 
 * @author nono
 */
public class PackageTreeCoverageBuilder implements ObjectiveBuilder {

  private ControlGraphBuilder graphBuilder;

  private Log log;

  private List<Class> objectives;

  /**
   * @return Returns the log.
   */
  public Log getLog() {
    return log;
  }

  /**
   * @param log
   *          The log to set.
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Root of the objective. This class dispatches events to the leaf objectives
   * using a map from full method name to objective.
   * 
   * @author nono
   * 
   */
  class PackageTreeObjective extends AggregateObjective {

    Map<String, CoverageObjective> methods = new HashMap<String, CoverageObjective>();

    /*
     * (non-Javadoc)
     * 
     * @see oqube.patchwork.report.coverage.AggregateObjective#update(int,
     *      java.lang.String, int)
     */
    @Override
    public void update(int tid, String method, int block) {
      CoverageObjective obj = methods.get(method);
      if (obj != null)
        obj.update(tid, method, block);
      else
        // default to walking hierarchy
        super.update(tid, method, block);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.coverage.ObjectiveBuilder#build(java.util.List)
   */
  public CoverageObjective build(Collection<String> classes) {
    /* packages */
    PackageTreeObjective full = new PackageTreeObjective();
    full.setName(MethodObjective.getDisplayName(objectives));
    Map /* < String, AggregateObjective > */packs = new HashMap();
    for (Iterator i = classes.iterator(); i.hasNext();) {
      String cln = (String) i.next();
      int dot = cln.lastIndexOf('/');
      String pn = (dot != -1) ? cln.substring(0, dot) : "";
      /* get aggregate for pack */
      AggregateObjective pack = (AggregateObjective) packs.get(pn);
      if (pack == null) {
        pack = new AggregateObjective();
        pack.setName(pn);
        full.addObjective(pack);
        packs.put(pn, pack);
      }
      /* append objective for class and all methods in class */
      AggregateObjective clag = new AggregateObjective();
      Map m;
      try {
        m = graphBuilder.createGraphsForClass(cln);
      } catch (IOException e) {
        log.error("Cannot build graph for " + cln + " from builder "
            + graphBuilder, e);
        continue;
      }
      clag.setName(cln);
      // iterate over methods
      for (Iterator j = m.entrySet().iterator(); j.hasNext();) {
        Map.Entry me = (Map.Entry) j.next();
        String k = (String) me.getKey();
        ControlGraph cg = (ControlGraph) me.getValue();
        // skip synthetic methods
        MethodFileInfo mfi = cg.getMethod();
        if (mfi != null && (mfi.getFlags() & Constants.ACC_SYNTHETIC) > 0)
          continue;
        MethodObjective obj;
        // reset name of enclosing class aggregate to sourcefile
        if (cg.getSourceFile() != null)
          clag.setName(cg.getSourceFile());
        try {
          obj = instantiateObjective();
        } catch (Exception e) {
          log.error("Cannot make " + objectives + " instance for " + k + ": "
              + e.getLocalizedMessage(), e);
          continue;
        }
        obj.setClassName(cln);
        obj.setGraph(cg);
        dot = k.lastIndexOf('.');
        int paren = k.lastIndexOf('(');
        // name is extracted from class file so it is correctly formatted
        assert paren != -1;
        obj.setMethodName(k.substring(dot + 1, paren));
        obj.setSignature(k.substring(paren));
        full.methods.put(obj.getMethod(), obj);
        clag.addObjective(obj);
      }
      pack.addObjective(clag);
    }
    return full;
  }

  private MethodObjective instantiateObjective() throws InstantiationException,
      IllegalAccessException {
    if (objectives.size() == 1)
      return (MethodObjective) objectives.get(0).newInstance();
    else {
      // construct compound objective
      CompoundObjective comp = new CompoundObjective();
      for (Class<? extends MethodObjective> cls : objectives)
        comp.add(cls.newInstance());
      return comp;
    }
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
   * @return the objectiveClass
   */
  public List<Class> getObjectives() {
    return objectives;
  }

  /**
   * @param objectiveClass
   *          the objectiveClass to set
   */
  public void setObjectives(List<Class> objectiveClass) {
    this.objectives = objectiveClass;
  }

}
