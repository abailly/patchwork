package oqube.bytes.loading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.lifl.utils.Pipe;

import oqube.bytes.ClassFile;
import oqube.bytes.utils.TemporaryFS;
import junit.framework.TestCase;

public class CachedClassFileFactoryTest extends TestCase {

	private CachedClassFileFactory factory;

	private TemporaryFS rootfs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.rootfs = new TemporaryFS(new File(new File(System
				.getProperty("java.io.tmpdir")), "tests"));
		// write some classfile data
		ClassLoader l = Thread.currentThread().getContextClassLoader();
		InputStream is = l.getResourceAsStream(Inner1.class.getName().replace(
				'.', '/')
				+ ".class");
		File nest = new File(rootfs.root(), "inner/oqube/bytes/loading");
		if (!nest.exists() && !nest.mkdirs())
			throw new Exception("Cannot create directory " + nest);
		// write class file
		FileOutputStream fos = new FileOutputStream(new File(nest,
				"CachedClassFileFactoryTest$Inner1.class"));
		new Pipe(fos, is).pump();
		fos.close();
		is.close();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		rootfs.clean();
	}

	static class Inner1 {
		public void m1() {

		}

		static class Inner2 {
			public void m2() {

			}
		}
	}

	public void testLoadingTwiceSameClassFileShouldReturnSameObject()
			throws IOException {
		this.factory = new CachedClassFileFactory();
		ClassFile cf = factory
				.getClassFileFor("oqube.bytes.loading.CachedClassFileFactoryTest");
		ClassFile cf2 = factory
				.getClassFileFor("oqube.bytes.loading.CachedClassFileFactoryTest");
		assertNotNull("no classfile created", cf);
		assertNotNull("no classfile created", cf2);
		assertSame("not the same object", cf, cf2);
	}

	public void testNonExistentClassThrowsIOException() {
		this.factory = new CachedClassFileFactory();
		ClassFile cf = factory.getClassFileFor("class.does.not.Exists");
		assertNull("should be null", cf);
	}

	public void testLoadingInnerClass() throws IOException {
		this.factory = new CachedClassFileFactory();
		ClassFile cf = factory
				.getClassFileFor("oqube.bytes.loading.CachedClassFileFactoryTest$Inner1");
		assertNotNull("no classfile created", cf);
	}

	public void testLoadingNestedClass() throws IOException {
		this.factory = new CachedClassFileFactory();
		ClassFile cf = factory
				.getClassFileFor("oqube.bytes.loading.CachedClassFileFactoryTest$Inner1$Inner2");
		assertNotNull("no classfile created", cf);
	}

	public void testAddFilesetForProvindingStreams() throws IOException {
		this.factory = new CachedClassFileFactory();
		this.factory.add(new FilesetDirectoriesFactory(rootfs.root()));
		ClassFile cf = factory
				.getClassFileFor("oqube.bytes.loading.CachedClassFileFactoryTest$Inner1");
		assertNotNull("no classfile created", cf);
	}
}
