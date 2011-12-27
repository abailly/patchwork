package oqube.patchwork.graph;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Map;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.struct.MethodFileInfo;
import junit.framework.TestCase;

public class DataFlowTest extends TestCase {

  private ControlGraph cg;
  private DataFlow df;

  protected void setUp() throws Exception {
    super.setUp();
    /* create classfile object */
    ClassFile cf = new ClassFile();
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "oqube/patchwork/graph/ControlGraph.class");
    cf.read(new DataInputStream(is));
    MethodFileInfo mfi = (MethodFileInfo) cf.getMethodInfo("parseCode",
        "(Ljava/util/List;I)V");
    cg = new ControlGraph(mfi);
    df = new DataFlow(cg);
  }

  public void test01() {
    Map m = df.getData();
    System.err.print(m);
  }
}
