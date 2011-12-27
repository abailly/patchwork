/**
 * 
 */
package oqube.patchwork.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import fr.lifl.utils.ExtensionFileFilter;

import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.visual.GraphScrollPane;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.ZoomUI;
import salvo.jesus.graph.visual.layout.GraphLayoutManager;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingLayout;
import salvo.jesus.graph.visual.print.VisualGraphImageOutput;

/**
 * Base class for windows displaying graphs. Automatically add a toolbar with
 * zoom UI, graph panel.
 * 
 * @author nono
 * 
 */
public abstract class GraphWindow extends GraphScrollPane {

  protected GraphLayoutManager layout;

  protected DirectedGraph graph;

  protected JFrame frame;

  private int maxZoom = 4096;

  private int minZoom = 1;

  private int zoomFactor = 1024;

  protected void initUI() {
    frame = new JFrame("Classgraph");
    Container cont = frame.getContentPane();
    /* add toolbar */
    JToolBar tool = new JToolBar();
    tool.add(new ZoomUI(this.gpanel));
    cont.add(tool, BorderLayout.NORTH);
    cont.add(this, BorderLayout.CENTER);
    /* position frame */
    Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    frame.pack();
    frame
        .setBounds(new Rectangle(100, 100, screen.width / 2, screen.height / 2));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  protected void zoomIn() {
    if (zoomFactor < maxZoom) {
      zoomFactor = zoomFactor * 2;
      gpanel.setZoomFactor(((double) zoomFactor) / (double) 1024);
    }
  }

  protected void zoomOut() {
    if (zoomFactor > minZoom) {
      zoomFactor = zoomFactor / 2;
      gpanel.setZoomFactor(((double) zoomFactor) / (double) 1024);
    }
  }

  protected void initLayout() {
    /* stop previous l ayout */
    if (layout != null)
      layout.layout();
    SimulatedAnnealingLayout layout = new SimulatedAnnealingLayout(
        getVisualGraph(), true);
    layout.setTemperature(100);
    layout.setCoolFactor(0.002);
    layout.setAttractiveForce(10);
    layout.setExpectedDist(200);
    // layout.setRandomMove(0.02);
    layout.setRepaint(true);
    setGraphLayoutManager(layout);
    layout.layout();
    this.layout = layout;
  }

  public ActionListener exportAction() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Save graph");
        chooser.setMultiSelectionEnabled(false);
        ExtensionFileFilter filter = new ExtensionFileFilter();
        filter.addExtension("png");
        filter.addExtension("eps");
        filter.setDescription("Graphic files");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          file = chooser.getSelectedFile();
        } else
          return;
        /* render layout to file */
        VisualGraph vg = getVisualGraph();
        try {
          VisualGraphImageOutput out = new VisualGraphImageOutput();
          String format = "eps";
          if (file.getName().lastIndexOf('.') > 0) {
            format = file.getName().substring(
                file.getName().lastIndexOf('.') + 1);
          }
          out.setFormat(format);
          FileOutputStream fos = new FileOutputStream(file);
          out.output(vg, fos);
          fos.flush();
          fos.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    };
  }

  /**
   * Return an action listener for zooming out.
   * 
   * @return
   */
  public ActionListener zoomOutAction() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomOut();
        repaint();
      }
    };
  }

  /**
   * Return an action listener for zooming in.
   * 
   * @return
   */
  public ActionListener zoomInAction() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomIn();
        repaint();
      }
    };
  }

public void display() {
    frame.setVisible(true);
  }

}
