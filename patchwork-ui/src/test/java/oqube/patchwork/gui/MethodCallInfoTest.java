package oqube.patchwork.gui;

import junit.framework.TestCase;

public class MethodCallInfoTest extends TestCase {

	MethodInfo mci = new MyMethodInfo();

	class MyMethodInfo extends MethodInfo {

		public boolean isReference() {
			// TODO Auto-generated method stub
			return false;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	public void testObjectsWithSameMethodButDifferentLabels() {
		MethodCallInfo mc1 = new MethodCallInfo(mci, "un");
		MethodCallInfo mc2 = new MethodCallInfo(mci, "deux");
		assertTrue("object should not be equals", !mc1.equals(mc2));
	}

	public void testObjectsWithSameLabelButDifferentMethods() {
		MyMethodInfo mci2 = new MyMethodInfo();
		MethodCallInfo mc1 = new MethodCallInfo(mci, "un");
		MethodCallInfo mc2 = new MethodCallInfo(mci2, "un");
		assertTrue("object should not be equals", !mc1.equals(mc2));
	}

	public void testObjectsThatShouldBeIdentical() {
		MethodCallInfo mc1 = new MethodCallInfo(mci, "un");
		MethodCallInfo mc2 = new MethodCallInfo(mci, "un");
		assertTrue("object should be equals", mc1.equals(mc2));
		assertEquals("hashcode should be equals", mc1.hashCode(), mc2
				.hashCode());
	}

	public void testObjectsWithDifferentClassesReturnFalse() {
		MethodCallInfo mc1 = new MethodCallInfo(mci, "un");
		assertTrue("object should not be equals", !mc1.equals(mci));

	}
}
