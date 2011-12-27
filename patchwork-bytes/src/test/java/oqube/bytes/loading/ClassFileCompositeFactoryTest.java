package oqube.bytes.loading;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import oqube.bytes.ClassFile;

import junit.framework.TestCase;

public class ClassFileCompositeFactoryTest extends TestCase {

	public void testComposeTwoProviders() {
		final ClassFile cf1 = new ClassFile();
		final ClassFile cf2 = new ClassFile();
		ClassFileFactory cfp1 = new ClassFileFactory() {

			public ClassFile getClassFileFor(String className) {
				return null;
			}

		};
		ClassFileFactory cfp2 = new ClassFileFactory() {

			public ClassFile getClassFileFor(String className) {
				return cf2;
			}

		};
		CompositeClassFileFactory ccp = new CompositeClassFileFactory();
		ccp.add(cfp1);
		ccp.add(cfp2);
		assertSame("bad stream", cf2, ccp.getClassFileFor("dummy"));

	}
}
