package framework;

import static framework.GlobalHelpers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.validation.ActionValidationConfig;
import framework.validation.Errors;

public class FrontController {

	private static final long serialVersionUID = 1L;
	// TODO MOVE THIS TO SOME OTHER PLACE WHICH IS NOT DEPENDENT ON SERVLETS
	public static final ThreadLocal<ThreadData> threadData = new ThreadLocal<ThreadData>();

	public void service(HttpServletRequest req, HttpServletResponse res, ServletContext ctx) throws ServletException, IOException {
		res.setCharacterEncoding("utf-8");
		String path = (String) (req.getAttribute(ACTION_URI) != null ? req.getAttribute(ACTION_URI) : req.getServletPath().substring(0, req.getServletPath().indexOf('.')));

		System.out.printf("\n------------------------------ %15s ------------------------------\n", path);
		long started = System.currentTimeMillis();

		final String controller = getController(path);
		final String action = getAction(path);
		ThreadData data = new ThreadData(req, res, controller, action);
		threadData.set(data);
		
		try {
			Errors errors = data.request.get("errors");
			if (errors == null) {
				errors = new Errors();
			}
			data.request.set("errors", errors);
			data.request.set("params", data.params);
			data.request.set("messages", new ArrayList<String>());
			data.request.set("flash", data.flash.pop());
			data.request.set("session", data.session);
			initClass(path);
			Response response = runBefore(path, data);
			if (response == null) {
				if (!errors.hasErrors()) { 
					ActionValidationConfig validationsConfig = ActionValidationConfig.get(path);
					validationsConfig.validate(data.params, errors);
					if (errors.hasErrors()) {
						path = validationsConfig.getInputPath(); 
						req.getRequestDispatcher(path + ".html").forward(req, res);
						return;
					}
				}
				response = runAction(path, data);
				if (response != null) {
					if (response.action != null) {
						req.getRequestDispatcher(response.action).forward(req, res);
						return;
					}
					if (response.redirect != null) {
						res.sendRedirect(response.redirect);
						return;
					}
				}
			} else {
				if (response.action != null) {
					req.getRequestDispatcher(response.action).forward(req, res);
					return;
				}
				if (response.redirect != null) {
					res.sendRedirect(response.redirect);
					return;
				}
			}
			if (response != null) {
				res.setContentType(response.contentType);
			}
			boolean servletIncluded = req.getAttribute(ACTION_URI) != null;
			if (!servletIncluded && (response == null || response.template != null)) {
				Map<String, Object> model = data.request.getAttributes();
				String template = response != null ? response.template : path;
				renderTemplate(res.getWriter(), model, template, response);
			} else if (response != null && response.text != null) {
				res.getWriter().print(response.text);
			} else if (response != null && response.bytes != null) {
				res.getOutputStream().write(response.bytes);
				res.getOutputStream().flush();
			} else if (servletIncluded) {
				req.setAttribute(ACTION_RETURNED_OBJECT, response != null ? response.singleObject : null);
			}
			System.out.printf("------------------------------ %12d ms ------------------------------ \n", System.currentTimeMillis() - started);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (ServletException e) {
			e.printStackTrace();
			throw e;
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
			// No need to have a controller class
		}		
	}
	
	private String getController(String path) throws ServletException {
		return path.substring(1, path.lastIndexOf('/'));
	}

	private String getAction(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}
	
	private String classForPath(String path) {
		String className = path.substring(1, path.lastIndexOf('/'));
		return "controllers." + className.substring(0, 1).toUpperCase() + className.substring(1) + "Controller";
	}

	private Response runBefore(String path, ThreadData data) throws ServletException {
		try {
			Action before = findAction(classForPath(path), "before");
			if (before != null) {
				return before.execute(data);
			}
			return null;
		} catch (Exception e) {
			Throwable cause = e.getCause();
			throw new ServletException("Problem executing the action " + path, cause);
		}
	}

	private Response runAction(String path, ThreadData data) throws ServletException {
		try {
			Action action = findAction(classForPath(path), getAction(path));
			if (action != null) {
				return action.execute(data);
			}
			return null;
		} catch (Exception e) {
			Throwable cause = e.getCause();
			throw new ServletException("Problem executing the action " + path, cause);
		}
	}

	private void renderTemplate(Writer out, Map<String, Object> model, String path, Response response) throws ServletException {
		try {
			StringWriter writer = new StringWriter();
			threadData.get().setOut(writer);
			Template.render(path, model, writer);
			model.put("content", writer.toString());
			threadData.get().setOut(out);
			String layoutTemplate = "/" + getController(path) + "/layout";
			boolean isPartial = (response != null && response.partial) || path.substring(path.lastIndexOf('/') + 1).charAt(0) == '_';
			if (Template.exists(layoutTemplate) && !isPartial) {
				Template.render(layoutTemplate, model, out);
			} else {
				out.write(writer.toString());
			}
		} catch (Exception e) {
			throw new ServletException("Problem rendering the page " + path, e);
		}
	}

}
