/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.IOException;

import oqube.bytes.struct.ClassFileComponent;

public abstract class AbstractAnnotation implements ClassFileComponent {

  /**
   * 
   * @return the length in bytes of this annotation element.
   */
  public abstract int getLength();
}