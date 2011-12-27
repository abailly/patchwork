/**
 * 
 */
package oqube.patchwork.gui;

import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.loading.ClassFileFactory;
import oqube.bytes.struct.ClassFileInfo;
import oqube.bytes.struct.MethodFileInfo;
import salvo.jesus.graph.DirectedGraph;

/**
 * A call graph stores all link between methods in a set of classes.
 * 
 * @author nono
 */
public class CallGraph extends AbstractDependencyGraph {

	private CallAnalyzer analyzer;

	private ClassFileFactory classFactory;

	public CallGraph() {
		this.analyzer = new CallAnalyzer();
	}

	public void analyze(ClassDependencyGraph classes) {
		this.analyzer.reset();
		this.analyzer.setFactory(classFactory);
		DirectedGraph dg = null;
		for (Object node : classes.getGraph().getAllVertices()) {
			ClassInfo ci = (ClassInfo) node;
			if (ci.getClassFile() != null) {
				ClassFile cf = ci.getClassFile();
				ClassFileInfo cfi = cf.getClassFileInfo();
				for (MethodFileInfo mfi : cf.getAllMethods()) {
					try {
						dg = analyzer.analyze(cfi.getName(), mfi.getName(), mfi
								.getSignature());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		setGraph(dg);
	}

	public void setFactory(ClassFileFactory classFactory) {
		this.classFactory = classFactory;
	}
}
