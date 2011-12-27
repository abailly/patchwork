/**
 * 
 */
package oqube.patchwork.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

/**
 * A widget that displays informations about properties of some object as a
 * table.
 * 
 * @author nono
 * 
 */
public class GraphInfo extends JTable {

	private static final String EOL = System
			.getProperty("line.separator", "\n");

	class MapTableModel extends AbstractTableModel {

		private Object[][] map;

		MapTableModel(Map<String, Object> m) {
			map = new Object[m.size()][2];
			int j = 0;
			for (Iterator<Map.Entry<String, Object>> i = m.entrySet()
					.iterator(); i.hasNext(); j++) {
				Map.Entry<String, Object> e = i.next();
				map[j][0] = e.getKey();
				map[j][1] = e.getValue();
			}
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return map.length;
		}

		public Object getValueAt(int arg0, int arg1) {
			return map[arg0][arg1];
		}

	}

	/**
	 * Create an initially invisible information widget.
	 * 
	 * 
	 */
	public GraphInfo() {
		setBackground(new Color(255,255,0,127));
		setForeground(Color.blue);
		setOpaque(false);
		setBorder(BorderFactory.createLineBorder(Color.black));
		setFont(new Font("sans-serif", Font.PLAIN, 8));
		setVisible(true);
		setFocusable(false);
		setBounds(10,10,200, getModel().getRowCount() * getRowHeight());
	}

	/**
	 * Sets the data used by this info panel. The given Map is converted into a
	 * nx2 table.
	 * 
	 * @param data
	 *            a map. May not be null.
	 */
	public void setData(Map<String, Object> data) {
		setModel(new MapTableModel(data));
		setBounds(10,10,200, getModel().getRowCount() * getRowHeight());
		repaint();
	}

	@Override
	public void paint(Graphics arg0) {
		// apply alpha to graphics
		Graphics2D g2d = (Graphics2D)arg0;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .6f));
		super.paint(g2d);
	}

}
