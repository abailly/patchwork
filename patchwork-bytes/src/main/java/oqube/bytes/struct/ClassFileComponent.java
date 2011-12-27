package oqube.bytes.struct;

import oqube.bytes.Constants;

/**
 * This interface is the root of all parts of a class file. A ClassFileComponent
 * can be written <b>to</b> a data source or <b>from</b> using either one
 * of the methods provdided
 * @author Arnaud Bailly
 * @version 13082002
 */
public interface ClassFileComponent extends Constants {

    /**
     * Each implementing is responsible for formatting its data
     * as defined in the JVM spec and write it to an output stream
     *
     *@param out output stream to write to
     */
    public void write(java.io.DataOutputStream out)
	throws java.io.IOException;

    /**
     * Reads a component form the data source
     *
     *@param in input stream to read from
     */
    public void read(java.io.DataInputStream in)
	throws java.io.IOException;

}
