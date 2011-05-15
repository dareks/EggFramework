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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrameworkServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static Object application;
	public boolean started;

	private void createAndStartApplication() throws ServletException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		Class<?> appClass = Thread.currentThread().getContextClassLoader()
				.loadClass("services.Application");
		application = appClass.newInstance();
		application.getClass().getMethod("start").invoke(application);
		started = true;
	}

	@Override
	public void destroy() {
		try {
			application.getClass().getMethod("stop").invoke(application);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		synchronized (this) { // bottleneck?
			if (!started) {
				try {
					createAndStartApplication();
				} catch (Exception e) {
					throw new ServletException(
							"Problem when creating application instance", e);
				}
			}
		}
		try {
			Class<?> controllerClass = Thread.currentThread()
					.getContextClassLoader()
					.loadClass("framework.FrontController");
			Object controller = controllerClass.newInstance();
			invoke("service", controller, req, resp, getServletContext());
		} catch (Exception e) {
			// TODO WTF?
			Throwable cause = e;
			if (e.getCause() != null) {
				cause = e.getCause();
			}
			if (cause.getCause() != null) {
				cause = cause.getCause();
			}
			if (cause instanceof ClientException) {
				resp.sendError(400, cause.getMessage());
			} else {
				throw new ServletException(cause.getMessage(), cause);
			}
		}
	}

	private void invoke(String methodName, Object controller,
			HttpServletRequest req, HttpServletResponse resp, ServletContext ctx)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method method = controller.getClass().getMethod(methodName,
				HttpServletRequest.class, HttpServletResponse.class,
				ServletContext.class);
		method.invoke(controller, req, resp, ctx);
	}

}
