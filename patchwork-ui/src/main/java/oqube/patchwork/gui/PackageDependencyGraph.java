/**
 * 
 */
package oqube.patchwork.gui;

import java.util.HashMap;
import java.util.Map;

import salvo.jesus.graph.Edge;
import salvo.jesus.graph.Graph;
import salvo.jesus.graph.GraphFactory;
import salvo.jesus.graph.GraphOps;
import salvo.jesus.graph.algorithm.GraphMorphism;

/**
 * A class that represents a graph of dependencies where nodes are packages.
 * 
 * @author nono
 * 
 */
public class PackageDependencyGraph extends AbstractDependencyGraph {
	
	public static final GraphMorphism packageMorphism = new GraphMorphism() {

		GraphFactory fact;

		/* map from package name to vertex */
		Map<String, PackageInfo> packmap;

		public void target(Graph target) {
			this.fact = target.getGraphFactory();
			this.packmap = new HashMap<String, PackageInfo>();
		}

		/*
		 * map class vertices to their corresponding package vertex
		 */
		public Object image(Object v) {
			ClassInfo ci = (ClassInfo) v;
			String pname = ClassDependencyGraph.packageName(ci);
			PackageInfo pi = packmap.get(pname);
			if (pi == null) {
				pi = new PackageInfo(pname);
				packmap.put(pname, pi);
			}
			pi.addClasses(ci);
			return pi;
		}

		public Edge imageOf(Edge e) {
			Object s = image(e.getVertexA());
			Object t = image(e.getVertexB());
			/* return a new edge arrow */
			return fact.createEdgeWith(s, t, e.getData());
		}
	};

	/**
	 * Construct a package dependency from a class dependency graph. Applies
	 * {@link #packageMorphism} as an homomorphism on the undelygin graph of
	 * <code>graph2</code>.
	 * 
	 * @param graph2
	 *            may not be null.
	 * @throws Exception
	 */
	public PackageDependencyGraph(ClassDependencyGraph graph2) throws Exception {
		setGraph(GraphOps.morph(graph2.getGraph(), packageMorphism));
	}

	@Override
	public Map<String, Object> getInfomap() {
		return super.getInfomap();
	}

}
