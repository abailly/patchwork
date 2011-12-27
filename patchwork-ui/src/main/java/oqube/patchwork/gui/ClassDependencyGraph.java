/**
 * 
 */
package oqube.patchwork.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import oqube.bytes.ClassFile;
import oqube.bytes.loading.ClassFileFactory;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.GraphException;
import salvo.jesus.graph.GraphOps;
import salvo.jesus.graph.algorithm.AndGraphFilter;
import salvo.jesus.graph.algorithm.GraphFilter;

/**
 * Store a graph of classes and their dependencies.
 * 
 * @author nono
 * 
 */
public class ClassDependencyGraph extends AbstractDependencyGraph {

  private Map<String, ClassInfo> vertexMap = new HashMap<String, ClassInfo>();

  private List<Pattern>          filteredClasses;

  /**
   * Create a new dependency graph for classes.
   * 
   * @param filterSysClasses
   *            if true, then system classes are filtered out from the graph.
   */
  public ClassDependencyGraph(boolean filterSysClasses) {
    if(filterSysClasses) {
      filteredClasses = new ArrayList<Pattern>() {
        {
          add(Pattern.compile("java/.*"));
          add(Pattern.compile("javax/.*"));
          add(Pattern.compile("com/sun.*"));
          add(Pattern.compile("sun/.*"));
          add(Pattern.compile("sunw/.*"));
          add(Pattern.compile("\\[.*"));
        }
      };
    } else {
      filteredClasses = new ArrayList<Pattern>();
    }
  }

  /**
   * Add a new ClassFile to this graph. The ClassFile object is scanned for
   * ClassData constants which are added to the graph. A map from strings to
   * vertices is maintained.
   * 
   * @param cf
   */
  public void add(ClassFile cf) throws Exception {
    String clname = cf.getClassFileInfo().getName();
    String supname = cf.getClassFileInfo().getParentName();
    Set ifaces = cf.getClassFileInfo().getInterfacesNames();
    DirectedGraph graph = getGraph();
    ClassInfo ci = maybeCreateVertex(cf, clname, graph);
    if(ci == null)
      return;
    /* enumerate class data constants */
    Iterator it = cf.getConstantPool().getAllClassData().iterator();
    while(it.hasNext()) {
      String ocn = (String)it.next();
      /* filter out system classes : java.*, com.sun.*, javax.* */
      if(!filter(ocn) | ocn.equals(clname))
        continue;
      LinkType type = LinkType.uses;
      /* check kind of link */
      if(ocn.equals(supname))
        type = LinkType.inherits;
      else if(ifaces.contains(ocn))
        type = LinkType.implement;
      /* find vertex */
      ClassInfo ov = maybeCreateVertex(null, ocn, graph);
      if(ov == null)
        continue;
      ov.incrementLinkTo();
      ci.incrementLinkFrom();
      // System.err.println("Linking " + v + " to " + ov);
      if(graph.getEdge(ci, ov) == null) {
        final DirectedEdgeImpl potentialEdge = new DirectedEdgeImpl(ci, ov, type);
        if(filter.filter(potentialEdge))
          graph.addEdge(potentialEdge);
      }
    }
  }

  private ClassInfo maybeCreateVertex(ClassFile cf, String clname, DirectedGraph graph) throws GraphException {
    ClassInfo ci = (ClassInfo)vertexMap.get(clname);
    if(ci == null) {
      ci = makeInfo(cf, clname);
      if(filter.filter(ci)) {
        vertexMap.put(clname, ci);
        graph.add(ci);
      } else
        ci = null;
    }
    return ci;
  }

  public static ClassInfo makeInfo(ClassFile cf, String clname) {
    ClassInfo ci = new ClassInfo(clname);
    ci.setDisplayName(simplify(clname));
    ci.setClassFile(cf);
    return ci;
  }

  /*
   * simplify class name by abbreviating package path.
   */
  public static String simplify(String clname) {
    StringBuilder sb = new StringBuilder();
    StringTokenizer st = new StringTokenizer(clname, "/");
    while(st.hasMoreTokens()) {
      String s = st.nextToken();
      if(st.hasMoreTokens())
        sb.append(s.charAt(0)).append('.');
      else
        sb.append(s);
    }
    return sb.toString();
  }

  /**
   * @param ocn
   * @return
   */
  private boolean filter(String ocn) {
    /* apply local filter */
    Iterator<Pattern> it = filteredClasses.iterator();
    while(it.hasNext()) {
      Pattern pat = it.next();
      if(pat.matcher(ocn).matches())
        return false;
    }
    return true;

  }

  /**
   * Add a new filter to this dependency graph. Filters can be added before or
   * after classes.
   * 
   * @param string
   *            pattern for class names that should <b>not</b> be included in
   *            this graph.
   */
  public void addFilter(String string) {
    filteredClasses.add(Pattern.compile(string));
    applyFilters();
  }

  private GraphFilter      filter = new GraphFilter() {

                                    public boolean filter(Edge e) {
                                      return true;
                                    }

                                    public boolean filter(Object v) {
                                      for(Pattern p : filteredClasses) {
                                        if(p.matcher(((ClassInfo)v).getName()).matches())
                                          return false;
                                      }
                                      return true;
                                    }

                                  };

  private ClassFileFactory classFactory;

  private void applyFilters() {
    try {
      setGraph(GraphOps.filter(getGraph(), filter));
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Construct a package dependency graph from this class dependency graph.
   * 
   * @return a graph.
   * @throws Exception
   */
  public PackageDependencyGraph makePackageGraph() throws Exception {
    return new PackageDependencyGraph(this);
  }

  public CallGraph makeCallGraph() {
    CallGraph cg = new CallGraph();
    cg.setFactory(classFactory);
    cg.analyze(this);
    return cg;
  }

  /**
   * Add a list of filters to applys to this graph.
   * 
   * @param filters
   *            a list of filter patterns.
   */
  public void addFilters(List<String> filters) {
    for(String s : filters)
      addFilter(s);
  }

  public void setFactory(ClassFileFactory classFactory) {
    this.classFactory = classFactory;
  }

  public void addFilter(GraphFilter filter) {
    this.filter = new AndGraphFilter(this.filter, filter);
    applyFilters();
  }

  public static String packageName(ClassInfo ci) {
    String pname = ci.getName();
    /* compute package name */
    if(pname.lastIndexOf('/') > -1)
      pname = pname.substring(0, pname.lastIndexOf('/'));
    return pname;
  }

  public static String className(ClassInfo from) {
    String pname = from.getName();
    if(pname.lastIndexOf('/') > -1)
      pname = pname.substring(pname.lastIndexOf('/') + 1);
    return pname;
  }
}