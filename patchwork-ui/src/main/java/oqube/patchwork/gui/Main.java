/*
 * Created on May 8, 2004
 * 
 * $Log: ControlGraphDisplay.java,v $
 * Revision 1.3  2004/08/30 21:06:07  bailly
 * cleaned imports
 *
 * Revision 1.2  2004/07/19 06:51:30  bailly
 * testing bytecode coverage with JDI interface
 *
 * Revision 1.1  2004/05/09 22:09:24  bailly
 * Added frame display capability to ControlGraph display
 * Corrected control graph construction
 * added exceptions block handling, tableswitch and lookupswitch
 * TODO : correct implementation of branch splitting and ordering of blocks
 *
 */
package oqube.patchwork.gui;

import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.UIManager;

import oqube.bytes.ClassFile;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.struct.MethodFileInfo;
import oqube.patchwork.graph.BasicBlock;
import oqube.patchwork.graph.ControlGraph;
import salvo.jesus.graph.visual.VisualGraph;
import salvo.jesus.graph.visual.VisualVertex;
import salvo.jesus.graph.visual.layout.SimulatedAnnealingLayout;
import salvo.jesus.graph.visual.print.VisualGraphImageOutput;
import fr.lifl.utils.CommandLine;

/**
 * This class displays the control graph of a given method code.
 * <p>
 * This program must be given two arguments :
 * <ul>
 * <li>a class file to display</li>
 * <li>a method name from this class </li>
 * </ul>
 * 
 * @author nono
 * @version $Id: Main.java 23 2008-01-25 16:45:44Z arnaud.oqube $
 */
public class Main {

	private ControlGraph cg;
	private File file;

	public static void usage() {
		System.out
				.println("ControlGraphDisplay [-o <outfilename>] [-i] <classfile> [<methodname>]");
	}

	public static void main(String[] args) {
		boolean interactive = false;
		String outputFilename = "controlgraph.png";
		/* read arguments */
		if (args.length < 2) {
			usage();
			System.exit(1);
		}
		CommandLine opts = new CommandLine();
		opts.addOption('i'); /* interactive mode */
		opts.addOptionSingle('o'); /* name of output file */
		opts.parseOptions(args);
		if (opts.isSet('i'))
			interactive = true;
		if (opts.isSet('o'))
			outputFilename = (String) opts.getOption('o').getArgument();
		List l = opts.getArguments();
		makeLookAndFeel();
		Main display;
		try {
			display = new Main(new File((String) l.get(0)), (String) l.get(1));
			if (interactive)
				display.displayGraph();
			else
				display.outputGraph(outputFilename);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error in creating control graph :"
					+ e.getMessage());
		}
	}

	private static void makeLookAndFeel() {
		JFrame.setDefaultLookAndFeelDecorated(false);
		// UIManager.LookAndFeelInfo[] lafs =
		// UIManager.getInstalledLookAndFeels();
		// for(int i = 0;i<lafs.length;i++)
		// System.err.println(lafs[i].getName() +" : "+lafs[i].getClassName());
		UIManager.put(com.jgoodies.plaf.Options.USE_SYSTEM_FONTS_APP_KEY,
				Boolean.TRUE);
		com.jgoodies.plaf.Options
				.setGlobalFontSizeHints(com.jgoodies.plaf.FontSizeHints.MIXED);
		com.jgoodies.plaf.Options.setPopupDropShadowEnabled(true);
		com.jgoodies.plaf.Options.setDefaultIconSize(new Dimension(18, 18));
		try {
			UIManager.setLookAndFeel(com.jgoodies.plaf.Options
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
			System.err.println("Cannot select look&feel");
		}
	}

	/**
	 * displays a graph in a frame
	 */
	private void displayGraph() {
		ControlGraphDisplayFrame frame = new ControlGraphDisplayFrame(cg);
		frame.display();
		try {
			frame.loadClassFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void outputGraph(String outputFile) throws IOException {
		/* create visual graph and output it */
		/* create visual graph */
		VisualGraph vg = new VisualGraph();
		vg.setGraph(cg.getGraph());
		/* layout graph */
		SimulatedAnnealingLayout layout = new SimulatedAnnealingLayout(vg,
				false);
		VisualVertex sv = vg.getVisualVertex(cg.getGraph().findVertex(
				new BasicBlock.StartBlock()));
		sv.setLocation(20, 20);
		layout.addFixedVertex(sv);
		layout.setExpectedDist(500);
		layout.setRepaint(false);
		layout.layout();
		VisualGraphImageOutput out = new VisualGraphImageOutput();
		out.setFormat("eps");
		FileOutputStream fos = new FileOutputStream(outputFile);
		out.output(vg, fos);
		fos.flush();
		fos.close();
	}

	/**
	 * @param file
	 * @param string
	 */
	public Main(File file, String string) throws Exception {
		/* read class file */
		ClassFile cf = new ClassFile();
		MethodFileInfo mfi = null;
		cf.read(new DataInputStream(new FileInputStream(file)));
		Collection<MethodFileInfo> methods = null;
		if (string == null || "".equals(string)) {
			methods = cf.getAllMethods();
		} else {
			methods = cf.getMethodInfo(string);
		}
		if (methods == null)
			throw new Exception("Unknown method " + string);
		mfi = (MethodFileInfo) methods.iterator().next();
		/* get sole method from file */
		CodeAttribute code = mfi.getCodeAttribute();
		if (code == null)
			throw new Exception("The method " + string + " is abstract");
		/* create control graph */
		this.cg = new ControlGraph(mfi);
		/* store file */
		this.file = file;
	}
}
