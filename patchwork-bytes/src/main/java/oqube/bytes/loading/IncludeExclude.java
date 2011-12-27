/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.bytes.loading;

import java.util.regex.Pattern;

/**
 * Include/exclude logic filter with string patterns.
 * A string is {@link #accept(String)}ed by this filter iff. it is:
 * <ul>
 * <li>Accepted by the include pattern, <strong>and</strong> </li>
 * <li>Not accepted by the exclude pattern.</li>
 * </ul>
 */
public class IncludeExclude {

  private final Pattern includePattern;
  private final Pattern excludePattern;

  public IncludeExclude(String includePattern, String excludePattern) {
    this.includePattern = Pattern.compile(includePattern);
    this.excludePattern = Pattern.compile(excludePattern);
  }

  public IncludeExclude(String includePattern) {
    this(includePattern, "");
  }

  /**
   * 
   * @param string 
   * @return true if the string is accepted by this {@link IncludeExclude} filter.
   */
  public boolean accept(String string) {
    return includePattern.matcher(string).matches() && !excludePattern.matcher(string).matches();
  }

}
