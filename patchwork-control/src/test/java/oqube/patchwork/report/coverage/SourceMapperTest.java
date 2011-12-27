package oqube.patchwork.report.coverage;

import java.io.File;
import java.io.IOException;

import com.uwyn.jhighlight.renderer.CppXhtmlRenderer;
import com.uwyn.jhighlight.renderer.JavaXhtmlRenderer;
import com.uwyn.jhighlight.renderer.Renderer;


import junit.framework.TestCase;
import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.report.source.SourceMapper;
import oqube.patchwork.report.source.SourceToURL;
import oqube.bytes.utils.TemporaryFS;

public class SourceMapperTest extends TestCase {

  private SourceMapper sm;

  private CachedControlGraphBuilder builder;

  private TemporaryFS temp;
  
  protected void setUp() throws Exception {
    super.setUp();
    this.sm = new SourceMapper();
    this.sm.setGraphBuilder(this.builder = new CachedControlGraphBuilder());
    // create tempdir
    this.temp = new TemporaryFS("tmp");
  }

 
  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    temp.clean();
    super.tearDown();
  }

  public void testLinesHighlight() throws IOException {
    String meth = "oqube/patchwork/report/coverage/SourceMapperTest.testLinesHighlight()V";
    this.sm.setBasedir(temp.root());
    this.sm.setSourceToURL(new SourceToURL());
    this.sm.getSourceToURL().addSourcepath(new File("src/test/java"));
    ControlGraph cg = builder.createGraphForMethod(meth);
    // check all control blocks have line info
    for (BasicBlock bb : cg.getBlocks()) {
      // update
      sm.update(0, meth, bb.getNumBlock());
    }
    // extract coverage info
    this.sm.highlight("SourceMapperTest.java");
    File f = new File(temp.root(),"SourceMapperTest.java.html");
    assertTrue("Coverage html file not created",f.exists());
  }
  
  public void testMakeRenderer () {
    Renderer r = this.sm.makeRenderer("toto.java");
    assertTrue("bad renderer class",JavaXhtmlRenderer.class == r.getClass());    
  }

  public void testMakeDefaultRenderer () {
    Renderer r = this.sm.makeRenderer("toto.tutu");
    assertTrue("bad renderer class",JavaXhtmlRenderer.class == r.getClass());    
  }
  
  public void testMakeDefaultRendererWithoutSuffix () {
    Renderer r = this.sm.makeRenderer("toto");
    assertTrue("bad renderer class",JavaXhtmlRenderer.class == r.getClass());    
  }
  
  public void testMakeCppRenderer () {
    Renderer r = this.sm.makeRenderer("toto.cpp");
    assertEquals("bad renderer class",CppXhtmlRenderer.class,r.getClass());    
  }

}
