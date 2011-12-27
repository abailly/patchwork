/**
 * 
 */
package oqube.patchwork.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import oqube.patchwork.report.coverage.XHTMLFormatter;
import oqube.patchwork.report.source.SourceMapper;
import oqube.patchwork.report.source.SourceToURL;
import fr.lifl.utils.CommandLine;

/**
 * Reporting from file main application.
 * This application 
 * @author nono
 *
 */
public class Report {


  public static void usage() {
    System.err
        .println("Usage: java oqube.patchwork.report.SimpleReporter [-s <sources> -x] -i <datafile> [-o <outputfile>]");
    System.err.println("      -h|-? : displays this help");
  }

  
  public static void main(String[] args) {
    /* command line handling */
    CommandLine cl = new CommandLine();
    cl.addOptionSingle('i'); /* coverage data */
    cl.addOptionSingle('o'); /* output report */
    cl.addOptionMultiple('s',':'); /*
     * set source path for generating source
     * coverage report
     */
    cl.addOptionMultiple('c',':'); /*
     * set class path for (non)instrumented classes
     */
    cl.addOption('x'); /* xhtml format */
    cl.addOption('h'); /* help */
    cl.addOption('?');
    cl.parseOptions(args);
    /* reporter */
    SimpleReporter reporter = new SimpleReporter();
    if (cl.isSet('?') || cl.isSet('h')) {
      usage();
      System.exit(0);
    }
    /* formatting type */
    if(cl.isSet('x')) 
      reporter.setFormater(new XHTMLFormatter());
    /* sourcepath */
    if(cl.isSet('s')) {
      SourceMapper sm = new SourceMapper();
      List<File> spath = new ArrayList<File>();
      for (String sp : (List<String>) cl.getOption('s').getArgument()) {
        File f = new File(sp);
        spath.add(f);
      }
      sm.setSourceToURL(new SourceToURL(spath));
      reporter.setSourceMapper(sm);
    }
    if (!cl.isSet('i')) {
      usage();
      System.exit(1);
    } else {
      File id = new File((String) cl.getOption('i').getArgument());
      try {
        reporter.analyze(new FileInputStream(id));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Caught exception while analyzing " + id + ": "
            + e.getLocalizedMessage());
        System.exit(1);
      }
    }
    if (cl.isSet('o')) {
      File id = new File((String) cl.getOption('o').getArgument());
      try {
        reporter.report(new FileOutputStream(id));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Caught exception while analyzing " + id + ": "
            + e.getLocalizedMessage());
        System.exit(1);
      }
    } else {
      reporter.report(System.out);
    }
  }

  /*
   * construct a classloader from a list of entrise
   */
  private static ClassLoader makeLoader(List<String> list) throws MalformedURLException {
    int n = list.size();
    List<URL> urls = new ArrayList<URL>(n);
    for(String entry: list){
      urls.add(new File(entry).toURL());
    }
    return new URLClassLoader(urls.toArray(new URL[n]));
  }
}
