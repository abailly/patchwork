/*______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Dec 29, 2005
 *
 */
package oqube.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oqube.bytes.events.BytecodeInputStream;
import oqube.bytes.events.BytecodeOutputStream;
import oqube.bytes.events.EventRecorder;

import fr.lifl.utils.Pipe;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Test I/O of ClassFile objects.
 * 
 * @author nono
 * @version $Id: IOTest.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class IOTest extends TestCase {

    public static final long lconst = 12L;

    public static final double dconst = 12.0;

    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * This test read a bytecode stream into a ClassFile 
     * object then writes the classFile object to a bytearray 
     * stream.
     * The two streams are compared for equality.
     */
    public void testIOClass() throws IOException {
        /* create classfile object */
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "oqube/bytes/pool/ClassData.class");
        /* store bytes into an array */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            new Pipe(bos, is).pump();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bos.close();
        byte[] rb = bos.toByteArray();
        /* create class from input stream */
        ClassFile cf = new ClassFile();
        BytecodeInputStream bis = new BytecodeInputStream(
                new ByteArrayInputStream(rb));
        cf.addIOListener(bis);
        cf.read(new DataInputStream(bis));
        /* read recorder */
        EventRecorder rr = bis.getRecorder();
        /* write class */
        bos = new ByteArrayOutputStream();
        BytecodeOutputStream bbos = new BytecodeOutputStream(bos);
        cf.addIOListener(bbos);
        cf.write(new DataOutputStream(bbos));
        /* compare bytes */
        byte[] wb = bos.toByteArray();
        assertEquals(rb,wb);
    }

    /*
     * Same test with an interface
     */
    public void testIOInterface() throws IOException {
        /* create classfile object */
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "oqube/bytes/struct/ClassFileComponent.class");
        /* store bytes into an array */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            new Pipe(bos, is).pump();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bos.close();
        byte[] rb = bos.toByteArray();
        /* create class from input stream */
        ClassFile cf = new ClassFile();
        BytecodeInputStream bis = new BytecodeInputStream(
                new ByteArrayInputStream(rb));
        cf.addIOListener(bis);
        cf.read(new DataInputStream(bis));
        /* read recorder */
        EventRecorder rr = bis.getRecorder();
        /* write class */
        bos = new ByteArrayOutputStream();
        BytecodeOutputStream bbos = new BytecodeOutputStream(bos);
        cf.addIOListener(bbos);
        cf.write(new DataOutputStream(bbos));
        byte[] wb = bos.toByteArray();
        /* compare bytes */
        assertEquals(rb,wb);
    }

    
    /**
     * Check discrepancies between lists.
     * 
     * @param rr
     * @param wr
     */
    private void assertLists(List events, List events2) {
        List discrepancies = new ArrayList();
        Iterator i = events.iterator();
        Iterator j = events2.iterator();
        int k = 0;
        while (true) {
            
            if(!(i.hasNext() & j.hasNext()))
                break;
            Object o1 = i.next();
            Object o2 = j.next();
            if(!(o1.equals(o2)))
                discrepancies.add("At "+k+": expected "+o1+", found "+o2);
            k++;
        }
        System.err.println(discrepancies);
        if(!discrepancies.isEmpty())
            throw new AssertionFailedError("Found discrepancies: "+discrepancies);
        if (i.hasNext() ^ j.hasNext()) {
            throw new AssertionFailedError(
                    "List do not have the same sizes: expected "
                            + events.size() + " found " + events2.size());
        } 
    }

    private void assertEquals(byte[] exp, byte[] real) {
        if (exp == null)
            if (real == null)
                return;
            else
                throw new AssertionFailedError("Expected null array but found"
                        + real);
        if (real == null)
            if (exp == null)
                return;
            else
                throw new AssertionFailedError("Expected " + exp
                        + " but found null array");
        if (exp.length != real.length)
            throw new AssertionFailedError("Incompatible length: Expected "
                    + exp.length + " but found " + real.length);
        int l = exp.length;
        for (int i = 0; i < l; i++)
            if (exp[i] != real[i])
                throw new AssertionFailedError("Discrepancy found at byte " + i
                        + " : expected " + exp[i] + ", found " + real[i]);

    }
}
