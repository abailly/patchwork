package oqube.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import oqube.bytes.events.BytecodeInputStream;
import oqube.bytes.events.ClassFileIOEvent;
import oqube.bytes.events.ClassFileIOListener;

import junit.framework.TestCase;

/**
 * Test loading of attributes.
 * 
 * @author nono
 * 
 */
public class AttributeTest extends TestCase {

  /*
   * try to load and save sample annotation class.
   */
  public void test01Annotations() throws IOException {
    // read sample
    InputStream is = getClass().getResourceAsStream("/annotation-sample");
    ClassFile cf = new ClassFile();
    BytecodeInputStream bis = new BytecodeInputStream(is);
    cf.addIOListener(bis);
    cf.read(new DataInputStream(bis));
    // write sample
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    cf.write(new DataOutputStream(bos));
    // compare
    is = getClass().getResourceAsStream("/annotation-sample");
    byte[] bytes = bos.toByteArray();
    int b;
    int i = 0;
    while ((b = is.read()) > -1) {
      assertEquals("different bytes at " + i, b, (int) bytes[i++] & 0xff);
    }
  }

  public void test02Generics() throws IOException {
    // read sample
    InputStream is = getClass().getResourceAsStream("/generic-sample");
    ClassFile cf = new ClassFile();
    cf.read(new DataInputStream(is));
    // write sample
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    cf.write(new DataOutputStream(bos));
    // compare
    is = getClass().getResourceAsStream("/generic-sample");
    byte[] bytes = bos.toByteArray();
    int b;
    int i = 0;
    while ((b = is.read()) > -1) {
      assertEquals("different bytes at " + i, b, (int) bytes[i] & 0xff);
      i++;
    }
  }
}
