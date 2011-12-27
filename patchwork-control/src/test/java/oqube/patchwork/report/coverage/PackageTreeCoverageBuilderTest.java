package oqube.patchwork.report.coverage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;

import junit.framework.TestCase;

public class PackageTreeCoverageBuilderTest extends TestCase {

  public static class MyObjective extends MethodObjective {

    public double coverage() {
      // TODO Auto-generated method stub
      return 0;
    }

    public double high() {
      // TODO Auto-generated method stub
      return 0;
    }

    public double low() {
      // TODO Auto-generated method stub
      return 0;
    }

    public void reset() {
      // TODO Auto-generated method stub

    }

    public void visit(ObjectiveVisitor vis) {
      // TODO Auto-generated method stub

    }

    public void update(Map codepaths) {
      // TODO Auto-generated method stub

    }

    public void update(int tid, String method, int block) {
      assertEquals(getMethod(), method);
    }

    public int hit() {
      // TODO Auto-generated method stub
      return 0;
    }

  }

  private PackageTreeCoverageBuilder tree;

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ControlGraphBuilder builder = new ControlGraphBuilder() {

      public Map<String, ControlGraph> createGraphsForClass(String cls)
          throws IOException {
        Map<String, ControlGraph> m = new HashMap<String, ControlGraph>();
        m.put(cls + ".one()", new ControlGraph());
        m.put(cls + ".two()", new ControlGraph());
        return m;
      }

      public ControlGraph createGraphForMethod(String method)
          throws IOException {
        // TODO Auto-generated method stub
        return null;
      }

      public ControlGraph createGraphForMethod(String cls, String method,
          String signature) throws IOException {
        // TODO Auto-generated method stub
        return null;
      }

      public Map<String, ControlGraph> createAllGraphs(InputStream is)
          throws IOException {
        // TODO Auto-generated method stub
        return null;
      }

    };
    this.tree = new PackageTreeCoverageBuilder();
    tree.setGraphBuilder(builder);
    tree.setObjectives(new ArrayList<Class>() {
      {
        add(MyObjective.class);
      }
    });
  }

  public void testRoutingToMethod() {
    Set<String> cls = new HashSet<String>();
    cls.add("pack/One");
    cls.add("pack/Two");
    CoverageObjective obj = tree.build(cls);
    // run some tests
    obj.update(1, "pack/One.one()", 1);
    obj.update(1, "pack/Two.one()", 1);
    obj.update(1, "pack/Two.two()", 1);
  }

}
