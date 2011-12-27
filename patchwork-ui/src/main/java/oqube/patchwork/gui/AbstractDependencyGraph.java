/**
 * 
 */
package oqube.patchwork.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;
import salvo.jesus.graph.algorithm.AdjacencyMatrix;
import salvo.jesus.graph.algorithm.Distance;
import salvo.jesus.graph.algorithm.GraphMatrix;

/**
 * Basic (graph-based) container for dependency graphs.
 * 
 * @author nono
 * 
 */
public class AbstractDependencyGraph {

	private DirectedGraph graph = new DirectedGraphImpl();

	private Map<String, Object> infomap;

	public DirectedGraph getGraph() {
		return graph;
	}

	public Map<String, Object> getInfomap() {
		if (infomap == null)
			infomap = makeInfomap();
		return infomap;
	}

	private Map<String, Object> makeInfomap() {
		Map<String, Object> m = new LinkedHashMap<String, Object>() {
			{
				if (graph != null) {
					GraphMatrix gm = new AdjacencyMatrix(graph);
					Distance d = new Distance(gm);
					put("nodes", graph.getAllVertices().size());
					put("edges", graph.getAllEdges().size());
					put("diameter", d.diameter());
					put("radius", d.radius());
				}
			}
		};
		return m;
	}

	public final void setGraph(DirectedGraph graph) {
		this.graph = graph;
	}

}
