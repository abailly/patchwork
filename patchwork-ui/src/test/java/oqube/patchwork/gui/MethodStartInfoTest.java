package oqube.patchwork.gui;

import oqube.patchwork.gui.MethodCallInfoTest.MyMethodInfo;
import junit.framework.TestCase;

public class MethodStartInfoTest extends TestCase {

	public void testObjectsWithSameKlassAndDifferentMethodsAndSignatures() {
		MethodStartInfo msi1 = new MethodStartInfo("k1", "m1", "s1");
		MethodStartInfo msi2 = new MethodStartInfo("k1", "m3", "s2");
		assertTrue("object should not be equals", !msi1.equals(msi2));
	}

	public void testObjectsWithSameKlassAndMethodsAndDifferentSignatures() {
		MethodStartInfo msi1 = new MethodStartInfo("k1", "m1", "s1");
		MethodStartInfo msi2 = new MethodStartInfo("k1", "m1", "s2");
		assertTrue("object should not be equals", !msi1.equals(msi2));
	}

	public void testObjectsWithSameKlassAndSignaturesAndDifferentMethods() {
		MethodStartInfo msi1 = new MethodStartInfo("k1", "m1", "s1");
		MethodStartInfo msi2 = new MethodStartInfo("k1", "m2", "s1");
		assertTrue("object should not be equals", !msi1.equals(msi2));
	}

	public void testObjectsWithSameSignaturesAndMethodsAndDifferentKlass() {
		MethodStartInfo msi1 = new MethodStartInfo("k1", "m1", "s1");
		MethodStartInfo msi2 = new MethodStartInfo("k2", "m1", "s1");
		assertTrue("object should not be equals", !msi1.equals(msi2));
	}

	public void testObjectsWithSameSignaturesAndMethodsAndKlass() {
		MethodStartInfo msi1 = new MethodStartInfo("k1", "m1", "s1");
		MethodStartInfo msi2 = new MethodStartInfo("k1", "m1", "s1");
		assertTrue("object should be equals", msi1.equals(msi2));
		assertEquals("objects should have same hashcode", msi1.hashCode(), msi2
				.hashCode());
	}

}
