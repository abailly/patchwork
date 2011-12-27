/*
 * Copyright 2011 PolySpot, S.A.S. All Rights Reserved.
 * This software is the proprietary information of PolySpot, S.A.S. Use is subject to license terms.
 */
package oqube.bytes;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import oqube.bytes.attributes.AbstractAnnotationAttribute;
import oqube.bytes.struct.AttributeFileInfo;
import oqube.bytes.struct.MethodFileInfo;

import org.junit.Test;

/**
 * 
 */
public class ClassFileTest {

  @Test
  public void extractJUnit4TestCasesFromClassFile() throws Exception {
    ClassFile classFile = ClassFile.makeClassFile(getClass().getResourceAsStream("/sample.cls"));
    Collection<MethodFileInfo> allMethods = classFile.getAllMethods();
    List<String> tests = new ArrayList<String>();
    for (MethodFileInfo m : allMethods) {
      for (Object attr : m.getAttributes()) {
        if (((AttributeFileInfo) attr).getName().equals("RuntimeVisibleAnnotations")
          && ((AbstractAnnotationAttribute) attr).getValue("Lorg/junit/Test;") != null) {
          tests.add(m.getName());
        }
      }
    }
    System.err.println(tests);
    assertTrue("expected at least on test method to be found", !tests.isEmpty());
  }
}
