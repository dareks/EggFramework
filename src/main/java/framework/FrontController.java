package framework;

import static framework.GlobalHelpers.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.StringUtil;

import com.google.common.base.Strings;

import framework.validation.ActionValidationConfig;
import framework.validation.Errors;

public class FrontController {

	private static final long serialVersionUID = 1L;
	// TODO MOVE THIS TO SOME OTHER PLACE WHICH IS NOT DEPENDENT ON SERVLETS
	public static final ThreadLocal<ThreadData> threadData = new ThreadLocal<ThreadData>();

	public void service(HttpServletRequest req, HttpServletResponse res, ServletContext ctx) throws ServletException, IOException {
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/html; charset=utf-8");
		String path = req.getServletPath();
		path = path.substring(0, path.indexOf('.'));

		System.out.printf("\n------------------------------ %15s ------------------------------\n", path);
		long started = System.currentTimeMillis();

		PrintWriter out = res.getWriter();
		ThreadData data = new ThreadData(req, res, out);
		threadData.set(data);
		try {
			Errors errors = new Errors();
			data.request.set("errors", errors);
			data.request.set("messages", new ArrayList<String>());
			data.request.set(data.flash.pop()); // Flash attributes are automatically inserted into attributes
			initClass(path);
			runBefore(path, data);
			ActionValidationConfig validationsConfig = ActionValidationConfig.get(path);
			validationsConfig.validate(data.params, errors);
			data.request.set("errors", errors); 
			if (errors.hasErrors()) {
				path = "/index"; // TODO GET PATH FROM CONFIG
			} else { 
				Response response = runAction(path, data);
				if (response != null) {
					if (response.forward != null) {
						path = response.forward;
					}
					if (response.redirect != null) {
						res.sendRedirect(response.redirect);
						return;
					}
				}
			}
			Map<String, Object> model = data.request.attributes;
			model.put("session", data.session);
			renderView(out, model, path);
			System.out.printf("------------------------------ %12d ms ------------------------------ \n", System.currentTimeMillis() - started);
		} finally {
			threadData.remove();
		}
	}
	
	/**
	 * Initializes controller class - static block will be executed
	 */
	private void initClass(String path) throws ServletException {
		String classForPath = classForPath(path);
		try {
			Class.forName(classForPath, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			throw new ServletException(e.getMessage(), e);
		}		
	}

	private String classForPath(String path) {
		if (!path.substring(1).contains("/")) {
			return "controllers.IndexController";
		} else {
			String className = path.substring(1, path.lastIndexOf("/"));
			return "controllers." + className.substring(0, 1).toUpperCase() + className.substring(1) + "Controller";
		}
	}

	private void runBefore(String path, ThreadData data) throws ServletException {
		try {
			Action before = findAction(classForPath(path), "before");
			if (before != null) {
				before.execute(data);
			}
		} catch (Exception e) {
			Throwable cause = e.getCause();
			throw new ServletException("Problem executing the action " + path, cause);
		}
	}

	private Response runAction(String path, ThreadData data) throws ServletException {
		try {
			Action action = findAction(classForPath(path), path.substring(path.lastIndexOf('/') + 1));
			if (action != null) {
				return action.execute(data);
			}
			return null;
		} catch (Exception e) {
			Throwable cause = e.getCause();
			throw new ServletException("Problem executing the action " + path, cause);
		}
	}

	private void renderView(Writer out, Map<String, Object> model, String path) throws ServletException {
		try {
			StringWriter writer = new StringWriter();
			threadData.get().out = writer;
			Template.render(path, model, writer);
			model.put("content", writer.toString());
			threadData.get().out = out;
			Template.render("layout", model, out);
		} catch (Exception e) {
			throw new ServletException("Problem rendering the page " + path, e);
		}
	}

}
