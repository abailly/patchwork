/**
 * 
 */
package oqube.bytes.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.struct.AttributeFileInfo;

/**
 * Base class for visible and invisible runtime annotations.
 * 
 * @author nono
 * 
 */
public abstract class AbstractAnnotationAttribute extends AttributeFileInfo {

    /*
     * annotations' list
     */
    private AnnotationValueSet values;

    public AbstractAnnotationAttribute(ClassFile cf, String name) {
        super(cf, name);
    }

    public AbstractAnnotationAttribute() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see oqube.bytes.struct.AttributeFileInfo#write(java.io.DataOutputStream)
     */
    @Override
    public void write(DataOutputStream dos) throws IOException {
        dos.writeShort(nameIndex);
        dos.writeInt(getLength());
        values.write(dos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see oqube.bytes.struct.ClassFileComponent#read(java.io.DataInputStream)
     */
    public void read(DataInputStream in) throws IOException {
        values = new AnnotationValueSet(classFile);
        values.read(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see oqube.bytes.struct.AttributeFileInfo#getLength()
     */
    @Override
    public int getLength() {
        return values.getLength();
    }

    /**
     * Returns the map of values for the given annotation.
     * 
     * @param ann
     *            the fully qualified name of the annotation class to retrieve
     *            value of.
     * @return an instance of map from annotations' attributes names to values.
     *         If this attribute does not contains the given annotation, it
     *         returns null.
     */
    public Map<String, Object> getValue(String ann) {
        for (AnnotationValue val : values.annotations) {
            String n = val.getAnnotationName();
            if (n.equals(ann)) return val.getElementsMap();
        }
        return null;
    }

    public Map<String, Map<String, Object>> getValues() {
        HashMap<String, Map<String, Object>> ret = new HashMap<String, Map<String, Object>>();
        for (AnnotationValue val : values.annotations) {
            String n = val.getAnnotationName();
            ret.put(n, val.getElementsMap());
        }
        return ret;
    }
}
