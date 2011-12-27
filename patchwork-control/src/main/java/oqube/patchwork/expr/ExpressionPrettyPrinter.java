/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.expr;

import oqube.patchwork.parser.Expression;

public class ExpressionPrettyPrinter {

  private StringBuilder buffer = new StringBuilder();

  /**
   * Pretty-prints an expression.
   * 
   * This pretty-printer outputs chained method calls as a series of word: It prints method names,
   * argument names and values and discards the usual dots and parenthesis used in real code.
   * @param expression
   * @return
   */
  public <T extends Expression<T>> String prettyPrint(T expression) {
    return expression.appendTo(this).asString();
  }

  private String asString() {
    return buffer.toString();
  }

  public void append(String string) {
    buffer.append(string);
  }

}
