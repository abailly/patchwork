/**
 * 
 */
package oqube.bytes.attributes;

import oqube.bytes.ClassFile;

/**
 * Represents runtime visible annotations for a class,method or field.
 * 
 * @author nono
 * 
 */
public class RuntimeVisibleAnnotationsAttribute extends
    AbstractAnnotationAttribute {

  private static final String attributeName = "RuntimeVisibleAnnotations";

  public RuntimeVisibleAnnotationsAttribute() {
  }

  public RuntimeVisibleAnnotationsAttribute(ClassFile cf) {
    super(cf, attributeName);
  }
}
