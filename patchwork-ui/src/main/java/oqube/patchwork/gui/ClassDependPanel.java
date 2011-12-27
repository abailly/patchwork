/**
 * 
 */
package oqube.patchwork.gui;

import java.awt.Graphics;

import salvo.jesus.graph.Graph;
import salvo.jesus.graph.visual.DefaultGraphPanel;
import salvo.jesus.graph.visual.VisualGraph;

/**
 * A graph panel with additional info displayed. This graph panel displays
 * statistical information about the currently displayed graph within a small
 * window hovering on the main panel.
 * 
 * @author nono
 * 
 */
public class ClassDependPanel extends DefaultGraphPanel {

	private GraphInfo graphInfo = new GraphInfo();

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		graphInfo.paint(g);
	}

	public GraphInfo getGraphInfo() {
		return graphInfo;
	}

	public void setGraphInfo(GraphInfo graphInfo) {
		this.graphInfo = graphInfo;
	}

}
