package framework;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ForkedJettyServer {

	private static Server server;

	public static void start() throws InterruptedException {
		while (server != null) {
			Thread.sleep(100);
		}
		server = new Server(8080);
		WebAppContext ctx = new WebAppContext();
		ctx.setContextPath("/");
		ctx.setResourceBase("src/main/webapp");
		ctx.setClassLoader(Thread.currentThread().getContextClassLoader());
		server.setHandler(ctx);
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void stop() throws Exception { 
		if (!server.isStopping()) {   
			server.stop();
			server = null;
		}
	}
}
