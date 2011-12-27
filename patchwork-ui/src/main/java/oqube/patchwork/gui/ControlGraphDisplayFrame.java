/*
 * Created on May 9, 2004
 * 
 * $Log: ControlGraphDisplayFrame.java,v $
 * Revision 1.4  2004/10/15 13:32:47  bailly
 * added names in instructions such as getfield, cast, invoke,...
 * corrected several bugs in control graph display (handling of jsr)
 * May break some dependencies due to API modification of Instruction
 *
 * Revision 1.3  2004/08/30 21:06:07  bailly
 * cleaned imports
 *
 * Revision 1.2  2004/05/18 12:57:39  bailly
 * correction graphe de controle
 * ajout graphe de flot de donnees
 *
 * Revision 1.1  2004/05/09 22:09:24  bailly
 * Added frame display capability to ControlGraph display
 * Corrected control graph construction
 * added exceptions block handling, tableswitch and lookupswitch
 * TODO : correct implementation of branch splitting and ordering of blocks
 *
 */
package oqube.patchwork.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.CachedControlGraphBuilder;
import oqube.patchwork.graph.ControlGraph;
import oqube.patchwork.graph.ControlGraphBuilder;
import oqube.patchwork.graph.DataFlowGraph;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.Graph;
import salvo.jesus.graph.algorithm.TarjanSCC;
import salvo.jesus.graph.visual.GraphScrollPane;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.layout.DigraphLayeredLayout;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingLayout;
import salvo.jesus.graph.visual.print.VisualGraphPrinter;
import fr.lifl.utils.ExtensionFileFilter;

/**
 * @author nono
 * @version $Id: ControlGraphDisplayFrame.java,v 1.4 2004/10/15 13:32:47 bailly
 *          Exp $
 */
public class ControlGraphDisplayFrame extends GraphWindow  {

  private JMenu selectMenu;

  private boolean collapsed;

  private Graph colGraph;

  private ControlGraph control;

  private ControlGraphBuilder graphBuilder = new CachedControlGraphBuilder();

  public ControlGraphDisplayFrame(ControlGraph dg) {
    graph = dg.getGraph();
    control = dg;
    setVisualGraph(new VisualGraph(graph));
    initUI();
    frame.setJMenuBar(makeMenuBar());
    initDGLayout();
  }

  private void initDGLayout() {
    if (layout != null)
      layout.layout();
    SimulatedAnnealingLayout layout = new SimulatedAnnealingLayout(
        getVisualGraph(), true);
    layout.setTemperature(150);
    layout.setCoolFactor(0.001);
    layout.setAttractiveForce(10);
    layout.setExpectedDist(100);
    // layout.setRandomMove(0.02);
    layout.setRepaint(true);
    setGraphLayoutManager(layout);
    layout.layout();
  }

  /**
   * @return
   */
  JMenuBar makeMenuBar() {
    JMenuBar menubar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menubar.add(menu);
    JMenuItem item = new JMenuItem("Print");
    menu.add(item);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        new VisualGraphPrinter(getVisualGraph()).showPrint(getX(), getY());
      }

    });
    item = new JMenuItem("Load");
    menu.add(item);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Load class file");
        chooser.setMultiSelectionEnabled(false);
        ExtensionFileFilter filter = new ExtensionFileFilter();
        filter.addExtension("class");
        filter.setDescription("byte-compiled files");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          file = chooser.getSelectedFile();
        } else
          return;
        /* load file and update edit menu */
        try {
          loadClassFile(file);
        } catch (Exception e1) {
          e1.printStackTrace();
          JOptionPane.showMessageDialog(frame, "Error in loading classfile "
              + file + " : " + e1.getMessage());
        }
      }

    });

    item = new JMenuItem("Export");
    item.addActionListener(exportAction());

    menu.add(item);
    item = new JMenuItem("Quit");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    menu.add(item);
    menu = new JMenu("Edit");
    item = new JMenu("Select");
    item.setEnabled(false);
    menu.add(item);
    selectMenu = (JMenu) item;
    item = new JCheckBoxMenuItem("Collapsed", collapsed);
    item.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        switch (e.getStateChange()) {
        case ItemEvent.SELECTED:
          collapsed = true;
          try {
            foldGraph();
          } catch (Exception e1) {
            JOptionPane.showMessageDialog(frame, "Cannot compute SCC graph :"
                + e1);
          }
          break;
        case ItemEvent.DESELECTED:
          collapsed = false;
          unfoldGraph();
          break;
        }

      }
    });
    menu.add(item);
    item = new JMenuItem("DataFlow graph");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        /* compute dataflow graph */
        DataFlowGraph dfg;
        try {
          dfg = new DataFlowGraph(control, 256);
        } catch (Exception e1) {
          e1.printStackTrace();
          JOptionPane.showMessageDialog(frame,
              "Cannot compute data flow graph :" + e1);
          return;
        }
        setGraph(dfg.getGraph());
        initDGLayout();
      }
    });
    menu.add(item);
    item = new JMenu("Zoom");
    JMenuItem item2 = new JMenuItem("in");
    item2.addActionListener(zoomInAction());
    item.add(item2);
    item2 = new JMenuItem("out");
    item2.addActionListener(zoomOutAction());
    item.add(item2);
    menu.add(item);
    menubar.add(menu);

    return menubar;
  }

  class SelectMethodListener implements ActionListener {

    private DirectedGraph graph;

    SelectMethodListener(DirectedGraph g) {
      this.graph = g;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisualGraph(new VisualGraph(graph));
      initLayout();
    }

  }

  /**
   * @param file
   * @throws IOException
   */
  protected void loadClassFile(File file) throws IOException {
    /* read class file */
    Map graphs = graphBuilder.createAllGraphs(new FileInputStream(file));
    /* update edit menu */
    selectMenu.removeAll();
    Iterator it = graphs.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      String mname = (String) entry.getKey();
      ControlGraph cgraph = (ControlGraph) entry.getValue();
      /* update menuitem */
      JMenuItem item = new JMenuItem(mname);
      item.addActionListener(new SelectMethodListener((DirectedGraph) cgraph
          .getGraph()));
      selectMenu.add(item);
    }
    selectMenu.setEnabled(true);
  }

  /**
   * 
   */
  protected void foldGraph() throws Exception {
    if (this.colGraph == null) {
      TarjanSCC scc = new TarjanSCC(graph);
      this.colGraph = scc.makeCollapsedSCC();
    }
    setGraph(this.colGraph);
    initLayout();
  }

  /**
   * 
   */
  protected void unfoldGraph() {
    setGraph(this.graph);
    initLayout();

  }

}