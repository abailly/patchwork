package oqube.patchwork.gui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

public class GraphInfoTest extends TestCase {

	private GraphInfo info;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.info = new GraphInfo();
	}

	public void testMapDefinition() {
		Map<String, Object> data = new LinkedHashMap<String, Object>() {
			{
				put("toto", "a string");
				put("tutu", 1);
				put("titi", true);
				put("tata", 25.12);
				put("a class", new ClassInfo("some/package/Class"));
			}
		};
		info.setData(data);
		// check model is updated
		assertEquals(5, info.getRowCount());
		assertEquals(true, info.getModel().getValueAt(2, 1));
		assertEquals(new ClassInfo("some/package/Class"), info.getModel()
				.getValueAt(4, 1));
	}

}
