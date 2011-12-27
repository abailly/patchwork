/**
 * 
 */
package oqube.patchwork.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oqube.bytes.instructions.Instruction;
import salvo.jesus.graph.DirectedEdge;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.GraphException;
import salvo.jesus.graph.GraphOps;
import salvo.jesus.graph.algorithm.GraphFilter;
import salvo.jesus.graph.algorithm.GraphVertexFold;

/**
 * This class constructs a graph of instructions from a full control graph where
 * only the matching instructions are present.
 * 
 * @author nono
 */
public class TrimmedGraph implements InstructionMatcher {

	/*
	 * the constructed graph
	 */
	private DirectedGraphImpl graph;

	/*
	 * a list of matchers
	 */
	private List matchers = new ArrayList();

	/**
	 * Default constructor. Construct a trimmed graph without any matcher. Those
	 * must be added using method {@link #addMatcher(InstructionMatcher)}.
	 */
	public TrimmedGraph() {

	}

	/**
	 * Construct a trimmed graph with given matcher.
	 * 
	 * @param matcher
	 *            a non null instruction matcher.
	 */
	public TrimmedGraph(InstructionMatcher matcher) {
		matchers.add(matcher);
	}

	public DirectedGraph makeGraph(ControlGraph cg) throws GraphException {
		DirectedGraph g = completeGraph(cg);
		GraphVertexFold gf = new GraphVertexFold() {

			public boolean fold(Object v) {
				return !(match((Instruction) v) || v == Instruction.START || v == Instruction.END);
			}

			public Object merge(Object i, Object o) {
				return i.toString() + o.toString();
			}

		};
		return GraphOps.fold((DirectedGraphImpl) g, gf);
	}

	/**
	 * Construct a trimmed graph using current matchers.
	 * 
	 * @param cg
	 *            the control graph to trim.
	 * @return a directed graph whose nodes are Instruction instances
	 * @throws GraphException
	 */
	private DirectedGraph trimGraph(ControlGraph cg) throws GraphException {
		// returned grah (nodes == instructions)
		DirectedGraph graph = new DirectedGraphImpl();
		// control graph (nodes == bblocks)
		DirectedGraph cgraph = cg.getGraph();
		/* add start vertex */
		Object start = Instruction.START;
		Object end = Instruction.END;
		BasicBlock bbs = new BasicBlock.StartBlock();
		BasicBlock bbe = new BasicBlock.EndBlock();
		/* map from vertices to blocks */
		Map vmap = new HashMap();
		graph.add(start);
		graph.add(end);
		/* map from bb to in instruction */
		Map binm = new HashMap();
		Map boutm = new HashMap();
		int numb = 1;
		/* iterate over vertices to store references */
		Iterator it = cgraph.getVerticesIterator();
		while (it.hasNext()) {
			BasicBlock bb = (BasicBlock) it.next();
			numb = bb.getNumBlock();
			Object bin = null; /* entry to block */
			Object bout = null; /* exit from block */
			if (bbs.equals(bb)) {
				bin = bout = start;
			} else if (bbe.equals(bb))
				bin = bout = end;
			Iterator it2 = bb.getInstructions();
			while (it2.hasNext()) {
				Instruction ins = (Instruction) it2.next();
				if (match(ins)) {
					/* make vertex */
					Object v = ins;
					graph.add(v);
					vmap.put(ins, v);
					/* link to last use */
					if (bin == null)
						bout = bin = v;
					/* link this vertex */
					if (bout != v) {
						graph.addEdge(new DirectedEdgeImpl(bout, v));
					} else if (bin != v)
						graph.addEdge(new DirectedEdgeImpl(bin, v));
					bout = v;
				}
			}
			/* update block maps */
			if (bout == null)
				bin = bout = "Block " + numb;
			binm.put(bb, bin);
			boutm.put(bb, bout);
		}
		/* add control edges between vertices */
		it = boutm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			BasicBlock bb = (BasicBlock) me.getKey();
			Object nv = me.getValue();
			Iterator it2 = cgraph.getOutgoingEdges(bb).iterator();
			while (it2.hasNext()) {
				DirectedEdge de = (DirectedEdge) it2.next();
				BasicBlock bbf = (BasicBlock) de.getSink();
				Object nvo = binm.get(bbf);
				graph.addEdge(new DirectedEdgeImpl(nv, nvo));
			}
		}
		/* link to start */
		it = cgraph.getAdjacentVertices(cgraph.findVertex(bbs)).iterator();
		while (it.hasNext()) {
			Object v = it.next();
			BasicBlock bb = (BasicBlock) v;
			Object iv = binm.get(bb);
			graph.addEdge(new DirectedEdgeImpl(start, iv));
		}
		return graph;
	}

	/**
	 * Return a directed of all instructions in the given control graph. In
	 * essence, this method replaces each block by its sequence of instructions.
	 * 
	 * @param cg
	 *            the control graph to use
	 * @return a DirectedGraph instance whose nodes are Instruction instances
	 * @throws GraphException
	 */
	public DirectedGraph completeGraph(ControlGraph cg) throws GraphException {
		List old = matchers;
		matchers = new ArrayList();
		matchers.add(InstructionMatcher.TRUE);
		DirectedGraph ret = trimGraph(cg);
		matchers = old;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oqube.patchwork.graph.InstructionMatcher#match(oqube.bytes.Instruction)
	 */
	public boolean match(Instruction ins) {
		for (Iterator i = matchers.iterator(); i.hasNext();) {
			if (!((InstructionMatcher) i.next()).match(ins))
				return false;
		}
		return true;
	}

	/**
	 * Return all matchers in this graph trimmer.
	 * 
	 * @return
	 */
	public List getMatchers() {
		return matchers;
	}

	/**
	 * Add a new matcher to this trimmer.
	 * 
	 * @param m
	 */
	public void addMatcher(InstructionMatcher m) {
		matchers.add(m);
	}
}
