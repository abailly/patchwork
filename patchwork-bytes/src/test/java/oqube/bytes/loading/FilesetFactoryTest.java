package oqube.bytes.loading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.lifl.utils.Pipe;

import oqube.bytes.ClassFile;
import oqube.bytes.utils.TemporaryFS;
import junit.framework.TestCase;

public class FilesetFactoryTest extends TestCase {

	private TemporaryFS tfs;

	private FilesetDirectoriesFactory provider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.tfs = new TemporaryFS(new File(new File(System
				.getProperty("java.io.tmpdir")), "tests"));
		// write some classfile data
		ClassLoader l = Thread.currentThread().getContextClassLoader();
		InputStream is = l.getResourceAsStream(FilesetFactoryTest.class
				.getName().replace('.', '/')
				+ ".class");
		File nest = new File(tfs.root(), "inner/oqube/bytes/loading");
		if (!nest.mkdirs())
			throw new Exception("Cannot create directory " + nest);
		// write class file
		FileOutputStream fos = new FileOutputStream(new File(nest,
				"FilesetFactoryTest.class"));
		new Pipe(fos, is).pump();
		fos.close();
		is.close();
		this.provider = new FilesetDirectoriesFactory(this.tfs.root());
	}

	public void testLoadNestedClass() throws IOException {
		ClassFile cf = this.provider
				.getClassFileFor("oqube.bytes.loading.FilesetFactoryTest");
		assertNotNull(cf);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.tfs.clean();
	}

}
