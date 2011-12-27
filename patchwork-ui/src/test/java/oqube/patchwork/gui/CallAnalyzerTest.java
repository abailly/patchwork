package oqube.patchwork.gui;

import java.io.IOException;

import junit.framework.TestCase;
import oqube.bytes.loading.ClassLoaderClassFileFactory;
import salvo.jesus.graph.DirectedEdge;
import salvo.jesus.graph.DirectedGraph;

public class CallAnalyzerTest extends TestCase {

  static class Class1 {
    void m1(int i) {
      if (i % 3 == 0)
        m2();
      else if (i % 3 == 1)
        m3();
      else
        m1(i--);
    }

    private void m3() {
      m2();
    }

    private void m2() {
      System.err.println("toto");
    }
  }

  static class Class2 {
    Class1 c1 = new Class1();

    void mc1(String s) {
      if (s == null)
        c1.m1(0);
      else
        c1.m1(2);
    }
  }

  static class Class3 extends Class1 {

    void m1(int i) {
      super.m1(i);
    }

    Interface1 i1;

    void m2() {
      System.err.println(i1.mi1());
      System.err.println(i1.toString());
    }
  }

  static interface Interface1 {
    String mi1();
  }

  static class Interface1Impl implements Interface1 {
    public String mi1() {
      return toString();
    }

    public String toString() {
      return "";
    }
  }

  static class Class4 extends Class3 {
    void m3() {
      System.err.println("toto");
    }
  }

  static interface Interface2 extends Interface1 {
    void m2();
  }

  static interface Interface3 extends Interface1 {
    void m3();
  }

  static interface Interface21 extends Interface2, Interface3 {
    void m4();
  }

  static class Class5 implements Interface21 {

    public void m4() {
      // TODO Auto-generated method stub

    }

    public void m2() {
      // TODO Auto-generated method stub

    }

    public String mi1() {
      // TODO Auto-generated method stub
      return null;
    }

    public void m3() {
      // TODO Auto-generated method stub

    }

  }

  public void testSingleMethodAnalysis() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    assertEquals("wrong number of nodes", 6, dg.getVerticesCount());
    assertEquals("wrong number of edges", 6, dg.getEdgesCount());
  }

  public void testMultipleMethodAnalysis() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m2", "()V");
    assertEquals("wrong number of nodes", 9, dg.getVerticesCount());
    assertEquals("wrong number of edges", 10, dg.getEdgesCount());
  }

  public void testMultipleClassesAnalysis() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m2", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class2", "mc1",
        "(Ljava/lang/String;)V");
    assertEquals("wrong number of nodes", 12, dg.getVerticesCount());
    assertEquals("wrong number of edges", 14, dg.getEdgesCount());
    // check some edge
    MethodStartInfo msi = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class2", "mc1",
        "(Ljava/lang/String;)V");
    MethodCallInfo mci = new MethodCallInfo(msi, "9");
    assertTrue("should have start vertex for metho m2",
        dg.findVertex(msi) != null);
    LinkType type = LinkType.follows;
    assertEdge(dg, msi, mci, type);
  }

  /**
   * @param dg
   * @param msi
   * @param mci
   * @param type
   */
  private void assertEdge(DirectedGraph dg, Object msi, Object mci, Object type) {
    boolean foundedge = false;
    for (DirectedEdge de : dg.getOutgoingEdges(msi)) {
      if (de.getData().equals(type) && de.getSink().equals(mci))
        foundedge = true;
    }
    assertTrue("did not find expected edge", foundedge);
  }

  public void testClassesAndInterfacesWithMethodDefinitions()
      throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class3", "m2", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl",
        "mi1", "()Ljava/lang/String;");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl",
        "toString", "()Ljava/lang/String;");
    assertEquals("wrong number of nodes", 12, dg.getVerticesCount());
    assertEquals("wrong number of edges", 13, dg.getEdgesCount());
    assertEdge(dg, new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1", "mi1",
        "()Ljava/lang/String;"), new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl", "mi1",
        "()Ljava/lang/String;"), LinkType.implemented);
    assertEdge(dg, new MethodStartInfo("java/lang/Object", "toString",
        "()Ljava/lang/String;"), new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl", "toString",
        "()Ljava/lang/String;"), LinkType.overriden);
  }

  public void testClassesAndInterfacesWithMethodOverrideInDifferentOrder()
      throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl", "mi1",
        "()Ljava/lang/String;");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl",
        "toString", "()Ljava/lang/String;");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class3", "m2", "()V");
    assertEquals("wrong number of nodes", 12, dg.getVerticesCount());
    assertEquals("wrong number of edges", 13, dg.getEdgesCount());
    assertEdge(dg, new MethodStartInfo("java/lang/Object", "toString",
        "()Ljava/lang/String;"), new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1Impl", "toString",
        "()Ljava/lang/String;"), LinkType.overriden);
  }

  public void testResetAndSetFactory() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    ca.reset();
    ca.setFactory(new ClassLoaderClassFileFactory());
    dg = ca
        .analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m2", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class2", "mc1",
        "(Ljava/lang/String;)V");
    assertEquals("wrong number of nodes", 12, dg.getVerticesCount());
    assertEquals("wrong number of edges", 14, dg.getEdgesCount());
  }

  public void testNestedInheritanceHierarchy() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca.analyze(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class1", "m2", "()V");
    dg = ca
        .analyze("oqube/patchwork/gui/CallAnalyzerTest$Class3", "m1", "(I)V");
    dg = ca.analyze("oqube/patchwork/gui/CallAnalyzerTest$Class4", "m3", "()V");
    MethodStartInfo msi = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m3", "()V");
    MethodStartInfo msi1 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class1", "m1", "(I)V");
    MethodStartInfo msi3 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class3", "m1", "(I)V");
    MethodStartInfo msi4 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class4", "m3", "()V");
    assertEdge(dg, msi1, msi3, LinkType.overriden);
    assertEdge(dg, msi, msi4, LinkType.overriden);
  }

  public void testNestedInheritanceWithInterfacesHierarchy() throws IOException {
    CallAnalyzer ca = new CallAnalyzer();
    DirectedGraph dg = ca
        .analyze("oqube/patchwork/gui/CallAnalyzerTest$Class5");
    MethodStartInfo msi = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface1", "mi1",
        "()Ljava/lang/String;");
    MethodStartInfo msi1 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface2", "m2", "()V");
    MethodStartInfo msi3 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface3", "m3", "()V");
    MethodStartInfo msi4 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Interface21", "m4", "()V");
    MethodStartInfo ms = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class5", "mi1",
        "()Ljava/lang/String;");
    MethodStartInfo ms1 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class5", "m2", "()V");
    MethodStartInfo ms3 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class5", "m3", "()V");
    MethodStartInfo ms4 = new MethodStartInfo(
        "oqube/patchwork/gui/CallAnalyzerTest$Class5", "m4", "()V");
    assertEdge(dg, msi, ms, LinkType.implemented);
    assertEdge(dg, msi1, ms1, LinkType.implemented);
    assertEdge(dg, msi3, ms3, LinkType.implemented);
    assertEdge(dg, msi4, ms4, LinkType.implemented);
  }

}
