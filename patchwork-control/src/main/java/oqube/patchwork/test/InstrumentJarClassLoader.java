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
 * Created on Jan 3, 2006
 *
 */
package oqube.patchwork.test;

import java.util.jar.JarFile;

import oqube.patchwork.instrument.InstrumentClass;
import fr.lifl.utils.JarClassLoader;

/**
 * A class loader for instrumenting the content 
 * of a jar file.
 * <p>
 * At instance creation, the content of the jar file is scanned and 
 * all its classes are instrumented, using the {@link oqube.patchwork.instrument.InstrumentClass} 
 * instance given.
 * </p>
 * 
 * @author nono
 * @version $Id: InstrumentJarClassLoader.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class InstrumentJarClassLoader extends JarClassLoader {

    public InstrumentJarClassLoader(InstrumentClass instrument,ClassLoader parent, JarFile file) {
        super(parent,file);
    }

}
