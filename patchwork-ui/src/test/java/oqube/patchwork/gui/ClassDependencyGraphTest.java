package oqube.patchwork.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import oqube.bytes.ClassFile;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.algorithm.GraphFilter;

public class ClassDependencyGraphTest extends TestCase {

  GraphFilter removeLinkToJunit = new GraphFilter() {

                                  public boolean filter(Edge arg0) {
                                    return !((ClassInfo)arg0.getVertexB()).getName().contains("junit/framework");
                                  }

                                  public boolean filter(Object arg0) {
                                    return true;
                                  }
                                };

  GraphFilter removeJunit       = new GraphFilter() {

                                  public boolean filter(Edge arg0) {
                                    return true;
                                  }

                                  public boolean filter(Object arg0) {
                                    return !((ClassInfo)arg0).getName().equals("junit/framework/TestCase");
                                  }
                                };

  public void testFilterSystemClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    // check system class
    assertTrue("should not contain java/lang/Object", !cg.getGraph().getAllVertices().contains(new ClassInfo("java/lang/Object")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraphTest", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraphTest")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraph", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraph")));
  }

  public void testUnfilterSystemClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(false);
    ClassFile cf = ClassFile.reify(ClassDependencyGraph.class);
    cg.add(cf);
    // check system class
    assertTrue("should contain java/util/List", cg.getGraph().getAllVertices().contains(new ClassInfo("java/util/List")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraph", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraph")));
  }

  public void testFilterPatterns() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    cg.addFilter("junit/.*");
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    assertTrue("should not contain junit/framework/TestCase", !cg.getGraph().getAllVertices().contains(new ClassInfo("junit/framework/TestCase")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraphTest", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraphTest")));

  }

  public void testAddEdgeFilterToDependencyGraph() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    cg.addFilter(removeLinkToJunit);
    final Collection<Edge> edges = cg.getGraph().getAllEdges();
    DependencyGraphTestHelpers.assertNoEdgeFromTo(edges, "oqube/patchwork/gui/ClassDependencyGraphTest", "junit/framework/TestCase");
  }

  public void testAddEdgeFilterToDependencyGraphBeforeAddingClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.addFilter(removeLinkToJunit);
    cg.add(cf);
    final Collection<Edge> edges = cg.getGraph().getAllEdges();
    DependencyGraphTestHelpers.assertNoEdgeFromTo(edges, "oqube/patchwork/gui/ClassDependencyGraphTest", "junit/framework/TestCase");
  }

  public void testAddVertexFilterToDependencyGraphBeforeAddingClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(TestCase.class);
    cg.addFilter(removeJunit);
    cg.add(cf);
    assertTrue("should be empty", cg.getGraph().getAllVertices().isEmpty());
  }

  public void testAddVertexFilterToDependencyGraphBeforeAddingClassesAppliesToDependentClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.addFilter(removeJunit);
    cg.add(cf);
    assertTrue("should not contain junit/framework/TestCase", !cg.getGraph().getAllVertices().contains(new ClassInfo("junit/framework/TestCase")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraph", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraph")));
  }

  public void testFilterAddedAfterClass() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    cg.addFilter("junit/.*");
    assertTrue("should not contain junit/framework/TestCase", !cg.getGraph().getAllVertices().contains(new ClassInfo("junit/framework/TestCase")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraphTest", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraphTest")));

  }

  public void testAddMultipleFilters() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    cg.addFilters(new ArrayList<String>() {
      {
        add("junit/.*");
        add("oqube/bytes/.*");
      }
    });
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    assertTrue("should not contain junit/framework/TestCase", !cg.getGraph().getAllVertices().contains(new ClassInfo("junit/framework/TestCase")));
    assertTrue("should not contain oqube/bytes/ClassFIle", !cg.getGraph().getAllVertices().contains(new ClassInfo("oqube/bytes/ClassFile")));
    assertTrue("should contain oqube/patchwork/gui/ClassDependencyGraphTest", cg.getGraph().getAllVertices().contains(
        new ClassInfo("oqube/patchwork/gui/ClassDependencyGraphTest")));
  }

  public void testExtractPackageGraph() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    // extract package graph
    AbstractDependencyGraph pg = cg.makePackageGraph();
    assertTrue("should not contain java/lang ", !pg.getGraph().getAllVertices().contains(new PackageInfo("java/lang")));
    assertTrue("should contain oqube/patchwork/gui", pg.getGraph().getAllVertices().contains(new PackageInfo("oqube/patchwork/gui")));
  }

  public void testReferenceInfoOnClasses() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    List<ClassInfo> vertices = cg.getGraph().getAllVertices();
    ClassInfo ci = vertices.get(vertices.indexOf(new ClassInfo("oqube/bytes/ClassFile")));
    assertTrue("should be reference node", ci.isReference());
    ci = vertices.get(vertices.indexOf(new ClassInfo("oqube/patchwork/gui/ClassDependencyGraphTest")));
    assertTrue("should not be reference node", !ci.isReference());
  }

  public void testReferenceInfoOnPackages() throws Exception {
    ClassDependencyGraph cg = new ClassDependencyGraph(true);
    ClassFile cf = ClassFile.reify(ClassDependencyGraphTest.class);
    cg.add(cf);
    // extract package graph
    AbstractDependencyGraph pg = cg.makePackageGraph();
    List<PackageInfo> vertices = pg.getGraph().getAllVertices();
    PackageInfo ci = vertices.get(vertices.indexOf(new PackageInfo("oqube/bytes")));
    assertTrue("should be reference node", ci.isReference());
    ci = vertices.get(vertices.indexOf(new PackageInfo("oqube/patchwork/gui")));
    assertTrue("should not be reference node", !ci.isReference());
  }
}
