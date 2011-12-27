/**
 * 
 */
package oqube.patchwork.it;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.maven.BuildFailureException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.cli.ConsoleDownloadMonitor;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.embedder.PlexusLoggerAdapter;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.monitor.event.DefaultEventMonitor;
import org.apache.maven.monitor.event.EventMonitor;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.dag.CycleDetectedException;

/**
 * Test cases for the patchwork maven plugin.
 * 
 * @author nono
 * 
 */
public class PatchworkMavenTest extends TestCase {

  private MavenEmbedder maven;

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    maven = new MavenEmbedder();
    maven.setClassLoader(getClass().getClassLoader());
    maven.setLogger(new MavenEmbedderConsoleLogger());
    try {
      maven.start();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

  }

  public void _test01() throws CycleDetectedException,
      LifecycleExecutionException, BuildFailureException,
      DuplicateProjectException, ArtifactResolutionException,
      ArtifactNotFoundException, ProjectBuildingException {
    // redirect output
    PrintStream out = System.out;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bos);
    System.setOut(ps);

    File targetDirectory = new File("target/test-classes/testPlugin01");
    File pomFile = new File(targetDirectory, "pom.xml");
    MavenProject pom = maven.readProjectWithDependencies(pomFile);
    MavenEmbedderConsoleLogger logger = new MavenEmbedderConsoleLogger();
    logger.setThreshold(MavenEmbedderConsoleLogger.LEVEL_DEBUG);
    PlexusLoggerAdapter plexusLoggerAdapter = new PlexusLoggerAdapter(logger);
    plexusLoggerAdapter.setThreshold(PlexusLoggerAdapter.LEVEL_DEBUG);
    EventMonitor eventMonitor = new DefaultEventMonitor(plexusLoggerAdapter);
    maven.execute(pom, Collections.singletonList("patchwork:test"),
        eventMonitor, new ConsoleDownloadMonitor(), null, targetDirectory);
    // check output
    System.setOut(out);
    ps.flush();
    ps.close();
    String s = bos.toString();
    assertTrue("unexpected content in output data", s
        .contains("isEquilateral ; 4.0 ; 8.0 ; 7.0 ; 87.5"));
  }

  public void _testDependencyWithCompileScope() throws CycleDetectedException,
      LifecycleExecutionException, BuildFailureException,
      DuplicateProjectException, ArtifactResolutionException,
      ArtifactNotFoundException, ProjectBuildingException {
    // redirect output
    PrintStream out = System.out;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bos);
    System.setOut(ps);

    File targetDirectory = new File("target/test-classes/testPlugin02");
    File pomFile = new File(targetDirectory, "pom.xml");
    MavenProject pom = maven.readProjectWithDependencies(pomFile);
    MavenEmbedderConsoleLogger logger = new MavenEmbedderConsoleLogger();
    logger.setThreshold(MavenEmbedderConsoleLogger.LEVEL_DEBUG);
    PlexusLoggerAdapter plexusLoggerAdapter = new PlexusLoggerAdapter(logger);
    plexusLoggerAdapter.setThreshold(PlexusLoggerAdapter.LEVEL_DEBUG);
    EventMonitor eventMonitor = new DefaultEventMonitor(plexusLoggerAdapter);
    maven.execute(pom, Collections.singletonList("patchwork:test"),
        eventMonitor, new ConsoleDownloadMonitor(), null, targetDirectory);
    // check output
    System.setOut(out);
    ps.flush();
    ps.close();
    String s = bos.toString();
    assertTrue("unexpected content in output data", s
        .contains("isEquilateral ; 4.0 ; 8.0 ; 7.0 ; 87.5"));
  }

}
