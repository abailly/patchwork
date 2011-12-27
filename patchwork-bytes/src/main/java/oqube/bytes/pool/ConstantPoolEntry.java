package oqube.bytes.pool;

import oqube.bytes.struct.ClassFileComponent;

/**
 * This class represents an entry in the Constant pool of a class file It is
 * specialized by various sub-classes, one for each of the constants allowed in
 * a class file
 */
public abstract class ConstantPoolEntry implements ClassFileComponent {

  // the index of this entry in the constant pool
  // initially set to -1 to ensure we can distinguish between
  // newly created entries and entries pertaining to a pool
  private short index = -1;

  // the constant pool this constant pertains to
  private ConstantPool constantPool;

  /**
   * Returns the index.
   * 
   * @return int
   */
  public short getIndex() {
    return index;
  }

  /**
   * Sets the index.
   * 
   * @param index
   *          The index to set
   */
  void setIndex(short index) {
    this.index = index;
  }

  /**
   * Returns the constantPool.
   * 
   * @return ConstantPool
   */
  public ConstantPool getConstantPool() {
    return constantPool;
  }

  /**
   * Sets the constantPool.
   * 
   * @param constantPool
   *          The constantPool to set
   */
  void setConstantPool(ConstantPool constantPool) {
    this.constantPool = constantPool;
  }

  /**
   * Call back method for ConstantPool to properly populate hash tables after
   * reading a class file. This method is implemeted by subclasses who calls
   * pool.insertInPool(this), thus inserting the constant in the right hashtable
   * 
   * @param pool
   *          ConstantPool to insert this constant in
   */
  public abstract void insertInPool(ConstantPool pool);

}
