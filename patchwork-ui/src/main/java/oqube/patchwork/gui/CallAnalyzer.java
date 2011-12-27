/**
 * 
 */
package oqube.patchwork.gui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import oqube.bytes.ClassFile;
import oqube.bytes.instructions.FourArgInst;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.instructions.TwoArgInst;
import oqube.bytes.loading.CachedClassFileFactory;
import oqube.bytes.loading.ClassFileFactory;
import oqube.bytes.loading.ClassLoaderClassFileFactory;
import oqube.bytes.pool.AbstractMethodData;
import oqube.bytes.pool.ClassData;
import oqube.bytes.pool.ConstantPool;
import oqube.bytes.pool.InterfaceMethodData;
import oqube.bytes.pool.MethodRefData;
import oqube.bytes.pool.NameAndTypeData;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.CallMatcher;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;
import oqube.patchwork.graph.TrimmedGraph;
import salvo.jesus.graph.DirectedEdge;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;
import salvo.jesus.graph.GraphException;

/**
 * A class that analyzes a java class file and produces a graph of the methods
 * called that match some class criteria. The graph is a simplification of the
 * control graph of the requested classes method where only the method calls of
 * interest appear.
 * 
 * @author nono
 * 
 */
public class CallAnalyzer {

  private TrimmedGraph trimmer = new TrimmedGraph(new CallMatcher());

  private ClassFileFactory factory = new ClassLoaderClassFileFactory();;

  private CachedControlGraphBuilder graphBuilder = new CachedControlGraphBuilder(
      factory);

  // the call graph
  private DirectedGraph callGraph = new DirectedGraphImpl();

  // set of method names in the currently constructed graph
  private Set<MethodStartInfo> methods = new HashSet<MethodStartInfo>();

  /**
   * Construct a call graph for the given class and methods.
   * 
   * @param cls
   *          path to a class within the class path
   * @param method
   *          name of method to analyze
   * @param signature
   *          signature of method to find. may be null if only one method named
   *          <code>method</code> exists in class
   * 
   * @return a directed graph with only calls matching this classe's criteria
   * @throws IOException
   */
  public DirectedGraph analyze(String cls, String method, String signature)
      throws IOException {
    // make control graph
    try {
      ClassFile cf = factory.getClassFileFor(cls);
      MethodStartInfo msi = new MethodStartInfo(cls, method, signature);
      // trim graph
      mergeCallGraph(trim(graphBuilder.createGraphForMethod(cls, method,
          signature)), msi, callGraph);
      // add inheritance links
      linkSuperclasses(method, signature, msi, cf);
      return callGraph;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

  private void linkSuperclasses(String method, String signature,
      MethodStartInfo msi, ClassFile cf) throws IOException, GraphException {
    ClassFileInfo cfi = cf.getClassFileInfo();
    String sclass = cfi.getParentName();
    ClassFile scf = factory.getClassFileFor(sclass);
    if (scf == null)
      return;
    // break recursion
    if (sclass.equals(cfi.getName()))
      return;
    MethodFileInfo mfi = scf.getMethodInfo(method, signature);
    if (mfi != null) {
      MethodStartInfo mfn = new MethodStartInfo(sclass, method, signature);
      mfn.setClassFile(scf);
      if (!methods.contains(mfn))
        methods.add(mfn);
      callGraph.addEdge(new DirectedEdgeImpl(mfn, msi, LinkType.overriden));
    } else
      linkSuperclasses(method, signature, msi, scf);
    linkInterfaces(method, signature, msi, cf);
  }

  private void linkInterfaces(String method, String signature,
      MethodStartInfo msi, ClassFile cf) throws IOException, GraphException {
    ClassFileInfo cfi = cf.getClassFileInfo();
    // lookup in interfaces
    for (String n : cfi.getInterfacesNames()) {
      ClassFile scf = factory.getClassFileFor(n);
      if (scf == null)
        continue;
      MethodFileInfo mfi = scf.getMethodInfo(method, signature);
      if (mfi != null) {
        MethodStartInfo mfn = new MethodStartInfo(n, method, signature);
        mfn.setClassFile(scf);
        if (!methods.contains(mfn))
          methods.add(mfn);
        callGraph.addEdge(new DirectedEdgeImpl(mfn, msi, LinkType.implemented));
      } else {
        linkInterfaces(method, signature, msi, scf);
      }
    }
  }

  /**
   * This method trims a control graph such it produces a graph where only some
   * instructions remain, according to some filter.
   * 
   * @throws GraphException
   */
  private DirectedGraph trim(ControlGraph cg) throws GraphException {
    DirectedGraph makeGraph = trimmer.makeGraph(cg);
    return makeGraph;
  }

  /**
   * @return Returns the trimmer.
   */
  public TrimmedGraph getTrimmer() {
    return trimmer;
  }

  /**
   * @param trimmer
   *          The trimmer to set.
   */
  public void setTrimmer(TrimmedGraph trimmer) {
    this.trimmer = trimmer;
  }

  /**
   * Merge the call graph for one method into the global graph this object
   * represents. This method tranform nodes in the input graph to
   * {@link MethodInfo} objects representing start and call sites. It then link
   * the new nodes to old nodes, such that:
   * <ul>
   * <li>call sites in the new graph are linked to method start in the old
   * graph</li>
   * <li>call sites in the old graph that use the new graphs method are linked
   * to this graph start node.</li>
   * </ul>
   * 
   * @param callg
   *          the call graph
   * @param msi
   *          the method's name. This is the fully qualified name, containing
   *          class, method and signature.
   * @param ret
   *          the graph to update
   * @throws GraphException
   */
  private void mergeCallGraph(DirectedGraph callg, MethodStartInfo msi,
      DirectedGraph ret) throws GraphException {
    MethodStartInfo msi1 = (MethodStartInfo) ret.findVertex(msi);
    if (msi1 != null) {
      msi1.setClassFile(msi.getClassFile());
      msi = msi1;
    }
    methods.add(msi);
    /* make map from old to new vertices */
    final Map<Instruction, MethodInfo> m = new HashMap<Instruction, MethodInfo>();
    final Map<MethodInfo, Instruction> im = new HashMap<MethodInfo, Instruction>();
    for (Iterator i = callg.getVerticesIterator(); i.hasNext();) {
      Instruction v = (Instruction) i.next();
      // skip end of graph
      if (v == Instruction.END)
        continue;
      if (v == Instruction.START) {
        m.put(v, msi);
        im.put(msi, v);
        continue;
      }
      // create a node
      MethodStartInfo mci = callee(v);
      assert mci != null;
      MethodCallInfo call = new MethodCallInfo(msi, Integer.toString(v.getPc()));
      ret.add(call);
      m.put(v, call);
      im.put(call, v);
      /* link caller to callee */
      ret.addEdge(new DirectedEdgeImpl(call, mci, LinkType.calls));
    }
    /* add all edges in this graph */
    for (Iterator i = callg.getAllEdges().iterator(); i.hasNext();) {
      DirectedEdge de = (DirectedEdge) i.next();
      Object os = de.getSource();
      Object oe = de.getSink();
      if (oe.equals(Instruction.END))
        continue;
      ret.addEdge(new DirectedEdgeImpl(m.get(os), m.get(oe), LinkType.follows));
    }
  }

  /*
   * reformat the control graph instruction or block node This code is really
   * ugly, we should use a visitor if there was one in the bet package.
   */
  private MethodStartInfo callee(Instruction object) {
    String c = null;
    String n = null;
    String s = null;
    ConstantPool constantPool = ((Instruction) object).getClassFile()
        .getConstantPool();
    AbstractMethodData mr = null;
    if (object instanceof TwoArgInst) { /* invoke method */
      TwoArgInst inst = (TwoArgInst) object;
      byte[] args = null;
      args = inst.args();
      mr = (MethodRefData) constantPool
          .getEntry((short) ((args[0] << 8) | ((int) (args[1] & 0xff))));
    } else if (object instanceof FourArgInst) { /* invoke interface */
      FourArgInst inst = (FourArgInst) object;
      byte[] args = null;
      args = inst.args();
      mr = (InterfaceMethodData) constantPool
          .getEntry((short) ((args[0] << 8) | ((int) (args[1] & 0xff))));
    } else
      return null;
    ClassData cd = (ClassData) constantPool.getEntry(mr.getClassIndex());
    c = cd.toString();
    NameAndTypeData nt = (NameAndTypeData) constantPool.getEntry(mr
        .getNameAndTypeIndex());
    n = nt.getName();
    s = nt.getType();
    MethodStartInfo msi = new MethodStartInfo(c, n, s);
    MethodStartInfo msi1 = (MethodStartInfo) callGraph.findVertex(msi);
    if (msi1 != null) {
      return msi1;
    } else {
      try {
        callGraph.add(msi);
        methods.add(msi);
      } catch (GraphException e) {
        e.printStackTrace();
      }
      return msi;
    }
  }

  public ClassFileFactory getFactory() {
    return factory;
  }

  public void setFactory(ClassFileFactory factory) {
    this.factory = factory;
    this.graphBuilder.setFactory(factory);
  }

  /**
   * Reinitializes this analyzer's graph.
   * 
   */
  public void reset() {
    this.callGraph = new DirectedGraphImpl();
    this.methods = new HashSet<MethodStartInfo>();
  }

  /**
   * @return the graphBuilder
   */
  public CachedControlGraphBuilder getGraphBuilder() {
    return graphBuilder;
  }

  /**
   * @param graphBuilder
   *          the graphBuilder to set
   */
  public void setGraphBuilder(CachedControlGraphBuilder graphBuilder) {
    this.graphBuilder = graphBuilder;
  }

  /**
   * Analyze all methods of given class.
   * 
   * @param klass the class name
   * @return directed call graph with analysis of all methods from given klass.
   * @throws IOException 
   */
  public DirectedGraph analyze(String klass) throws IOException {
    DirectedGraph dg = callGraph;
    ClassFile cf = factory.getClassFileFor(klass);
    ClassFileInfo cfi = cf.getClassFileInfo();
    for (MethodFileInfo mfi : cf.getAllMethods()) {
      try {
        dg = analyze(cfi.getName(), mfi.getName(), mfi
            .getSignature());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return callGraph;
  }
}
