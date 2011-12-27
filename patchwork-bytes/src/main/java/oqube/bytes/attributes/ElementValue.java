/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import oqube.bytes.ClassFile;
import oqube.bytes.Constants;
import oqube.bytes.pool.ClassData;
import oqube.bytes.pool.ConstantData;
import oqube.bytes.pool.IntData;
import oqube.bytes.pool.StringData;

/**
 * Class for annotation elements value. A union type representing value of an
 * annotation's elements.
 * 
 * @author nono
 * 
 */
public class ElementValue {

    /*
     * element name. UTF-8 constant
     */
    private short              nameIndex;

    /*
     * either a primitive type or special tags.
     */
    private byte               tag;

    /*
     * value discriminated by tag.
     */
    private AbstractAnnotation value;

    /*
     * containing class file.
     */
    private ClassFile          classFile;

    /*
     * for primitive, 's', 'c' tags
     */
    class PoolIndex extends AbstractAnnotation {
        short index;

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(index);
        }

        public void read(DataInputStream in) throws IOException {
            index = in.readShort();
        }

        @Override
        public int getLength() {
            return 2;
        }

    }

    class EnumValue extends AbstractAnnotation {

        /*
         * type reference UTF-8 constant
         */
        short classNameIndex;

        /*
         * constant field name reference UTF-8 constant
         */
        short enumNameIndex;

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(classNameIndex);
            dos.writeShort(enumNameIndex);
        }

        public void read(DataInputStream in) throws IOException {
            classNameIndex = in.readShort();
            enumNameIndex = in.readShort();
        }

        @Override
        public int getLength() {
            return 4;
        }

    }

    class ArrayValue extends AbstractAnnotation {

        /*
         * number of values
         */
        short              num;

        /*
         * list of values
         */
        List<ElementValue> values = new ArrayList<ElementValue>();

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(num);
            for (ElementValue val : values)
                val.write(dos);
        }

        public void read(DataInputStream in) throws IOException {
            num = in.readShort();
            values = new ArrayList<ElementValue>();
            for (int i = 0; i < num; i++) {
                ElementValue val = new ElementValue(classFile);
                val.read(in);
                values.add(val);
            }
        }

        @Override
        public int getLength() {
            int sum = 2;
            for (ElementValue val : values)
                sum += val.getLength();
            return sum;
        }

    }

    public ElementValue(ClassFile cf) {
        this.classFile = cf;
    }

    public ElementValue() {
    }

    public void write(java.io.DataOutputStream dos) throws java.io.IOException {
        dos.write(tag);
        value.write(dos);
    }

    /**
     * @see fr.norsys.klass.ClassFileComponent#read(DataInputStream)
     */
    public void read(DataInputStream in) throws IOException {
        tag = in.readByte();
        switch ((char) tag & 0xff) {
        case Constants.SIGC_BYTE:
        case Constants.SIGC_CHAR:
        case Constants.SIGC_DOUBLE:
        case Constants.SIGC_FLOAT:
        case Constants.SIGC_INT:
        case Constants.SIGC_BOOLEAN:
        case Constants.SIGC_LONG:
        case Constants.SIGC_SHORT:
        case Constants.SIGC_STRING:
        case Constants.SIGC_CLASS_REF:
            value = new PoolIndex();
            value.read(in);
            break;
        case Constants.SIGC_ENUM:
            value = new EnumValue();
            value.read(in);
            break;
        case Constants.SIGC_ARRAY:
            value = new ArrayValue();
            value.read(in);
            break;
        case Constants.SIGC_ANNOTATION:
            value = new AnnotationValue(classFile);
            value.read(in);
            break;
        default:
            throw new IOException("Unexpected discriminator constant " + tag);
        }
    }

    public int getLength() {
        return 1 + value.getLength();
    }

    /**
     * Returns the representation of this element value as an object.
     * 
     * @return an object representing the value of this element.
     */
    public Object getValue() {
        switch ((char) tag & 0xff) {
        case Constants.SIGC_BYTE:
            IntData data = (IntData) classFile.getConstantPool()
                                              .getEntry(((PoolIndex) value).index);
            return new Byte(data.getInt().byteValue());
        case Constants.SIGC_CHAR:
            data = (IntData) classFile.getConstantPool()
                                      .getEntry(((PoolIndex) value).index);
            return new Character((char) data.getInt().shortValue());
        case Constants.SIGC_SHORT:
            data = (IntData) classFile.getConstantPool()
                                      .getEntry(((PoolIndex) value).index);
            return new Short(data.getInt().shortValue());
        case Constants.SIGC_BOOLEAN:
            data = (IntData) classFile.getConstantPool()
                                      .getEntry(((PoolIndex) value).index);
            if (data.getInt() == 0)
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        case Constants.SIGC_INT:
        case Constants.SIGC_DOUBLE:
        case Constants.SIGC_FLOAT:
        case Constants.SIGC_LONG:
        case Constants.SIGC_STRING:
        case Constants.SIGC_CLASS_REF:
            ConstantData data1 = (ConstantData) classFile.getConstantPool()
                                                         .getEntry(((PoolIndex) value).index);
            return data1.getValue();
        case Constants.SIGC_ENUM:
            ClassData cls = (ClassData) classFile.getConstantPool()
                                                 .getEntry(((EnumValue) value).classNameIndex);
            StringData str = (StringData) classFile.getConstantPool()
                                                   .getEntry(((EnumValue) value).enumNameIndex);
            return Enum.valueOf((Class) cls.getValue(), (String) str.getValue());
        case Constants.SIGC_ARRAY:
            List<Object> ret = new ArrayList<Object>();
            for (ElementValue val : ((ArrayValue) value).values)
                ret.add(val.getValue());
            return ret.toArray();
        case Constants.SIGC_ANNOTATION:
        default:
            // TODO
            return null;
        }

    }
}
