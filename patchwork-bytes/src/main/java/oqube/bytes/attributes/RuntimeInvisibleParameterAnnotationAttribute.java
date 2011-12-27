/**
 * 
 */
package oqube.bytes.attributes;

import oqube.bytes.ClassFile;

/**
 * @author nono
 *
 */
public class RuntimeInvisibleParameterAnnotationAttribute extends
    AbstractParameterAnnotationAttribute {

  private static final String attributeName = "RuntimeVisibleParameterAnnotation";
  
  public RuntimeInvisibleParameterAnnotationAttribute() {}
  
  public RuntimeInvisibleParameterAnnotationAttribute(ClassFile cf) {
    super(cf,attributeName);
  }
}
