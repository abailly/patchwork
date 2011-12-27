package oqube.patchwork.graph;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.instructions.Sequence;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.TestClassfileMaker;

import fr.lifl.utils.JarClassLoader;

import junit.framework.TestCase;

public class ControlGraphTest extends TestCase {

  private CachedControlGraphBuilder builder;

  protected void setUp() throws Exception {
    super.setUp();
    this.builder = new CachedControlGraphBuilder();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test01Factory() {
    try {
      ControlGraph cg = builder.createGraphForMethod(
          "oqube/patchwork/graph/ControlGraph", "<init>", "()V");
      assertNotNull(cg);
    } catch (IOException e) {
      fail("Should have found method: " + e);
    }
  }

  // Note there is some side-effects with caching from the preceding
  // method
  public void test01FactoryWithDots() {
    try {
      ControlGraph cg = builder.createGraphForMethod(
          "oqube.patchwork.graph.ControlGraph", "<init>", "()V");
      assertNotNull(cg);
    } catch (IOException e) {
      fail("Should have found method: " + e);
    }
  }

  public void test02FactoryFail() {
    try {
      ControlGraph cg = builder
          .createGraphForMethod(
              "oqube/patchwork/graph/ControGraph",
              "createGraphForMethod",
              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Loqube/patchwork/graph/ControlGraph;");
      fail("Should hav thrown exception, class does not exist");
    } catch (IOException e) {
    }
  }

  public void test03FactoryFail() {
    try {
      ControlGraph cg = builder
          .createGraphForMethod(
              "oqube/patchwork/graph/ControlGraph",
              "creatGraphForMethod",
              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Loqube/patchwork/graph/ControlGraph;");
      fail("Should hav thrown exception, method does not exist");
    } catch (IOException e) {
    }
  }

  public void test04FactoryFail() {
    try {
      ControlGraph cg = builder
          .createGraphForMethod(
              "oqube/patchwork/graph/ControlGraph",
              "createGraphForMethod",
              "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)Loqube/patchwork/graph/ControlGraph;");
      fail("Should hav thrown exception, method does not exist with this signature");
    } catch (IOException e) {
    }
  }

  public void test05LineInfo() throws IOException {
    /* create classfile object */
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "linesinfo.bytes");
    ControlGraph cg = new CachedControlGraphBuilder().createAllGraphs(is).get("oqube/patchwork/report/coverage/AllNodesObjective.update(Ljava/util/Map;)V");
    // check line mapping
    int[][] lines = { { 59, 60 }, { 61, 61 }, { 64, 67 }, { 68, 70 },
         { 72, 72 }, { 74, 74 },{ 75, 76}, { 76, 77 }, { 76, 76 }, { 74, 74 },
        { 79, 80 } };
    for (BasicBlock bb : cg.getBlocks()) {
      assertEquals(lines[bb.getNumBlock() - 1][0], bb.getStartLine());
      assertEquals(lines[bb.getNumBlock() - 1][1], bb.getEndLine());
    }
  }

  public void testLineInfoFromMuseJar() throws Exception {
    builder = new CachedControlGraphBuilder(new URLClassLoader(new URL[] { new URL(
        "file:src/test/resources/muse.jar") }));
    Map<String, ControlGraph> meths = builder
        .createGraphsForClass("oqube.muse.html.MuseHTMLSink");
    // check all control blocks have line info
    for (Iterator<Entry<String, ControlGraph>> i = meths.entrySet().iterator(); i
        .hasNext();) {
      Map.Entry<String, ControlGraph> me = i.next();
      ControlGraph cg = me.getValue();
      for (BasicBlock bb : cg.getBlocks()) {
        assertTrue("Bad start line info in " + me.getKey() + " at block "
            + bb.getNumBlock(), bb.getStartLine() > 0);
        assertTrue("Bad end line info in " + me.getKey() + " at block "
            + bb.getNumBlock(), bb.getEndLine() > 0);
      }
    }
  }

  /*
   * check code structure for code with exception and finally blocks (without
   * jsr). We specifically check block splitting on try range coverage.
   */
  public void testExceptionsAndFinallyInsideOneBlock() throws Exception {
    TestClassfileMaker testClassfileMaker = new TestClassfileMaker();
    /* create classfile object */
    ClassFile cf = testClassfileMaker.makeTestClassfile();
    CodeAttribute ca = testClassfileMaker.getCode();
    /* code sequence */
    Sequence seq = new Sequence(cf);
    seq
    ._iconst_0() 
    ._istore_1() // block try
    ._iload_1()
    ._istore_2() 
    ._iconst_0() // fin bloc try
    ._istore_1() 
    ._iload_1()
    ._istore_2()
    ._iload_1()
    ._goto(13)  // jump to end
    ._iconst_1() // catch block 
    ._istore_1()
    ._iload_1()
    ._istore_2()
    ._iconst_0()
    ._ireturn()
    ._iconst_3() // bloc finally
    ._istore_1() 
    ._iload_1()
    ._istore_2()
     ._ireturn(); // end
    ca.add(seq);
    /* add exceptions */
    ca.makeExceptionEntry("java/lang/Exception", (short) 1, (short) 4,
        (short) 12);
    ca.makeExceptionEntry((short)0, (short) 1, (short) 4,
        (short) 18);
    // finally pour exception
    ca.makeExceptionEntry((short)0, (short) 12, (short) 18,
        (short) 18);
    /* construct graph */
    ControlGraph cg = new ControlGraph(cf.getMethodInfo("test", "(II)I"));
    /* check blocks */
    List<BasicBlock> blocks = cg.getBlocks();
    assertEquals("Bad number of blocks", 6,blocks.size());
   }

  /*
   * check code structure for code with exception and finally blocks (without
   * jsr). We specifically check block splitting on try range coverage with
   * 
   */
  public void testExceptionsAndFinallySpanningSeveralBlocks() throws Exception {
    TestClassfileMaker testClassfileMaker = new TestClassfileMaker();
    /* create classfile object */
    ClassFile cf = testClassfileMaker.makeTestClassfile();
    CodeAttribute ca = testClassfileMaker.getCode();
    /* code sequence */
    Sequence seq = new Sequence(cf);
    seq
    ._iconst_0() 
    ._istore_1() // block try
    ._iload_1()
    ._istore_2() 
    ._iconst_0() 
    ._goto(4)    // fin bloc
    ._istore_1() // debut bloc
    ._iload_1()  // debut bloc
    ._istore_2() // fin bloc try
    ._iload_1()
    ._goto(13)   // jump to end
    ._iconst_1() // catch block 
    ._istore_1()
    ._iload_1()
    ._istore_2()
    ._iconst_0()
    ._ireturn()
    ._iconst_3() // bloc finally
    ._istore_1() 
    ._iload_1()
    ._istore_2()
     ._ireturn(); // end
    ca.add(seq);
    /* add exceptions */
    ca.makeExceptionEntry("java/lang/Exception", (short) 1, (short) 9,
        (short) 15);
    ca.makeExceptionEntry((short)0, (short) 1, (short) 10,
        (short) 21);
    // finally pour exception
    ca.makeExceptionEntry((short)0, (short) 15, (short) 21,
        (short) 21);
    /* construct graph */
    ControlGraph cg = new ControlGraph(cf.getMethodInfo("test", "(II)I"));
    /* check blocks */
    List<BasicBlock> blocks = cg.getBlocks();
    System.err.println(blocks);
    assertEquals("Bad number of blocks", 8,blocks.size());
   }
}
