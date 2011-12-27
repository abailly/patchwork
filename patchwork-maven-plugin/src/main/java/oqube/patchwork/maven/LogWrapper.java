/*______________________________________________________________________________
 * 
 * Copyright 2006 OQube / Arnaud Bailly 
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
 * Created on 6 juin 2006
 *
 */

package oqube.patchwork.maven;

import org.apache.commons.logging.Log;

/**
 * A class that wraps maven style logging object in a commons-logging style
 * logging object.
 * 
 * @author nono
 * 
 */
public class LogWrapper implements Log {

  private org.apache.maven.plugin.logging.Log log;

  public LogWrapper(org.apache.maven.plugin.logging.Log log) {
    this.log = log;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isDebugEnabled()
   */
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isErrorEnabled()
   */
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isFatalEnabled()
   */
  public boolean isFatalEnabled() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isInfoEnabled()
   */
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isTraceEnabled()
   */
  public boolean isTraceEnabled() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#isWarnEnabled()
   */
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#trace(java.lang.Object)
   */
  public void trace(Object arg0) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#trace(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void trace(Object arg0, Throwable arg1) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#debug(java.lang.Object)
   */
  public void debug(Object arg0) {
    log.debug(arg0.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#debug(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void debug(Object arg0, Throwable arg1) {
    log.debug(arg0.toString(), arg1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#info(java.lang.Object)
   */
  public void info(Object arg0) {
    log.info(arg0.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#info(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void info(Object arg0, Throwable arg1) {
    log.info(arg0.toString(), arg1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#warn(java.lang.Object)
   */
  public void warn(Object arg0) {
    log.warn(arg0.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#warn(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void warn(Object arg0, Throwable arg1) {
    log.warn(arg0.toString(), arg1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#error(java.lang.Object)
   */
  public void error(Object arg0) {
    log.error(arg0.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#error(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void error(Object arg0, Throwable arg1) {
    log.error(arg0.toString(), arg1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
   */
  public void fatal(Object arg0) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.logging.Log#fatal(java.lang.Object,
   *      java.lang.Throwable)
   */
  public void fatal(Object arg0, Throwable arg1) {
  }

}
