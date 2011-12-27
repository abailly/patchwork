/**
 * 
 */
package oqube.patchwork.report.source;

import java.util.BitSet;
import java.util.List;

import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;

/**
 * A class that handle mapping between a control graph and line numbers. This
 * class keep tracks of association between block numbers and line numbers. It
 * can be used to retrieve, given some sequence of coverage events, the part of
 * line that has been covered.
 * 
 * @author nono
 * 
 */
public class LineAndBlocks {

  public static class StartEnd {
    final int start, end;

    public StartEnd(int s, int e) {
      this.start = s;
      this.end = e;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      StartEnd se = (StartEnd) obj;
      return se.start == start && se.end == end;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "(" + start + ", " + end + ")";
    }

  }

  public LineAndBlocks(ControlGraph cg) {
    List<BasicBlock> l = cg.getBlocks();
    this.blocks = new StartEnd[l.size()];
    this.covBlocks = new BitSet(l.size());
    int i = 0;
    for (BasicBlock bb : l) {
      this.blocks[i++] = new StartEnd(bb.getStartLine(), bb.getEndLine());
    }
  }

  /*
   * count of blocks in each line index is line number, content is number of
   * blocks in this line.
   */
  private int[] covs;

  private StartEnd[] blocks;

  private BitSet covBlocks;

  /**
   * Returns the start and end lines of a block.
   * 
   * @param i
   *          block number.
   * @return a pair of line numbers.
   */
  public StartEnd getStartEnd(int i) {
    return blocks[i - 1];
  }

  /**
   * updates the coverage information for given block. This method asserts that
   * code at given block hence between start and end line inclusives have been
   * executed.
   * 
   * @param i
   *          block number. Must be greater than zero,
   */
  public void update(int i) {
    if (i > 0)
      covBlocks.set(i - 1);
  }

  /**
   * Return the number of fragment of given line that has been covered. A
   * fragment corresponds to some block starting and/or ending on this line.
   * 
   * @param line
   *          a line number (1 based).
   * @return the number of executions for this line.
   */
  public int covered(int line) {
    int ret = 0;
    for (int j = 0, ln = blocks.length; j < ln; j++)
      if (line >= blocks[j].start && line <= blocks[j].end && covBlocks.get(j))
        ret++;
    return ret;
  }

  /**
   * Return the maximum number of blocks that covers a given line.
   * 
   * @param line
   *          a line number (1 based).
   * @return number of blocks ending, passing through or starting at this line.
   */
  public int blocks(int line) {
    int ret = 0;
    for (int j = 0, ln = blocks.length; j < ln; j++)
      if (line >= blocks[j].start && line <= blocks[j].end)
        ret++;
    return ret;
  }
}
