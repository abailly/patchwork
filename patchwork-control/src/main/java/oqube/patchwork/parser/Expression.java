/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.patchwork.parser;

import oqube.patchwork.expr.ExpressionPrettyPrinter;

/**
 * 
 * INSERT DESCRIPTION HERE
 *
 * @author abailly
 * @param <T> Usual self-type encoding parameter, used to specialize method returns and callbacks.
 */
public interface Expression<T extends Expression<T>> {

  ExpressionPrettyPrinter appendTo(ExpressionPrettyPrinter pp);
}
