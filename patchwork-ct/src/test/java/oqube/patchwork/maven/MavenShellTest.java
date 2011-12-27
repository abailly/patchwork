package oqube.patchwork.maven;

import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

import junit.framework.TestCase;

public class MavenShellTest extends TestCase {

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
  }
  
  public void testLoadPOM() throws ProjectBuildingException,
      MavenEmbedderException {
    MavenShell shell = new MavenShell();
    shell.loadPOM("src/test/resources/test01/pom.xml");
    // check project
    MavenProject pom = shell.getProject();
    assertNotNull(pom);
  }

}
