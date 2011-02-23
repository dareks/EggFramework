package framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.stores.ResourceStore;

public class JarResourceStore implements ResourceStore {

	private JarFile jar;

	public JarResourceStore(String filename) throws IOException {
		jar = new JarFile(filename);
	}

	public void write(String pResourceName, byte[] pResourceData) {
	}

	public synchronized byte[] read(String pResourceName) {
		JarEntry jarEntry = jar.getJarEntry(pResourceName);
		if (jarEntry != null) {
			try {
				InputStream inputStream = jar.getInputStream(jarEntry);
				try {
					return IOUtils.toByteArray(inputStream);
				} finally {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public void remove(String pResourceName) {
	}

}
