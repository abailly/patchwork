/*______________________________________________________________________________
 * 
 * Copyright (C) 2006 Arnaud Bailly / OQube 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 * email: contact@oqube.com
 * creation: Thu Sep 21 2006
 */
package oqube.patchwork.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import oqube.patchwork.TestRunner;
import oqube.patchwork.report.coverage.XHTMLFormatter;
import oqube.patchwork.test.JUnitRunner;

import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A mojo for running test with patchwork coverage. This simple mojo is wrapper
 * around {@link TestRunner} class that runs from inside a maven project. It
 * does the following:
 * <ul>
 * <li>collect all test cases (at present, JUnit 3.8.1),</li>
 * <li>instrument tested code classes, either online or offline,</li>
 * <li>generate a coverage report,</li>
 * <li>optionnally generate coverage information for source files.</li>
 * </ul>
 * 
 * @author abailly@oqube.com
 * @version $Id: PatchworkMojo.java 3 2007-03-13 21:43:21Z arnaud.oqube $
 * @goal test
 * @phase test
 */
public class PatchworkMojo extends AbstractMojo {

  /**
   * The base directory of the project being tested. This can be obtained in
   * your unit test by System.getProperty("basedir").
   * 
   * @parameter expression="${basedir}"
   * @required
   */
  private File basedir;

  /**
   * The directory containing generated classes of the project being tested.
   * 
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   */
  private File classesDirectory;

  /**
   * The directory containing the sources of the project.
   * 
   * @parameter expression="${project.build.sourceDirectory}
   */
  private File sourceDirectory;

  /**
   * The directory containing generated test classes of the project being
   * tested.
   * 
   * @parameter expression="${project.build.testOutputDirectory}"
   * @required
   */
  private File testClassesDirectory;

  /**
   * The classpath elements of the project being tested.
   * 
   * @parameter expression="${project.testClasspathElements}"
   * @required
   * @readonly
   */
  private List<String> classpathElements;

  /**
   * Base directory where all reports are written to.
   * 
   * @parameter expression="${project.build.directory}/patchwork"
   */
  private File reportsDirectory;

  /**
   * List of patterns (separated by commas) used to specify the tests that
   * should be included in testing. When not specified and when the
   * <code>test</code> parameter is not specified, the default includes will
   * be
   * <code>**&#47;Test*.java   **&#47;*Test.java   **&#47;*TestCase.java</code>
   * 
   * @parameter expression=".*Test.class|Test.*.class|.*TestCase.class"
   */
  private String include;

  /**
   * List of patterns (separated by commas) used to specify the tests that
   * should be excluded in testing. When not specified and when the
   * <code>test</code> parameter is not specified, the default excludes will
   * be
   * <code>**&#47;Abstract*Test.java  **&#47;Abstract*TestCase.java **&#47;*$*</code>
   * 
   * @parameter expression="^$"
   */
  private String exclude;

  /**
   * Coverage information output format. May be one of 'text' or 'xhtml'. By
   * default, this is text.
   * 
   * @parameter expression="text"
   */
  private String format;

  /**
   * Sets the low threshold (percent) for patchwork report. All coverage
   * measures that fall at or below this threshold will be highlighted in red in
   * the report. This is a percentage between 0.0 and 100.0.
   * 
   * @parameter expression="50.0"
   */
  private double low;

  /**
   * Sets the high threshold (percent) for patchwork report. All coverage
   * measures that fall at or above this threshold will be highlighted in green
   * in the report. This is a percentage between 0.0 and 100.0.
   * 
   * @parameter expression="90.0"
   */
  private double high;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    // create classloader from test class path
    ClassLoader cl;
    try {
      cl = makeTestClassLoader();
    } catch (Exception e) {
      throw new MojoExecutionException("Error executing patchwork mojo", e);
    }
    TestRunner junit = new JUnitRunner(cl);
    junit.setCoveredClasspath(Collections.singletonList(classesDirectory));
    junit.setTestClasses(Collections.singletonList(testClassesDirectory));
    junit.setLog(new LogWrapper(getLog()));
    junit.setIncludePattern(Pattern.compile(include));
    junit.setExcludePattern(Pattern.compile(exclude));
    junit.setSourcepath(Collections.singletonList(sourceDirectory));
    junit.setReportOutputDir(reportsDirectory);
    junit.setLow(low);
    junit.setHigh(high);
    if ("xhtml".equals(format))
      junit.setReportFormater(new XHTMLFormatter());
    // run
    junit.run();
  }

  private ClassLoader makeTestClassLoader() throws MojoExecutionException,
      ArtifactResolutionException, ArtifactNotFoundException,
      MalformedURLException {
    List<URL> urls = new ArrayList<URL>();
    for (String s : classpathElements) {
      try {
        getLog().debug("adding " + s + " to classpath for Patchwork");
        urls.add(new URL("file:" + s));
      } catch (MalformedURLException e) {
        e.printStackTrace();
        getLog().error("Bad test classpath entry:" + s, e);
      }
    }
    return new URLClassLoader(urls.toArray(new URL[urls.size()]), this
        .getClass().getClassLoader());
  }

}
