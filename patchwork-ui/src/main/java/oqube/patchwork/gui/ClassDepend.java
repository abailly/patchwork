/*
 * Created on Mar 24, 2004
 * $Log: ClassDepend.java,v $
 * Revision 1.2  2004/08/30 21:06:07  bailly
 * cleaned imports
 *
 * Revision 1.1  2004/06/24 14:05:34  bailly
 * integration visualiseur de dependances
 *
 */
package oqube.patchwork.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import oqube.bytes.ClassFile;
import oqube.patchwork.gui.LinkType.LinkTypeFilter;
import oqube.bytes.loading.CachedClassFileFactory;
import oqube.bytes.loading.CompositeClassFileFactory;
import oqube.bytes.loading.ClassFileFactory;
import oqube.bytes.loading.FilesFactory;
import oqube.bytes.loading.FilesetDirectoriesFactory;
import oqube.bytes.utils.Fileset;
import salvo.jesus.graph.DirectedEdgeImpl;
import salvo.jesus.graph.DirectedGraph;
import salvo.jesus.graph.DirectedGraphImpl;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.Graph;
import salvo.jesus.graph.GraphFactory;
import salvo.jesus.graph.GraphOps;
import salvo.jesus.graph.algorithm.AndGraphFilter;
import salvo.jesus.graph.algorithm.GraphFilter;
import salvo.jesus.graph.algorithm.GraphMorphism;
import salvo.jesus.graph.algorithm.TarjanSCC;
import salvo.jesus.graph.visual.Arrowhead;
import salvo.jesus.graph.visual.DefaultArrowhead;
import salvo.jesus.graph.visual.GraphPanelNormalState;
import salvo.jesus.graph.visual.GraphScrollPane;
import salvo.jesus.graph.visual.SimpleGraphPanel;
import salvo.jesus.graph.visual.VisualEdge;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.VisualGraphComponentFactory;
import salvo.jesus.graph.visual.VisualGraphComponentManager;
import salvo.jesus.graph.visual.VisualVertex;
import salvo.jesus.graph.visual.ZoomUI;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingLayout;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingUI;
import salvo.jesus.graph.visual.print.VisualGraphPrinter;
import fr.lifl.utils.CommandLine;
import fr.lifl.utils.ExtensionFileFilter;

/**
 * This class scans a set of class files and shows interclass relationships in a
 * graphical window.
 * 
 * @author nono
 * @version $Id: ClassDepend.java 20 2007-05-29 19:50:34Z arnaud.oqube $
 */
public class ClassDepend extends GraphScrollPane {

  private static final Logger         log          = Logger.getLogger(ClassDepend.class.getName());

  private static Preferences          prefs        = Preferences.userNodeForPackage(ClassDepend.class);

  protected boolean                   references   = true;

  private boolean                     calls;

  /* a filter that excludes referenced classes */
  private GraphFilter                 refFilter    = new GraphFilter() {
                                                     public boolean filter(Object v) {
                                                       if(((CodeInfo)v).isReference())
                                                         return false;
                                                       return true;
                                                     }

                                                     public boolean filter(Edge e) {
                                                       return true;
                                                     }
                                                   };

  // currently displayed graph
  private DirectedGraph               current;

  private int                         minZoom      = 1;

  private int                         maxZoom      = 4096;

  private int                         zoomFactor   = 1024;

  private static List                 scanList;

  private SimulatedAnnealingLayout    layout;

  /* contains links to show from graph */
  private Set<LinkType>               links        = new HashSet<LinkType>();

  private LinkTypeFilter              linkFilter   = new LinkType.LinkTypeFilter(links);

  private ClassDependencyGraph        classDependencyGraph;

  static ClassDepend                  cd;

  private VisualGraphComponentFactory vf;

  private boolean                     filterSystem = true;

  protected boolean                   packages;

  private SimulatedAnnealingUI        layoutUI;

  private GraphInfo                   infotable;

  private CachedClassFileFactory      classFactory;

  ClassDepend() {
    setFont(new Font("sans-serif", Font.PLAIN, 9));
    vf = new VisualGraphComponentFactory() {
      Random rand = new Random();

      public VisualVertex createVisualVertex(Object vertex, VisualGraph graph) {
        // create shape - compute size of text and adjust
        FontMetrics fm = getFontMetrics(getFont());
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, fm.stringWidth(vertex.toString()) * 1.2, 2 * fm.getHeight());
        // create VisualVertex
        VisualVertex vv = new VisualVertex(vertex, bounds, Color.BLACK, Color.yellow, getFont(), graph);
        vv.setLocation(rand.nextInt(1000), rand.nextInt(1000));
        /* differentiate analyzed classes from referenced classes */
        if(((CodeInfo)vertex).isReference())
          vv.setFillcolor(Color.ORANGE);
        return vv;
      }

      public VisualEdge createVisualEdge(Edge edge, VisualGraph graph) {
        VisualEdge ve = new VisualEdge(edge, graph);
        if(edge.getData().equals(LinkType.inherits))
          ve.setOutlinecolor(Color.ORANGE);
        else if(edge.getData().equals(LinkType.implement))
          ve.setOutlinecolor(Color.RED);
        else if(edge.getData().equals(LinkType.uses))
          ve.setOutlinecolor(Color.black);
        return ve;
      }

      public Arrowhead createArrowhead() {
        return new DefaultArrowhead();
      }
    };
    VisualGraphComponentManager.setFactory(vf);
    classFactory = new CachedClassFileFactory();
  }

  private void initData() {
    classFactory = new CachedClassFileFactory();
    classDependencyGraph = new ClassDependencyGraph(filterSystem);
    classDependencyGraph.setFactory(classFactory);
    current = classDependencyGraph.getGraph();
  }

  private void initUI() {
    JFrame frame = new JFrame("Class Dependency Graph");
    frame.setJMenuBar(makeMenuBar());
    Container cont = frame.getContentPane();
    /* add toolbar */
    JToolBar tool = new JToolBar();
    tool.add(new ZoomUI(this.gpanel));
    tool.add(layoutUI = new SimulatedAnnealingUI());
    cont.add(tool, BorderLayout.NORTH);
    cont.add(this, BorderLayout.CENTER);
    /* position frame */
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setBounds(new Rectangle(100, 100, screen.width / 2, screen.height / 2));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    this.infotable = new GraphInfo();
    this.gpanel.setLayout(null);
    this.gpanel.add(infotable);
    updateGraph();
    invalidate();
    frame.pack();
  }

  private void doGraphLayout() {
    /* stop previous l ayout */
    if(layout != null)
      layout.layout();
    layout = new SimulatedAnnealingLayout(gpanel.getVisualGraph(), true);
    layout.setTemperature(150);
    layout.setCoolFactor(0.002);
    layout.setAttractiveForce(3);
    layout.setExpectedDist(200);
    // layout.setRandomMove(0.02);
    layout.setRepaint(true);
    gpanel.getVisualGraph().setGraphLayoutManager(layout);
    layoutUI.setLayoutManager(layout);
    layout.layout();
  }

  /**
   * First argument is a directory from which to load classes
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    /* get options */
    CommandLine cl = new CommandLine();
    cl.addOption('h'); // show inheritance links
    cl.addOption('d'); // show dependance links
    cl.addOption('i'); // show implementation links
    cl.addOption('p'); // show package links
    cl.addOptionMultiple('f'); // filtered classes
    cl.addOptionSingle('v'); // verbosity level
    cl.parseOptions(args);
    /* position arguments */
    if(cl.getArguments().isEmpty())
      cl.getArguments().add(".");
    /* create graph object */
    cd = new ClassDepend();
    if(cl.isSet('h'))
      cd.links.add(LinkType.inherits);
    if(cl.isSet('d'))
      cd.links.add(LinkType.uses);
    if(cl.isSet('i'))
      cd.links.add(LinkType.implement);
    cd.initData();
    cd.classDependencyGraph.addFilters((LinkedList<String>)cl.getOption('f').getArgument());
    cd.scan(cl.getArguments());
    makeLookAndFeel();
    cd.initUI();
  }

  private static void makeLookAndFeel() {
    JFrame.setDefaultLookAndFeelDecorated(false);
    UIManager.put(com.jgoodies.plaf.Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
    com.jgoodies.plaf.Options.setGlobalFontSizeHints(com.jgoodies.plaf.FontSizeHints.MIXED);
    com.jgoodies.plaf.Options.setPopupDropShadowEnabled(true);
    com.jgoodies.plaf.Options.setDefaultIconSize(new Dimension(18, 18));
    try {
      UIManager.setLookAndFeel(com.jgoodies.plaf.Options.getCrossPlatformLookAndFeelClassName());
    } catch(Exception e1) {
      e1.printStackTrace();
      System.err.println("Cannot select look&feel");
    }
  }

  /*
   * This method scan given list of files and directories pattenrs and append
   * their content to the processed list of files
   * 
   */
  private void scan(List<String> dirlist) throws FileNotFoundException, IOException {
    List<File> lf = new ArrayList<File>();
    for(String s : dirlist)
      lf.add(new File(s));
    scanFiles(lf);
  }

  private void scanFiles(List<File> lf) throws FileNotFoundException, IOException {
    // create provider
    FilesFactory ff = new FilesFactory();
    long time = System.currentTimeMillis();
    int count = 0;
    // construct list of files
    for(File f : lf) {
      ff.add(f);
      Collection<ClassFile> cfs = ff.getAllDefinedClassFiles();
      count += cfs.size();
      for(ClassFile classe : cfs)
        try {
          classDependencyGraph.add(classe);
        } catch(Exception e) {
          e.printStackTrace();
          log.warning("Cannot analyze class file " + classe + ": " + e);
        }
    }
    this.classFactory.add(ff);
    log.info("Scanned " + count + " classes in " + (System.currentTimeMillis() - time) + "ms");
  }

  private ClassFile scanFile(File clf) throws FileNotFoundException, IOException {
    /* read class file and generate ClassFile object */
    InputStream fis = new FileInputStream(clf);
    DataInputStream dis = new DataInputStream(fis);
    ClassFile cf = new ClassFile();
    try {
      cf.read(dis);
      return cf;
    } finally {
      dis.close();
      fis.close();
    }
  }

  class LinkMenuItem extends JCheckBoxMenuItem {

    LinkMenuItem(String name, final LinkType type) {
      super(name, links.contains(type));
      setSelected(prefs.getBoolean("filter." + type, false));
      addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          switch(e.getStateChange()) {
          case ItemEvent.SELECTED:
            links.add(type);
            break;
          case ItemEvent.DESELECTED:
            links.remove(type);
            break;
          }
          prefs.putBoolean("filter." + type, e.getStateChange() == ItemEvent.SELECTED);
          updateGraph();
        }
      });
      if(isSelected())
        links.add(type);
    }

  }

  /**
   * @return
   */
  private JMenuBar makeMenuBar() {
    JMenuBar menubar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menubar.add(menu);
    JMenuItem item = new JMenuItem("Load");
    menu.add(item);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String rootdir = prefs.get("default.directory", ".");
        /* prompt for a directory to scan */
        File[] file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(rootdir));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Scan classes from a directory");
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(cd);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
          file = chooser.getSelectedFiles();
        } else
          return;
        /* initialize graph */
        initData();
        try {
          scanFiles(Arrays.asList(file));
          updateGraph();
        } catch(FileNotFoundException e1) {
          JOptionPane.showMessageDialog(cd, "Cannot find a file : " + e1);
        } catch(IOException e1) {
          JOptionPane.showMessageDialog(cd, "Error in scanning files : " + e1);
        }
        // save files list in user preferences
        File dir = chooser.getCurrentDirectory();
        prefs.put("default.directory", dir.getAbsolutePath());
      }

    });
    item = new JMenuItem("Reset");
    menu.add(item);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        initData();
        updateGraph();
      }

    });

    item = new JMenuItem("Print");
    menu.add(item);
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        new VisualGraphPrinter(gpanel.getVisualGraph()).showPrint(getX(), getY());
      }

    });

    item = new JMenuItem("Export");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Save dependency graph");
        chooser.setMultiSelectionEnabled(false);
        ExtensionFileFilter filter = new ExtensionFileFilter();
        filter.addExtension("png");
        filter.setDescription("PNG files");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(cd);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
          file = chooser.getSelectedFile();
        } else
          return;
        /* render layout to file */
        VisualGraph vg = gpanel.getVisualGraph();
        Dimension dim = vg.getMaxSize();
        BufferedImage im = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, dim.width, dim.height);
        vg.paint(g);
        try {
          ImageIO.write(im, "png", file);
        } catch(IOException e1) {
          e1.printStackTrace();
        }
      }
    });

    menu.add(item);
    item = new JMenuItem("Quit");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    menu.add(item);
    menu = new JMenu("Edit");
    item = new JMenuItem("Filter nodes");
    item.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        String filter = JOptionPane.showInputDialog(ClassDepend.this, "Enter regular expression for filtering nodes");
        // append filter to classdependency and redisplay
        if(filter != null) {
          classDependencyGraph.addFilter(filter);
          updateGraph();
        }
      }

    });
    menu.add(item);
    item = new JMenu("Filter links");
    menu.add(item);
    JMenuItem subitem = new LinkMenuItem("Inheritance", LinkType.inherits);
    item.add(subitem);
    subitem = new LinkMenuItem("Dependance", LinkType.uses);
    item.add(subitem);
    subitem = new LinkMenuItem("Implements", LinkType.implement);
    item.add(subitem);
    subitem = new JCheckBoxMenuItem("References", references) {
      {
        setSelected(references = prefs.getBoolean("filter.references", false));
      }
    };
    subitem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        switch(e.getStateChange()) {
        case ItemEvent.SELECTED:
          references = true;
          break;
        case ItemEvent.DESELECTED:
          references = false;
          break;
        }
        prefs.putBoolean("filter.references", references);
        updateGraph();
      }
    });
    item.add(subitem);
    item = new JCheckBoxMenuItem("Package", false) {
      {
        setSelected(packages = prefs.getBoolean("filter.packages", false));
      }
    };
    item.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        switch(e.getStateChange()) {
        case ItemEvent.SELECTED:
          packages = true;
          break;
        case ItemEvent.DESELECTED:
          packages = false;
          break;
        }
        prefs.putBoolean("filter.packages", packages);
        updateGraph();
      }
    });
    menu.add(item);
    item = new JCheckBoxMenuItem("Method calls", false);
    item.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        switch(e.getStateChange()) {
        case ItemEvent.SELECTED:
          calls = true;
          break;
        case ItemEvent.DESELECTED:
          calls = false;
          break;
        }
        updateGraph();
      }
    });
    menu.add(item);
    item = new JMenu("Zoom");
    JMenuItem item2 = new JMenuItem("in");
    item2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomIn();
      }
    });
    item.add(item2);
    item2 = new JMenuItem("out");
    item2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomOut();
      }
    });
    item.add(item2);
    menu.add(item);
    menubar.add(menu);

    return menubar;
  }

  /**
   * 
   */
  protected void updateGraph() {
    try {
      if(this.calls) {
        CallGraph pg = this.classDependencyGraph.makeCallGraph();
        this.current = pg.getGraph();
        infotable.setData(pg.getInfomap());
        gpanel.setVisualGraph(new VisualGraph(this.current));
        doGraphLayout();
        return;
      }
      if(this.packages) {
        PackageDependencyGraph pg = this.classDependencyGraph.makePackageGraph();
        this.current = pg.getGraph();
        infotable.setData(pg.getInfomap());
      } else {
        this.current = this.classDependencyGraph.getGraph();
        infotable.setData(this.classDependencyGraph.getInfomap());
      }
      /* apply required filters */
      GraphFilter flt = linkFilter;
      if(!references)
        if(flt != null)
          flt = new AndGraphFilter(flt, refFilter);
        else
          flt = refFilter;
      if(flt != null)
        this.current = GraphOps.filter(this.current, flt);
    } catch(Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Unable to udpate graph view :" + e.getMessage());
      return;
    }
    gpanel.setVisualGraph(new VisualGraph(this.current));
    doGraphLayout();
  }

  private void zoomIn() {
    if(zoomFactor < maxZoom) {
      zoomFactor = zoomFactor * 2;
      gpanel.setZoomFactor(((double)zoomFactor) / (double)1024);
    }
  }

  private void zoomOut() {
    if(zoomFactor > minZoom) {
      zoomFactor = zoomFactor / 2;
      gpanel.setZoomFactor(((double)zoomFactor) / (double)1024);
    }
  }

}