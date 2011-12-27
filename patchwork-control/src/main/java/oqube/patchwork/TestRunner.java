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
package oqube.patchwork;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestResult;
import oqube.bytes.ClassFile;
import oqube.bytes.loading.ByteArrayClassLoader;
import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraphBuilder;
import oqube.patchwork.instrument.CoverageBackend;
import oqube.patchwork.instrument.CoverageGenerator;
import oqube.patchwork.instrument.InstrumentClass;
import oqube.patchwork.report.Coverage;
import oqube.patchwork.report.CoverageInfo;
import oqube.patchwork.report.OnlineBackend;
import oqube.patchwork.report.Reporter;
import oqube.patchwork.report.SimpleReporter;
import oqube.patchwork.report.coverage.AllEdgesObjective;
import oqube.patchwork.report.coverage.CoverageObjective;
import oqube.patchwork.report.coverage.OutputFormatter;
import oqube.patchwork.report.coverage.PackageTreeCoverageBuilder;
import oqube.patchwork.report.coverage.SimpleTextOutput;
import oqube.patchwork.report.coverage.XHTMLFormatter;
import oqube.patchwork.report.source.SourceMapper;
import oqube.patchwork.report.source.SourceToURL;
import oqube.patchwork.test.JUnitRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.lifl.utils.CommandLine;
import fr.lifl.utils.CompoundClassLoader;
import fr.lifl.utils.DirClassLoader;
import fr.lifl.utils.JarClassLoader;

/**
 * Class for launching coverage enhanced tests.
 * <p>
 * This class is the main UI object to the patchwork system. It encapsulates all
 * the necessary parameters to <em>instrument</em> a set of
 * <code>.class</code> files, run some JUnit test cases that make use of these
 * classes and produce a binary coverage report from the test execution.
 * </p>
 * <p>
 * The result of TestRunner execution can then be later analyzed using some
 * Coverage criterion through CoverageReporter.
 * </p>
 * 
 * @author nono
 * @version $Id: TestRunner.java 10 2007-05-02 16:37:54Z arnaud.oqube $
 */
public abstract class TestRunner implements Runnable {

  /*
   * the loader containing the instrumented classes.
   */
  private ByteArrayClassLoader instLoader;

  /*
   * the loader for test classes
   */
  protected CompoundClassLoader testLoader;

  /**
   * the instrumented class definitions are stored here
   */
  private InstrumentClass instrument = new InstrumentClass();

  /*
   * the coverage generator
   */
  private CoverageGenerator generator = new CoverageGenerator();

  /*
   * list of classpath entries to cover
   */
  private List<File> coveredClasspath = new ArrayList<File>();

  /*
   * list of classpath entries to look for test classes
   */
  private List<File> testClasses = new ArrayList<File>();
  
  /*
   * pattern for test files to match
   */
  private Pattern includePattern = Pattern.compile(".*Test.class");

  /*
   * pattern for test files to exclude
   */
  private Pattern excludePattern = Pattern.compile("^$");

  /*
   * log channel
   */
  protected Log log = LogFactory.getLog(getClass());

  /*
   * output directory for generated classes
   */
  private File outputDir = null;

  /*
   * the reporter instance
   */
  private Reporter reporter = new SimpleReporter();

  /*
   * the backend used for reporting data
   */
  private CoverageBackend backend;

  /*
   * Graph builder instance.
   */
  private ControlGraphBuilder graphBuilder;

  /*
   * the formater used for reporting.
   */
  private OutputFormatter reportFormater = new SimpleTextOutput();

  /*
   * the objective class for constructing test objective.
   */
  private List<Class> coverageObjectives = Collections.<Class>singletonList(AllEdgesObjective.class);

  /*
   * list of classpath style entries where sources can be found.
   */
  private List<File> sourcepath = new ArrayList<File>();

  /*
   * directory for outputting source coverage data.
   */
  private File reportOutputDir = new File("patchwork");

  /*
   * low thershold
   */
  private double low;

  /*
   * high threshold
   */
  private double high;

  public TestRunner(ClassLoader root) {
    this.testLoader = new CompoundClassLoader(root);
    this.instLoader = new ByteArrayClassLoader(this.testLoader);
    this.testLoader.addLoader("instrumented", this.instLoader);
    this.graphBuilder = new CachedControlGraphBuilder(instLoader);
    this.backend = new OnlineBackend(reporter);
  }

  /**
   * Add a new jar file to be instrumented by this runner object. The classes
   * contained in the given archive will be instrumented by coverage
   * information.
   * 
   * @param jarfile
   *          a File pointing to some jar file.
   * @throws IOException
   */
  private void addJar(ZipFile jarfile) throws IOException {
    log.info("[Instrument] Instrumenting jar " + jarfile.getName());
    /* read each class in jcl and instrument it */
    for (Enumeration e = jarfile.entries(); e.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
        /* create classfile and instrument it */
        ClassFile cf = new ClassFile();
        cf.read(new DataInputStream(jarfile.getInputStream(entry)));
        instrument.instrument(cf);
      }
    }
    log.info("[Instrument] Done instrumenting jar " + jarfile.getName());
  }

  /**
   * Add all the classes found in the given directory to thiis instrumented set
   * of classes.
   * 
   * @param dir
   *          a File which must be a directory.
   * @throws IOException
   */
  private void addDirectory(File dir) throws IOException {
    log.info("[Instrument] Instrumenting directory " + dir.getPath());
    File[] files = dir.listFiles();
    int ln = files.length;
    for (int i = 0; i < ln; i++) {
      if (files[i].canRead())
        /* recursive exploration */
        if (files[i].isDirectory())
          addDirectory(files[i]);
        else if (files[i].getName().endsWith(".class")) {
          addFile(files[i]);
        }
    }
    log.info("[Instrument] Done nstrumenting directory " + dir.getPath());
  }

  /**
   * Add the content of file this instrumented set.
   * 
   * @param file
   * @throws IOException
   */
  private void addFile(File file) throws IOException {
    /* instrument class */
    ClassFile cf = new ClassFile();
    FileInputStream fis = new FileInputStream(file);
    cf.read(new DataInputStream(fis));
    instrument.instrument(cf);
    fis.close();
    log.info("[Instrument] Instrumenting file  " + file.getPath());
  }

  /**
   * Instrument all classes found in the given list of File objects.
   * 
   * @param l
   *          a List<File> instance
   * @param number
   *          of instrumented classes
   */
  private int instrument(List /* < File > */<File>l) {
    int nb = 0;
    /* collect class files */
    for (Iterator<File> i = l.iterator(); i.hasNext();) {
      File f = i.next();
      try {
        if (f.isDirectory())
          addDirectory(f);
        else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip"))
          addJar(new ZipFile(f));
        else if (f.getName().endsWith(".class"))
          addFile(f);
        else
          log.warn("Skipping unknown classpath element " + f);
      } catch (IOException e) {
        log.error("Cannot instrument classes from path " + f + " : "
            + e.getMessage());
      }
    }
    /* prepare classfiles for classloading */
    for (Iterator i = instrument.getClasses().iterator(); i.hasNext(); nb++) {
      ClassFile cf = (ClassFile) i.next();
      try {
        instLoader.putBytes(cf.getClassFileInfo().getName().replace('/', '.'),
            cf.getBytes());
      } catch (IOException e) {
        e.printStackTrace();
        log.error("Cannot get bytes for instrumented class  "
            + cf.getClassFileInfo().getName() + " : " + e.getMessage());
      }
    }
    /* append coverage */
    try {
      for (Iterator i = generator.generate(instrument).iterator(); i.hasNext();) {
        ClassFile cf = (ClassFile) i.next();
        instLoader.putBytes(cf.getClassFileInfo().getName().replace('/', '.'),
            cf.getBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Cannot construct CoverageInfo class : " + e.getMessage());
    }
    return nb;
  }

  /**
   * Prepare tests to execute from the list of entries given.
   * 
   * @param cp
   */
  private List<String> prepareTests(List<File> l) {
    List<String> testclasses = new ArrayList<String>();
    /* collect class files from the given list of classpath entries */
    for (Iterator<File> i = l.iterator(); i.hasNext();) {
      File f = i.next();
      try {
        if (f.isDirectory()) {
          testclasses.addAll(testDirectory(f, new File("")));
          testLoader.addLoader(f.getName(), new DirClassLoader(testLoader, f));
        } else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip"))
          testclasses.addAll(testJar(new ZipFile(f)));
        else
          log.warn("Skipping unknown test classpath element " + f);
      } catch (IOException e) {
        log.error("Cannot instrument classes from path " + f + " : "
            + e.getMessage());
      }
    }
    return testclasses;
  }

  private List<String> testJar(ZipFile file) {
    List<String> suites = new ArrayList<String>();
    testLoader.addLoader(file.getName(), new JarClassLoader(testLoader, file));
    /* read each class and add it to test classes if pattern matches */
    for (Enumeration e = file.entries(); e.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      if (includePattern.matcher(entry.getName()).matches()
          && !excludePattern.matcher(entry.getName()).matches()) {
        /* add class name to suites */
        String clname = entry.getName();
        clname = clname.substring(0, clname.lastIndexOf('.'));
        clname = clname.replace('/', '.');
        suites.add(clname);
      }
    }
    return suites;
  }

  /**
   * Runs a set of test suites.
   * <p>
   * This method is specific to each testing subsystem as defined in the main
   * command-line. The parameter contains test classes names collected from the
   * test classpath using the test pattern defined for this runner.
   * </p>
   * <p>
   * It is up to the implementation to instantiate the classes from the
   * testloader, run them and provide a result. By convention, if the returned
   * value is null, then the test is considered a success.
   * </p>
   * 
   * @param l
   *          a List<String> object containing test classes names.
   * @return a test result or null if everything is OK.
   */
  public abstract Object runSuite(List<String> l);

  /**
   * @return Returns the coveredClasspath.
   */
  public List<File> getCoveredClasspath() {
    return coveredClasspath;
  }

  /**
   * @param coveredClasspath
   *          The coveredClasspath to set.
   */
  public void setCoveredClasspath(List<File> coveredClasspath) {
    this.coveredClasspath = coveredClasspath;
  }

  /**
   * @return Returns the testPattern.
   */
  public Pattern getIncludePattern() {
    return includePattern;
  }

  /**
   * @param testPattern
   *          The testPattern to set.
   */
  public void setIncludePattern(Pattern testPattern) {
    this.includePattern = testPattern;
  }

  private List<String> testDirectory(File f, File root) {
    File[] files = f.listFiles();
    List<String> ret = new ArrayList<String>();
    int ln = files.length;
    for (int i = 0; i < ln; i++) {
      if (files[i].canRead())
        /* recursive exploration */
        if (files[i].isDirectory())
          ret
              .addAll(testDirectory(files[i],
                  new File(root, files[i].getName())));
        else if (files[i].getName().endsWith(".class")) {
          if (includePattern.matcher(files[i].getName()).matches()
              && !excludePattern.matcher(files[i].getName()).matches()) {
            /* add class name to suites */
            String clname = new File(root, files[i].getName()).getPath();
            clname = clname.substring(1, clname.lastIndexOf('.'));
            clname = clname.replace('/', '.');
            ret.add(clname);
          }
        }
    }
    return ret;
  }

  private void prepareTestObjective(Collection<String> name) {
    PackageTreeCoverageBuilder builder = new PackageTreeCoverageBuilder();
    builder.setLog(log);
    builder.setGraphBuilder(graphBuilder);
    builder.setObjectives(coverageObjectives);
    CoverageObjective objective = builder.build(name);
    reporter.setObjective(objective);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    long ts = System.nanoTime();
    long ts2 = System.nanoTime();
    if (outputDir != null) {
      instLoader.setWrite(true);
      instLoader.setRootDir(outputDir.getAbsolutePath());
    }
    /* initialize coverage info */
    try {
      if (!coveredClasspath.isEmpty()) {
        int nb = instrument(coveredClasspath);
        ts2 = System.nanoTime();
        log.info("Instrumented " + nb + " classes in " + (ts2 - ts) / 1000000
            + "ms");
      }
      if (!sourcepath.isEmpty()) {
        prepareSourceCoverage();
      }
      if (!testClasses.isEmpty()) {
        // extract coverage information
        Class ci = instLoader.findClass(generator.getClassName().replace('/',
            '.'));
        for (Class cls : ci.getInterfaces())
          System.err.println("CoverageInfoImpl interface " + cls.getName()
              + " class loader is " + cls.getClassLoader());

        System.err.println("CoverageInfo cl:"
            + CoverageInfo.class.getClassLoader());
        CoverageInfo info = (CoverageInfo) ci.newInstance();
        Coverage.setCoverageInfo(info);
        Coverage.setBackend(backend);
        // construct test objective
        prepareTestObjective(Arrays.asList(info.getClasses()));
        List<String> tnames = prepareTests(testClasses);
        // run tests
        ts = System.nanoTime();
        TestResult ret = (TestResult) runSuite(tnames);
        ts2 = System.nanoTime();
        log.info("Executed " + ret.runCount() + " tests, " + ret.failureCount()
            + " failures, " + ret.errorCount() + " errors in " + (ts2 - ts)
            / 1000000 + "ms");
        /* flush coverage info */
        Coverage.closeOutput();
        /* report */
        if (!reportOutputDir.exists() && !reportOutputDir.mkdirs())
          log.warn("Cannot make directory " + reportOutputDir
              + " for storing reporting data ");
        else {
          ts = System.nanoTime();
          reporter.report(new FileOutputStream(new File(reportOutputDir,
              "patchwork.data")));
          ts2 = System.nanoTime();
          log.info("Generated coverage report in " + (ts2 - ts) / 1000000
              + "ms");
        }
      } else {
        // just dump
        if (outputDir != null)
          instLoader.buildAll();
      }
    } catch (ClassNotFoundException e1) {
      log.error("Cannot load coverage info class: " + e1.getMessage());
    } catch (Exception e) {
      log.error("Cannot close coverage outputter " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void usage() {
    System.err.println("Usage: java oqube.patchwork.test.TestRunner [options]");
    System.err.println("      -h|-? : displays this help");
    System.err
        .println("      -c <cpentry>(:<cpentry>)*  : defines classpath for instrumented classes (.)");
    System.err
        .println("      -t <cpentry>(:<cpentry>)*  : defines classpath for test classes (.)");
    System.err
        .println("      -g <clname>                : name of generator class (CoverageInfo) ");
    System.err
        .println("      -p <regex>                 : pattern for test classes (*Test.class)");
    System.err
        .println("      -P <regex>                 : exclusion pattern for test classes (^$)");
    System.err
        .println("      -o <dir>                   : output directory for instruemented classes (none)");
    System.err
        .println("      -r <clname>                : reporter class name (SimpleCoverageReporter)");
    System.err
        .println("      -i                         : instrumentation mode");
    System.err.println("      -x                         : xhtml output");
    System.err
        .println("      -O                         : report and root covered source output directory (./patchwork)");
    System.err
        .println("      -C <clname>(:<clname>)*    : list of method objective subclasses to compute coverage (AllEdgesObjective)");
    System.err
        .println("      -s <cpentry>(:<cpentry>)*  : source files classpath (.)");
    System.err.println("<cpentry> is either a directory, a jar or a zip file");
  }

  public static void main(String[] args) {
    List<String> instcp = new ArrayList<String>();
    List<String> testcp = new ArrayList<String>();
    List<String> covobj = new ArrayList<String>();
    List<File> cp = new ArrayList<File>();
    /* command line handling */
    CommandLine cl = new CommandLine();
    cl.addOptionMultiple('C', ':'); /* coverage classe */
    cl.addOptionMultiple('c', ':'); /* classpath of instrumented classes */
    cl.addOptionMultiple('t', ':'); /* classpath of test classes */
    cl.addOptionSingle('g'); /* generator class name */
    cl.addOptionSingle('p'); /* test classes pattern */
    cl.addOptionSingle('P'); /* test classes exclude pattern */
    cl.addOptionSingle('o'); /* output directory */
    cl.addOptionSingle('r'); /* reporter class name */
    cl.addOption('i'); /* instrumentation mode only - modify class loading */
    cl.addOption('h'); /* help */
    cl.addOptionMultiple('s', ':'); /* sourcepath */
    cl.addOption('x'); /* xhtml format */
    cl.addOptionSingle('O'); /* covered sources output */
    cl.addOption('?');
    cl.parseOptions(args);
    /* runner */
    TestRunner runner = null;
    if (cl.isSet('i'))
      runner = new JUnitRunner(Thread.currentThread().getContextClassLoader()
          .getParent());
    else
      runner = new JUnitRunner(Thread.currentThread().getContextClassLoader());
    if (cl.isSet('?') || cl.isSet('h')) {
      usage();
      System.exit(0);
    }
    if (cl.isSet('o')) {
      File id = new File((String) cl.getOption('o').getArgument());
      if (id.exists() || id.mkdirs())
        runner.setOutputDir(id);
      else {
        System.err.println("Cannot read or create output directory "
            + cl.getOption('g').getArgument());
        usage();
        System.exit(1);
      }
    }
    if (cl.isSet('O'))
      runner.reportOutputDir = new File((String) cl.getOption('O')
          .getArgument());
    if (cl.isSet('c'))
      instcp.addAll((List<String>) cl.getOption('c').getArgument());
    if (cl.isSet('t'))
      testcp.addAll((List<String>) cl.getOption('t').getArgument());
    if (cl.isSet('C'))
      covobj.addAll((List<String>) cl.getOption('C').getArgument());
    if (cl.isSet('g')) {
      String gn = (String) cl.getOption('g').getArgument();
      runner.generator = new CoverageGenerator(gn);
    } else
      runner.generator = new CoverageGenerator();
    if (cl.isSet('p'))
      runner.includePattern = Pattern.compile((String) cl.getOption('p')
          .getArgument());
    else
      runner.includePattern = Pattern.compile(".*Test.class");
    if (cl.isSet('P'))
      runner.excludePattern = Pattern.compile((String) cl.getOption('P')
          .getArgument());
    else
      runner.excludePattern = Pattern.compile("^$");
    /* instantiate file lists */
    List<File> covcp = new ArrayList<File>();
    for (Iterator<String> i = instcp.iterator(); i.hasNext();) {
      File f = new File(i.next());
      covcp.add(f);
    }
    runner.setCoveredClasspath(covcp);
    covcp = new ArrayList<File>();
    for (Iterator<String> i = testcp.iterator(); i.hasNext();) {
      File f = new File(i.next());
      covcp.add(f);
    }
    runner.setTestClasses(covcp);
    /* formatting type */
    if (cl.isSet('x'))
      runner.reportFormater = new XHTMLFormatter();
    /* sourcepath */
    if (cl.isSet('s')) {
      List<File> srcpath = new ArrayList<File>();
      for (Iterator i = ((List) cl.getOption('s').getArgument()).iterator(); i
          .hasNext();) {
        File f = new File((String) i.next());
        srcpath.add(f);
      }
      runner.setSourcepath(srcpath);
    }
    /* ocverage objcetives */
    if (!covobj.isEmpty()) {
      List<Class> cls = new ArrayList<Class>();
      for (String cln : covobj) {
        try {
          cls.add(runner.testLoader.loadClass(cln));
        } catch (ClassNotFoundException e) {
          runner.log.warn("Cannot load objective class " + cln + ", ignoring",
              e);
        }
      }
      runner.setCoverageObjectives(cls);
    }
    /* done */
    runner.run();
  }

  private void prepareSourceCoverage() {
    SourceMapper sm = new SourceMapper();
    sm.setSourceToURL(new SourceToURL(getSourcepath()));
    sm.setBasedir(reportOutputDir);
    sm.setGraphBuilder(graphBuilder);
    sm.setUrlPrefix("patchwork/");
    reportFormater.setHigh(high);
    reportFormater.setLow(low);
    reporter.setSourceMapper(sm);
    reporter.setFormater(reportFormater);
  }

  /**
   * @return Returns the outputDir.
   */
  public File getOutputDir() {
    return outputDir;
  }

  /**
   * @param outputDir
   *          The outputDir to set.
   */
  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
  }

  /**
   * @return Returns the log.
   */
  public Log getLog() {
    return log;
  }

  /**
   * @param log
   *          The log to set.
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * @return the excludePattern
   */
  public Pattern getExcludePattern() {
    return excludePattern;
  }

  /**
   * @param excludePattern
   *          the excludePattern to set
   */
  public void setExcludePattern(Pattern excludePattern) {
    this.excludePattern = excludePattern;
  }

  /**
   * @return the sourcepath
   */
  public List<File> getSourcepath() {
    return sourcepath;
  }

  /**
   * @param sourcepath
   *          the sourcepath to set
   */
  public void setSourcepath(List<File> sourcepath) {
    this.sourcepath = sourcepath;
  }

  /**
   * @return the sourceOutputDir
   */
  public File getReportOutputDir() {
    return reportOutputDir;
  }

  /**
   * @param sourceOutputDir
   *          the sourceOutputDir to set
   */
  public void setReportOutputDir(File sourceOutputDir) {
    this.reportOutputDir = sourceOutputDir;
  }

  /**
   * @return the coverageObjectives
   */
  public List<Class> getCoverageObjectives() {
    return coverageObjectives;
  }

  /**
   * @param coverageObjectives
   *          the coverageObjectives to set
   */
  public void setCoverageObjectives(List<Class> coverageObjectives) {
    this.coverageObjectives = coverageObjectives;
  }

  /**
   * @return the reportFormater
   */
  public OutputFormatter getReportFormater() {
    return reportFormater;
  }

  /**
   * @param reportFormater
   *          the reportFormater to set
   */
  public void setReportFormater(OutputFormatter reportFormater) {
    this.reportFormater = reportFormater;
  }

  public void setLow(double low) {
    this.low = low;
  }

  public void setHigh(double high) {
    this.high = high;
  }

  /**
   * @return the testClasses
   */
  public List<File> getTestClasses() {
    return testClasses;
  }

  /**
   * @param testClasses the testClasses to set
   */
  public void setTestClasses(List<File> testClasses) {
    this.testClasses = testClasses;
  }

}
