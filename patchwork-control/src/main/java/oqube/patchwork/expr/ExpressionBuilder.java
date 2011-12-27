/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.expr;

import java.io.File;

import fj.data.Either;
import fj.data.Either.LeftProjection;

import oqube.patchwork.parser.Expression;

public class ExpressionBuilder {

  /**
   * 
   * @param file a file containing bytecode for a class.
   * @return this builder for chaining calls.
   */
  public ExpressionBuilder open(File file) {
    return this;
  }

  /**
   * Parse a instructions from a given method and generated corresponding expression tree.
   * 
   * @param methodName
   * @return an expression if it succeeds or a {@link Throwable} in case of error.
   */
  public <T extends Expression<T>> Either<T, Throwable> build(String methodName) {
    return Either.left(null);
  }

}
