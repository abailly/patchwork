package oqube.patchwork.graph;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.Graph;
import salvo.jesus.graph.GraphException;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.layout.DigraphLayeredLayout;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingLayout;
import salvo.jesus.graph.visual.print.VisualGraphImageOutput;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.struct.MethodFileInfo;
import junit.framework.TestCase;

public class TrimmedGraphTest extends TestCase {

	private ControlGraph cg;

	private TrimmedGraph trim;

	private DirectedGraph graph;

	protected void setUp() throws Exception {
		super.setUp();
		/* create classfile object */
		ClassFile cf = new ClassFile();
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"oqube/patchwork/graph/TrimmedGraph.class");
		cf.read(new DataInputStream(is));
		MethodFileInfo mfi = (MethodFileInfo) cf.getMethodInfo("makeGraph")
				.iterator().next();
		cg = new ControlGraph(mfi);
		trim = new TrimmedGraph();
	}

	public void test01NotEmpty() throws GraphException, IOException {
		// match any call
		trim.addMatcher(new CallMatcher());
		graph = trim.makeGraph(cg);
		System.err.println(graph);
		assertTrue("Graph is empty", !graph.getAllVertices().isEmpty());
		assertNotNull(graph);
	}

	public void test02CompleteGraph() throws GraphException, IOException {
		graph = trim.makeGraph(cg);
		assertTrue("Graph is empty", !graph.getAllVertices().isEmpty());
		assertNotNull(graph);
	}

	public void tesStructuralCorrectnessOfStartAndEnd() throws GraphException,
			IOException {
		graph = trim.makeGraph(cg);
		assertTrue("Graph is empty", !graph.getAllVertices().isEmpty());
		// check no incoming edges to start
		List l = graph.getIncomingEdges(Instruction.START);
		assertTrue("incoming edges to start node:" + l, l.isEmpty());
		l = graph.getOutgoingEdges(Instruction.END);
		assertTrue("outgoing edges from end node:" + l, l.isEmpty());
	}

}
