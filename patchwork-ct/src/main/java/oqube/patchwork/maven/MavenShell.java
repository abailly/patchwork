/**
 * 
 */
package oqube.patchwork.maven;

import java.io.File;

import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

/**
 * A class that loads a POM and can receive commands.
 * 
 * @author nono
 *
 */
public class MavenShell {

  private MavenEmbedder maven = new MavenEmbedder();
  private MavenProject project;
  
  public void loadPOM(String string) throws ProjectBuildingException, MavenEmbedderException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    maven.setClassLoader(classLoader);
    maven.setLogger(new MavenEmbedderConsoleLogger());
    maven.start();
    this.project = maven.readProject(new File(string));
  }

  public MavenProject getProject() {
    return project;
  }

}
