/**
 * 
 */
package oqube.bytes.loading;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import oqube.bytes.ClassFile;
import oqube.bytes.utils.Fileset;
import oqube.bytes.utils.TemporaryFS;

/**
 * Load class files from a root directory. When asked for classes data, this
 * object scans its directory recursively and tries to locate a
 * <code>.class</code> file whose name matches the given class name. Note that
 * the class's package structure need not be rooted at the top of this fileset
 * provider directory structure, but it must exists.
 * 
 * @author nono
 * 
 */
public class FilesetDirectoriesFactory implements ClassFileFactory {

	private File root;

	private Fileset fileset;

	public FilesetDirectoriesFactory(File root) {
		this.root = root;
		this.fileset = new Fileset(new FileFilter() {

			public boolean accept(File arg0) {
				return arg0.isFile() && arg0.canRead()
						&& arg0.getName().endsWith(".class");
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see oqube.bytes.loading.ClassFileFactory#getClassFileFor(java.lang.String)
	 */
	public ClassFile getClassFileFor(String className) throws IOException {
		String fname = className.replace('.', File.separatorChar) + ".class";
		// try to locate file
		for (File f : fileset.files(root))
			if (f.getPath().endsWith(fname))
				return ClassFile.makeClassFile(new FileInputStream(f));
		return null;
	}
}
