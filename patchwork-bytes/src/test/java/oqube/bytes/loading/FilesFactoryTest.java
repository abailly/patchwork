package oqube.bytes.loading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import oqube.bytes.utils.TemporaryFS;
import fr.lifl.utils.Pipe;
import junit.framework.TestCase;

public class FilesFactoryTest extends TestCase {

	private TemporaryFS tfs;

	private FilesFactory provider;

	private File file;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.tfs = new TemporaryFS(new File(new File(System
				.getProperty("java.io.tmpdir")), "tests"));
		// write some classfile data
		ClassLoader l = Thread.currentThread().getContextClassLoader();
		InputStream is = l.getResourceAsStream(FilesFactoryTest.class
				.getName().replace('.', '/')
				+ ".class");
		File nest = new File(tfs.root(), "inner");
		if (!nest.exists() && !nest.mkdirs())
			throw new Exception("Cannot create directory " + nest);
		// write class file
		FileOutputStream fos = new FileOutputStream(file = new File(nest,
				"FilesFactoryTest.class"));
		new Pipe(fos, is).pump();
		fos.close();
		is.close();
		this.provider = new FilesFactory();
	}

  public void testAddADirectory() throws IOException {
    this.provider.add(tfs.root());
    assertNotNull("provider did not find class in directory", this.provider
        .getClassFileFor("oqube/bytes/loading/FilesFactoryTest"));
  }
  
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.tfs.clean();
	}

}
