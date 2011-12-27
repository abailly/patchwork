/**
 * 
 */
package oqube.bytes.attributes;

import oqube.bytes.ClassFile;

/**
 * @author nono
 *
 */
public class RuntimeVisibleParameterAnnotationAttribute extends
    AbstractParameterAnnotationAttribute {

  private static final String attributeName = "RuntimeVisibleParameterAnnotation";
  
  public RuntimeVisibleParameterAnnotationAttribute() {}
  
  public RuntimeVisibleParameterAnnotationAttribute(ClassFile cf) {
    super(cf,attributeName);
  }
}
