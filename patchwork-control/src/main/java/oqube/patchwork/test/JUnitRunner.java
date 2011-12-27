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
 * Created on Jan 9, 2006
 *
 */
package oqube.patchwork.test;

import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import oqube.patchwork.TestRunner;

/**
 * A class to run JUnit tests.
 * <p>
 * This implementation of TestRunner may run JUnit test suites over
 * instrumented classes.
 * 
 * </p>
 * @author nono
 * @version $Id: JUnitRunner.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
public class JUnitRunner extends TestRunner {

    public JUnitRunner(ClassLoader root) {
        super(root);
    }

    public JUnitRunner() {
        super(Thread.currentThread().getContextClassLoader());
    };

    class RawTestListener implements TestListener {

        public void addError(Test arg0, Throwable arg1) {
            log
                    .info("[ERROR  ] " + arg0.toString() + " : "
                            + arg1.getMessage());
            arg1.printStackTrace();
        }

        public void addFailure(Test arg0, AssertionFailedError arg1) {
            log
                    .info("[FAILURE] " + arg0.toString() + " : "
                            + arg1.getMessage());
        }

        public void endTest(Test arg0) {
            log.info("[Ending] " + arg0.toString());
        }

        public void startTest(Test arg0) {
            log.info("[Starting] " + arg0.toString());
        }

    }

    public Object runSuite(List l) {
        TestResult tr = new TestResult();
        tr.addListener(new RawTestListener());
        for (Iterator i = l.iterator(); i.hasNext();) {
            String cn = (String) i.next();
            try {
                Class cls = testLoader.loadClass(cn);
                TestSuite ts = new TestSuite(cls);
                ts.run(tr);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                log.error("Cannot load test suite " + cn);
            }
        }
        return tr;
    }

}
