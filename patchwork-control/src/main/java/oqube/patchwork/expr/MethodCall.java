/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.expr;

import oqube.patchwork.parser.Expression;

public class MethodCall implements Expression<MethodCall> {

  private final String methodName;

  public MethodCall(String methodName) {
    this.methodName = methodName;
  }

  public ExpressionPrettyPrinter appendTo(ExpressionPrettyPrinter pp) {
    pp.append(methodName);
    return pp;
  }

}
