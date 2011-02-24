package framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.stores.ResourceStore;

public class JarResourceStore implements ResourceStore {

	private String filename;

	public JarResourceStore(String filename) throws IOException {
		this.filename = filename;
	}

	public void write(String pResourceName, byte[] pResourceData) {
	}

	public synchronized byte[] read(String pResourceName) {
		JarFile jar = null;
		try {
			jar = new JarFile(filename);  
			JarEntry jarEntry = jar.getJarEntry(pResourceName);
			if (jarEntry != null) {

				InputStream inputStream = jar.getInputStream(jarEntry);
				try {
					return IOUtils.toByteArray(inputStream);
				} finally {
					inputStream.close();
				}  
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
		return null;
	}

	public void remove(String pResourceName) {
	}

}
