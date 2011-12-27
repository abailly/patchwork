/**
 * 
 */
package oqube.patchwork.report.source;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that stores {@link LineAndBlocks} instances associated to some source
 * file. A {@link LineAndBlocks} object is associated to one control graph of
 * one method, while a SourceLines instance holds all possible line and blocks
 * for a given source file (which may contain several classes).
 * 
 * @author nono
 * 
 */
public class SourceLines {

  /*
   * stores an association between method full names and their line coverage
   * info.
   */
  private Map<String, LineAndBlocks> methodLines = new HashMap<String, LineAndBlocks>();

  public void put(String meth1, LineAndBlocks line1) {
    this.methodLines.put(meth1, line1);
  }

  public LineAndBlocks get(String meth1) {
    return methodLines.get(meth1);
  }

  public int covered(int i) {
    int ret = 0;
    for(LineAndBlocks lb : methodLines.values())
      ret += lb.covered(i);
    return ret;
  }

  public int blocks(int i) {
    int ret = 0;
    for(LineAndBlocks lb : methodLines.values())
      ret += lb.blocks(i);
    return ret;
  }

}
