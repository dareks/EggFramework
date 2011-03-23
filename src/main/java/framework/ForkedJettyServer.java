/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.webapp.WebAppContext;

public class ForkedJettyServer {

	private static Server server;

	public static void start() throws InterruptedException {
		while (server != null) {
			Thread.sleep(100);
		}
		server = new Server(8080);
		WebAppContext ctx = new WebAppContext();
		HashSessionManager sessionManager = (HashSessionManager) ctx.getSessionHandler().getSessionManager();
		sessionManager.setSavePeriod(1);
		sessionManager.setStoreDirectory(new File("target/sessions"));
		ctx.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false"); // this should be only on Windows
		ctx.getInitParams().put("org.eclipse.jetty.servlet.Default.maxCachedFiles", "0");
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
