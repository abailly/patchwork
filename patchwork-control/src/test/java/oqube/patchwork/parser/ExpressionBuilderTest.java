/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.parser;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import oqube.patchwork.expr.ExpressionBuilder;
import oqube.patchwork.expr.ExpressionPrettyPrinter;
import oqube.patchwork.expr.MethodCall;

import org.junit.Ignore;
import org.junit.Test;

public class ExpressionBuilderTest {

  @Test
  @Ignore
  public void canBuildASimpleCallWithoutParameter() throws Exception {
    ExpressionBuilder builder = new ExpressionBuilder();
    builder.open(new File("target/test-classes/buildertest.bytes"));
    MethodCall call = builder.<MethodCall> build("simpleCall").left().value();
    assertThat(new ExpressionPrettyPrinter().prettyPrint(call), is("callMethod"));
  }
}
