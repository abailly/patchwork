/*______________________________________________________________________________
 * 
 * Copyright 2006 Arnaud Bailly - NORSYS/LIFL
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
 * Created on Jan 5, 2006
 *
 */
package oqube.bytes.events;

import java.io.IOException;
import java.io.OutputStream;

public class BytecodeOutputStream extends OutputStream implements
        ClassFileIOListener {

    private OutputStream os;

    private int count;

    private EventRecorder recorder = new EventRecorder();

    public BytecodeOutputStream(OutputStream bis) {
        this.os = bis;
        this.count = 0;
    }

    public void notify(ClassFileIOEvent event) {
        if (event.getDirection() == ClassFileIOEvent.WRITE)
            recorder.notify(event, count);
    }

    public void write(int b) throws IOException {
        os.write(b);
        count++;
    }

    /**
     * @return Returns the recorder.
     */
    public EventRecorder getRecorder() {
        return recorder;
    }

    /**
     * @param recorder The recorder to set.
     */
    public void setRecorder(EventRecorder recorder) {
        this.recorder = recorder;
    }

}