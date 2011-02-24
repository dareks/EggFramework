package framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;

import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.stores.FileResourceStore;

/**
 * Run this class in order to run a web server with you application. Remember to specify java.library.path JVM 
 * option pointing to libs directory (-Djava.library.path=libs). 
 * 
 * @author Jacek Olszak
 */
public class ReloadingServer {

	private volatile boolean modified;

	public ReloadingServer() throws InterruptedException, IOException {
		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		ReloadingClassLoader pParent = new ReloadingClassLoader(urlClassLoader);
		final ReloadingClassLoader loader = new ReloadingClassLoader(pParent);
		for (URL url : urlClassLoader.getURLs()) {
			String file = url.getFile();
			if (!file.contains("commons-jci")) {
				if (file.endsWith(".jar")) {
					pParent.addResourceStore(new JarResourceStore(file));
				} else {   
					pParent.addResourceStore(new FileResourceStore(new File(file)));
				}
			}
		}
		Thread.currentThread().setContextClassLoader(loader);

		String classesDir = new File("target/classes").getAbsolutePath();                
		int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
		boolean watchSubtree = true; 
		try {
			int watchID = JNotify.addWatch(classesDir, mask, watchSubtree, new JNotifyAdapter() {
				@Override
				public void fileModified(int wd, String rootPath, String name) {
					if (name.endsWith(".class")) {
						modified = true;
					}
				}
			});
		} catch (Exception e) {                
			e.printStackTrace();         
		}
		startJetty();
		while (true) { 
			try { 
				Thread.sleep(400);
			} catch (InterruptedException e) {
				break;
			}
			synchronized (ReloadingServer.class) {
				if (modified) {
					stopJetty();  
					ReloadingClassLoader contextClassLoader = (ReloadingClassLoader) Thread.currentThread().getContextClassLoader();
					contextClassLoader.handleNotification(); 
					ReloadingClassLoader parent = (ReloadingClassLoader) contextClassLoader.getParent();
					parent.handleNotification();
					Thread.currentThread().setContextClassLoader(new ReloadingClassLoader(parent));
					startJetty();
					modified = false;
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new ReloadingServer();
	}

	public static void startJetty() {
		run("framework.ForkedJettyServer", "start");
	}

	private static void run(String className, String method) {
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Object obj = clazz.newInstance();
			obj.getClass().getMethod(method).invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopJetty() {
		run("framework.ForkedJettyServer", "stop");
	}
}
