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
package oqube.bytes.events;

import oqube.bytes.ClassFile;

/**
 * A class representing an I/O event for classfiles reading/writing.
 * <p>
 * This class offers data about the event:
 * <ul>
 * <li>The originator of the event,</li>
 * <li>The direction (read or write),</li>
 * <li>The position in event (start, end or atomic),</li>
 * <li>The data identifying the event. Note that start and end events 
 * have the same data associated (in the sense of equals()).</li>
 * </ul>
 * </p>
 * @author nono
 * @version $Id: ClassFileIOEvent.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class ClassFileIOEvent {

    /**
     * Constant denoting a read event.
     */
    public static final int READ = 0;
    
    /**
     * Constant denoting a write event.
     */
    public static final int WRITE = 1;
    
    
    /**
     * Constant denoting start of an event.
     */
    public static final int START = 0;
    
    /**
     * Constant denoting end of an event.
     */
    public static final int END = 1;

    /**
     * Constant denoting an atomic event.
     */
    public static final int ATOMIC = 2;
    
    private ClassFile classFile;
    
    private int direction;
    
    private int timing;
    
    private Object data;

    /**
     * General constructor for an event.
     * 
     * @param cf
     * @param dir
     * @param time
     * @param data
     */
    public ClassFileIOEvent(ClassFile cf,int dir, int time, Object data) {
        this.classFile = cf;
        this.direction = dir;
        this.timing = time;
        this.data = data;
    }
    
    /**
     * @return Returns the classFile.
     */
    public ClassFile getClassFile() {
        return classFile;
    }

    /**
     * @param classFile The classFile to set.
     */
    public void setClassFile(ClassFile classFile) {
        this.classFile = classFile;
    }

    /**
     * @return Returns the data.
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data The data to set.
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * @return Returns the direction.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @param direction The direction to set.
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * @return Returns the timing.
     */
    public int getTiming() {
        return timing;
    }

    /**
     * @param timing The timing to set.
     */
    public void setTiming(int timing) {
        this.timing = timing;
    }
}
