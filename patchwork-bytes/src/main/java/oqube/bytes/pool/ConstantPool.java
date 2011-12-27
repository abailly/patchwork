package oqube.bytes.pool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.Constants;
import oqube.bytes.struct.ClassFileComponent;

/** 
 * The constant table class implements the constant pool found in the
 * class file format. There are various functions to manipulate this table
 * through the various data types in it
 */
public class ConstantPool implements Constants, ClassFileComponent, Cloneable {

    // the set of constants
    private List tbl = new ArrayList();

    // map of strings to UTF
    private java.util.Map utfData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map stringData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map classData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map nameAndTypeData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map methodRefData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map ifaceMethodData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map fieldRefData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map intData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map floatData = new java.util.HashMap();

    // map of strings to String constants
    private java.util.Map doubleData = new java.util.HashMap();

    private java.util.Map longData = new java.util.HashMap();

    public ConstantPool() {
        // reinitialize  *Data classes
    }

    private void init() {
        tbl = new ArrayList();
        utfData = new HashMap();
        stringData = new HashMap();
        classData = new HashMap();
        nameAndTypeData = new HashMap();
        methodRefData = new HashMap();
        ifaceMethodData = new HashMap();
        fieldRefData = new HashMap();
        intData = new HashMap();
        floatData = new HashMap();
        doubleData = new HashMap();
        longData = new HashMap();
    }

    /**
     * returns the current index in this table and adds to it an offset
     * as a parameter
     *
     * @param j offset to the index
     * @return the old value of the index
     */
    short inc() {
        return (short) (tbl.size() + 1);
    }

    /** 
     * returns the running index without modifying it
     *
     * @return the value of the index
     */
    int index() {
        return tbl.size();
    }

    /**
     * Add a new constant to the table
     */
    void add(UTFData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        utfData.put(u.toString(), u);
    }

    /* get a UTFData constant from a string */
    UTFData getUTFData(String s) {
        return (UTFData) utfData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(StringData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        stringData.put(u.toString(), u);
    }

    /* get a StringData constant from a string */
    StringData getStringData(String s) {
        return (StringData) stringData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(ClassData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        classData.put(u.toString(), u);
    }

    /* get a classData constant from a string */
    public ClassData getClassData(String s) {
        return (ClassData) classData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(NameAndTypeData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        nameAndTypeData.put(u.toString(), u);
    }

    /* get a NameAndTypeData constant from a string */
    NameAndTypeData getNameAndTypeData(String s) {
        return (NameAndTypeData) nameAndTypeData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(MethodRefData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        methodRefData.put(u.toString(), u);
    }

    /* get a MethodRefData constant from a string */
    MethodRefData getMethodRefData(String s) {
        return (MethodRefData) methodRefData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(InterfaceMethodData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        ifaceMethodData.put(u.toString(), u);
    }

    /* get a IfaceMethodData constant from a string */
    InterfaceMethodData getIfaceMethodData(String s) {
        return (InterfaceMethodData) ifaceMethodData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(FieldRefData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        fieldRefData.put(u.toString(), u);
    }

    /* get a FieldRefData constant from a string */
    FieldRefData getFieldRefData(String s) {
        return (FieldRefData) fieldRefData.get(s);
    }

    /**
     * Add a new constant to the table
     */
    void add(IntData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        intData.put(u.getInt(), u);
    }

    /* get a IntData constant from a string */
    IntData getIntData(Integer i) {
        return (IntData) intData.get(i);
    }

    /**
     * Add a new constant to the table
     */
    void add(FloatData u) {
        u.setIndex(inc());
        u.setConstantPool(this);

        tbl.add(u);
        floatData.put(u.getFloat(), u);
    }

    /* get a FloatData constant from a string */
    FloatData getFloatData(Float i) {
        return (FloatData) floatData.get(i);
    }

    /**
     * Add a new constant to the table
     */
    void add(DoubleData u) {
        u.setIndex(inc());
        u.setConstantPool(this);
        tbl.add(u);
        tbl.add(null);
        doubleData.put(u.getDouble(), u);
    }

    /* get a FloatData constant from a string */
    DoubleData getDoubleData(Double i) {
        return (DoubleData) doubleData.get(i);
    }

    /**
     * Returns a Constant pool entry given its index
     * 
     * @param index an short denoting an entry in the constant
     *  pool
     */
    public ConstantPoolEntry getEntry(short index) {
        try {
            return (ConstantPoolEntry) tbl.get(index - 1);
        } catch (IndexOutOfBoundsException e) {
            /*
             * maybe the requested data does not yet exists
             * if we are reading a file so better return null
             */
            if(index <= 0)
                throw new IllegalArgumentException("unable to find entry number "+index);
            return null;
        }
    }

    /**
     * Writes this Constant table to the passed OutputStream
     *
     * @param os the stream to write to 
     */
    public void write(java.io.DataOutputStream os) throws java.io.IOException {
        Iterator it;
        int n = tbl.size();
        int i = 0;
        if (n == 0)
            return;
        os.writeShort(n + 1);
        // creates an array containing the entries
        it = tbl.iterator();
        while (it.hasNext()) {
            ConstantPoolEntry data = ((ConstantPoolEntry) it.next());
            /*
             * data may be null for long and double constants
             */
            if(data != null)
                data.write(os);            
        }
    }

    /**
     * This method assumes we are at the beginning of the constant
     * pool area in a stream respecting .class files format
     * 
     * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
     */
    public void read(DataInputStream in) throws IOException {
        // read poolsize
        short poolSize = in.readShort();
        ConstantPoolEntry data = null;
        boolean dbl = false; /* for constants taking two slots */
        // read each entry
        for (int i = 1; i < poolSize; i++) {
            byte type = in.readByte();
            switch ((int) type) {
            case CONSTANT_UTF8:
                data = new UTFData();
                break;
            case CONSTANT_STRING:
                data = new StringData();
                break;
            case CONSTANT_CLASS:
                data = new ClassData();
                break;
            case CONSTANT_FIELD:
                data = new FieldRefData();
                break;
            case CONSTANT_FLOAT:
                data = new FloatData();
                break;
            case CONSTANT_INTEGER:
                data = new IntData();
                break;
            case CONSTANT_LONG:
                dbl = true;
                data = new LongData();
                break;
            case CONSTANT_INTERFACEMETHOD:
                data = new InterfaceMethodData();
                break;
            case CONSTANT_DOUBLE:
                dbl = true;
                data = new DoubleData();
                break;
            case CONSTANT_METHOD:
                data = new MethodRefData();
                break;
            case CONSTANT_NAMEANDTYPE:
                data = new NameAndTypeData();
                break;
            default:
                throw new IOException("Unknown constant type " + type
                        + " at index " + i + " in pool ");
            }
            data.read(in);
            data.setConstantPool(this);
            data.setIndex((short) (i));
            tbl.add(data);
            if (dbl) {
                i++;
                tbl.add(null);
            }
            dbl = false;
        }
        // insert constants in hashtables
        Iterator it = tbl.iterator();
        while (it.hasNext()) {
            ConstantPoolEntry constant = (ConstantPoolEntry) it.next();
            if (constant == null) /* null may occur when double constants are added to the pool */
                continue;
            //System.err.println("Constant "+constant.getIndex());
            constant.insertInPool(this);
        }
    }

    /**
     * Add a new constant to the table
     */
    void hash(UTFData u) {
        utfData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(StringData u) {
        stringData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(ClassData u) {
        classData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(NameAndTypeData u) {
        nameAndTypeData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(MethodRefData u) {
        methodRefData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(InterfaceMethodData u) {
        ifaceMethodData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(FieldRefData u) {
        fieldRefData.put(u.toString(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(IntData u) {
        intData.put(u.getInt(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(FloatData u) {
        floatData.put(u.getFloat(), u);
    }

    /**
     * InsertInPool a new constant to the table
     */
    void hash(DoubleData u) {
        doubleData.put(u.getDouble(), u);
    }

    void hash(LongData u) {
        longData.put(u.getLong(), u);
    }

    /**
     * 
     */
    public Collection getAllClassData() {
        return classData.keySet();
    }

    /**
     * @param data
     */
    void add(LongData data) {
        data.setIndex(inc());
        data.setConstantPool(this);

        tbl.add(data);
        longData.put(data.getLong(), data);
    }

    /* get a FloatData constant from a string */
    LongData getLongData(Long i) {
        return (LongData) longData.get(i);
    }

    /*
     * to clone pool, we first write it to a bytearray
     *  then read it from the same array 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            ConstantPool pool = (ConstantPool) super.clone();
            pool.init();
            /* write all data */
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            write(dos);
            dos.flush();
            dos.close();
            /* read all data */
            ByteArrayInputStream bis = new ByteArrayInputStream(bos
                    .toByteArray());
            DataInputStream dis = new DataInputStream(bis);
            pool.read(dis);
            return pool;
        } catch (Exception e) {
            // SHOULD NEVER HAPPEN
            return null;
        }
    }

}
