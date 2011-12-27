/**
 * 
 */
package oqube.patchwork.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import oqube.bytes.utils.Fileset;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import fr.lifl.utils.Pipe;

/**
 * A maven report for patchwork coverage data. This report expects to find the
 * result of a patchwork execution in some directory in the build directory of
 * the project: A patchwork.data file that contains the coverage data and
 * optionally source files (all other .html files) for translating coverage
 * information to source data.
 * 
 * 
 * @author nono
 * @goal report
 * @phase site
 */
public class PatchworkReportMojo extends AbstractMavenReport {

  /**
   * Directory where reports will go.
   * 
   * @parameter expression="${project.reporting.outputDirectory}/patchwork
   * 
   * @required
   * @readonly
   */
  private String outputDirectory;

  /**
   * Base directory where all reports are written to.
   * 
   * @parameter expression="${project.build.directory}/patchwork"
   */
  private File reportsDirectory;

  /**
   * <i>Maven Internal</i>: The Doxia Site Renderer.
   * 
   * @component
   */
  private Renderer siteRenderer;

  /**
   * <i>Maven Internal</i>: The Project descriptor.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * @return Returns the siteRenderer.
   */
  public Renderer getSiteRenderer() {
    return siteRenderer;
  }

  protected MavenProject getProject() {
    return project;
  }

  protected void executeReport(Locale arg0) throws MavenReportException {
    // sink for writing the report
    Sink sink = getSink();
    // open the coverage data
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(new File(reportsDirectory,
          "patchwork.data")));
    } catch (FileNotFoundException e) {
      sink
          .text("Cannot find coverage information: Looking for 'coverage.data' file in directory "
              + reportsDirectory);
      return;
    }
    // write its content into sink
    String data = null;
    try {
      while ((data = br.readLine()) != null)
        sink.rawText(data);
    } catch (IOException e) {
      throw new MavenReportException("Error in reading coverage.data file", e);
    }
    getLog().info("Done generating coverage report ");
    // copy source html files as is
    Fileset fs = new Fileset(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".html");
      }
    });
    // create output directory
    File outdir = new File(outputDirectory);
    if (!outdir.exists() && !outdir.mkdir()) {
      getLog().error("Cannot create output directory " + outdir);
      return;
    }
    for (File in : fs.files(reportsDirectory)) {
      String name = in.getName();
      File out = new File(outdir, name);
      // copy content
      try {
        new Pipe(new FileOutputStream(out), new FileInputStream(in)).pump();
        getLog().info("Done generating source coverage report for " + name);
      } catch (IOException e) {
        getLog().error("Error while copying source file " + in, e);
      }
    }
  }

  public String getDescription(Locale locale) {
    return getBundle(locale).getString("report.patchwork.description");
  }

  public String getName(Locale locale) {
    return getBundle(locale).getString("report.patchwork.name");
  }

  public String getOutputName() {
    return "patchwork";
  }

  private ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle("patchwork", locale, this.getClass()
        .getClassLoader());
  }

  /**
   * @param siteRenderer
   *          The siteRenderer to set.
   */
  public void setSiteRenderer(Renderer siteRenderer) {
    this.siteRenderer = siteRenderer;
  }

  /**
   * For testing purpose only.
   * 
   * @param project
   *          The project to set.
   */
  public void setProject(MavenProject project) {
    this.project = project;
  }

  protected String getOutputDirectory() {
    return outputDirectory;
  }

}
