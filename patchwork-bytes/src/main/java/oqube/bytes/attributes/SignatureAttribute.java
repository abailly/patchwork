/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * Signature attribute for generic types informations in classes, fields and
 * methods signatures.
 * 
 * @author nono
 * 
 */
public class SignatureAttribute extends AttributeFileInfo implements Constants {

  /**
   * String constant designating this attribute in the constant pool
   */
  public static final String attributeName = "Signature";

  /*
   * utf-8 constant for signature.
   */
  private short signatureIndex;

  public SignatureAttribute(ClassFile cf) {
    super(cf, attributeName);
  }

  public SignatureAttribute() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.AttributeFileInfo#write(java.io.DataOutputStream)
   */
  @Override
  public void write(DataOutputStream dos) throws IOException {
    dos.writeShort(nameIndex);
    dos.writeInt(getLength());
    dos.writeShort(signatureIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.ClassFileComponent#read(java.io.DataInputStream)
   */
  public void read(DataInputStream in) throws IOException {
    signatureIndex = in.readShort();
  }

  /**
   * @return Returns the signature.
   */
  public short getSignature() {
    return signatureIndex;
  }

  /**
   * @param signature
   *          The signature to set.
   */
  public void setSignature(short signature) {
    this.signatureIndex = signature;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oqube.bytes.struct.AttributeFileInfo#getLength()
   */
  @Override
  public int getLength() {
    return 2;
  }

}
