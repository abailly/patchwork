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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import oqube.patchwork.graph.ControlGraphBuilder;
import oqube.patchwork.report.coverage.CoverageObjective;
import oqube.patchwork.report.coverage.OutputFormatter;
import oqube.patchwork.report.coverage.SimpleTextOutput;
import oqube.patchwork.report.source.SourceMapper;

/**
 * This reporter reads data from a coverage stream and produces nodes and edges
 * coverage information
 * 
 * @author nono
 * 
 */
public class SimpleReporter implements Reporter {

  private CoverageObjective   objective;

  private OutputFormatter     formater = new SimpleTextOutput();

  private static final Logger log      = Logger.getLogger(SimpleReporter.class.getName());

  private SourceMapper        sourceMapper;

  private ControlGraphBuilder graphBuilder;

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.Reporter#analyze(java.io.InputStream)
   */
  public void analyze(InputStream is) throws IOException {
    log.info("Start analyzing coverage stream");
    long ts = System.nanoTime();
    List<short[]> indices = new ArrayList<short[]>();
    String[][] names = storeIndicesFrom(is, indices);
    log.info("Read " + names.length + " classes names");
    // extract class names
    updateCoverageFromStream(names, indices);
    log.info("done analyzing coverage stream in " + (System.nanoTime() - ts) / 1000000 + " ms");
  }

  private String[][] storeIndicesFrom(InputStream is, List<short[]> indices) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is, Coverage.bufferSize);
    DataInputStream dis = new DataInputStream(bis);
    long val = 0;
    long count = 0;
    while(val != -1) {
      val = dis.readLong();
      if(val != -1) {
        count++;
        // extract info from val
        short tid = (short)((val >> 48) & 0xffff);
        short cid = (short)((val >> 32) & 0xffff);
        short mid = (short)((val >> 16) & 0xffff);
        short bid = (short)(val & 0xffff);
        indices.add(new short[] { tid, cid, mid, bid });
      } else {
        log.info("read "+count + " coverage records");
      }
    }
    return readClassesAndMethods(dis);
  }

  private void updateCoverageFromStream(String[][] names, List<short[]> indices) {
    for(short[] index : indices) {
      short tid = index[0];
      short cid = index[1];
      assert cid < names.length;
      short mid = index[2];
      assert mid < names[cid].length;
      short bid = index[3];
      String mname = names[cid][mid + 1];
      objective.update(tid, mname, bid);
      if(sourceMapper != null)
        sourceMapper.update(tid, mname, bid);
    }
  }

  private void checkpoint() {
    try {
      FileOutputStream out = new FileOutputStream(new File(".patchwork.tmp"));
      report(out);
      out.flush();
      out.close();
    } catch(IOException e) {
      e.printStackTrace();
      log.severe("Failed to checkpoint, " + e.getLocalizedMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.patchwork.report.Reporter#report(java.io.OutputStream)
   */
  public void report(OutputStream os) {
    formater.setOut(new PrintWriter(os));
    formater.setSourceMap(sourceMapper);
    formater.start();
    // report simple
    objective.visit(formater);
    formater.done();
  }

  /**
   * This method is used to read the informations in a coverage file. It reads
   * data from an input stream, wraps it in a data stream and returns a two
   * dimensional array of strings where the first string in a line is the class
   * name followed by its method names. Note that it does not modifies the
   * stream in any way other than reading it, which means that the position of
   * the stream when this method returns is the start of block execution
   * recording.
   * 
   * @param stream
   *          data stream
   * @return a String[][] array with names.
   * @throws IOException
   *           If error in reading stream.
   */
  String[][] readClassesAndMethods(DataInputStream dis) throws IOException {
    int sz = dis.readInt();
    String[][] ret = new String[sz][];
    for(int i = 0; i < sz; i++) {
      String cln = dis.readUTF();
      int nm = dis.readInt();
      ret[i] = new String[nm + 1];
      ret[i][0] = cln;
      for(int j = 0; j < nm; j++) {
        ret[i][j + 1] = cln + '.' + dis.readUTF();
      }
    }
    return ret;
  }

  /**
   * @return Returns the sourceMapper.
   */
  public SourceMapper getSourceMapper() {
    return sourceMapper;
  }

  /**
   * @param sourceMapper
   *          The sourceMapper to set.
   */
  public void setSourceMapper(SourceMapper sourceMapper) {
    this.sourceMapper = sourceMapper;
    if(formater != null)
      formater.setSourceMap(sourceMapper);
  }

  /**
   * @return Returns the output.
   */
  public OutputFormatter getFormater() {
    return formater;
  }

  /**
   * @param output
   *          The output to set.
   */
  public void setFormater(OutputFormatter output) {
    this.formater = output;
    if(sourceMapper != null)
      output.setSourceMap(sourceMapper);
  }

  /**
   * @return Returns the objective.
   */
  public CoverageObjective getObjective() {
    return objective;
  }

  /**
   * @param objective
   *          The objective to set.
   */
  public void setObjective(CoverageObjective objective) {
    this.objective = objective;
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

}
