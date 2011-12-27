/**
 * Copyright 2011 FoldLabs All Rights Reserved.
 *
 * This software is the proprietary information of FoldLabs
 * Use is subject to license terms.
 */
package oqube.bytes.loading;

import java.util.Map;

import oqube.bytes.ClassFile;

public interface ClassFactory {

  public ClassFile instrument(ClassFile cf);

  public Map<String, ClassFile> getGenerated();

}
