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

import static framework.GlobalHelpers.*;

import java.io.FileNotFoundException;
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

// TODO Access to partial directly from browser should not be possible
public class FrontController {

    private static final long serialVersionUID = 1L;
    // TODO MOVE THIS TO SOME OTHER PLACE WHICH IS NOT DEPENDENT ON SERVLETS
    public static final ThreadLocal<ThreadData> threadData = new ThreadLocal<ThreadData>();

    public void service(HttpServletRequest req, HttpServletResponse res, ServletContext ctx, Routing routing) throws ServletException, IOException {
        res.setCharacterEncoding("utf-8");
        String path = (String) req.getAttribute(ACTION_URI);
        if (path == null) {
            String servletPath = req.getServletPath();
            if (servletPath.indexOf('.') > -1) {
                path = servletPath.substring(0, servletPath.indexOf('.'));
            } else {
                path = servletPath;
            }
        }

        long before = System.nanoTime();
        Request request = routing.route(path, req);
        long diff = System.nanoTime() - before;
        Loggers.BENCHMARK.info(f("Routing time: %d us", diff / 1000));
        if (request == null) {
            res.sendError(404);
            return;
        }
        req = request.getRequest();
        ThreadData data = new ThreadData(request, res);
        threadData.set(data);

        try {
            Errors errors = data.request.get("errors");
            if (errors == null) {
                errors = new Errors();
            }
            data.setErrors(errors);
            data.request.set("errors", errors);
            data.request.set("params", data.params);
            data.request.set("messages", new ArrayList<String>());
            data.flash.loadFromSession(req);
            data.request.set("session", data.session);
            initClass(request.getController());
            Response response = runBefore(request.getController(), data);
            if (response == null) {
                if (!errors.hasErrors()) {
                    ActionValidationConfig validationsConfig = ActionValidationConfig.get(request.getPath());
                    validationsConfig.validate(data.params, errors);
                    if (errors.hasErrors()) {
                        path = validationsConfig.getInputPath();
                        req.getRequestDispatcher(path + ".html").forward(req, res);
                        return;
                    }
                }
                response = runAction(request, data);
                if (response != null) {
                    if (response instanceof AsyncForward) {
                        return;
                    }
                    if (response.isForward()) {
                        req.getRequestDispatcher(response.action).forward(req, res);
                        return;
                    }
                    if (response.isRedirect()) {
                        sendRedirect(res, data, response);
                        return;
                    }
                }
            } else {
                if (response instanceof AsyncForward) {
                    return;
                }
                if (response.isForward()) {
                    req.getRequestDispatcher(response.action).forward(req, res);
                    return;
                }
                if (response.isRedirect()) {
                    sendRedirect(res, data, response);
                    return;
                }
            }
            if (response != null) {
                if (response.bufferSize != null) {
                    res.setBufferSize(response.bufferSize);
                }
                res.setContentType(response.contentType);
                res.setStatus(response.status);
            }
            boolean servletIncluded = req.getAttribute(ACTION_URI) != null;
            if (!servletIncluded && (response == null || response.template != null)) {
                Map<String, Object> model = data.request.getAttributes();
                String template = response != null ? response.template : request.getPath();
                renderTemplate(res.getWriter(), model, template, response);
            } else if (response != null && response.text != null) {
                res.getWriter().print(response.text);
            } else if (response != null && response.bytes != null) {
                res.getOutputStream().write(response.bytes);
                res.getOutputStream().flush();
            } else if (servletIncluded) {
                req.setAttribute(ACTION_RETURNED_OBJECT, response != null ? response.singleObject : null);
            }
        } catch (FileNotFoundException e) {
            res.sendError(404, e.getMessage());
        } catch (IOException e) {
            Loggers.CONTROLLER.error(e.getMessage(), e);
            throw e;
        } catch (ServletException e) {
            Loggers.CONTROLLER.error(e.getMessage(), e);
            throw e;
        } finally {
            data.flash.saveToSession(req);
            threadData.remove();
        }
    }

    private void sendRedirect(HttpServletResponse res, ThreadData data, Response response) throws IOException {
        if (data.flash.hasCurrentAttributes()) {
            res.sendRedirect(appendParams(response.redirect, map(Flash.FLASHID_PARAM, data.flash.flashId)));
        } else {
            res.sendRedirect(response.redirect);
        }
    }

    /**
     * Initializes controller class - static block will be executed
     */
    private void initClass(String controller) throws ServletException {
        String clazz = classForController(controller);
        try {
            Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            // No need to have a controller class
        }
    }

    private String classForController(String controller) {
        return "controllers." + controller.substring(0, 1).toUpperCase() + controller.substring(1) + "Controller";
    }

    private Response runBefore(String controller, ThreadData data) throws ServletException {
        try {
            Action before = findAction(classForController(controller), "before");
            if (before != null) {
                return before.execute(data);
            }
            return null;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            throw new ServletException("Problem executing before in controller " + controller, cause);
        }
    }

    private Response runAction(Request req, ThreadData data) throws ServletException {
        try {
            Action action = findAction(classForController(req.getController()), req.getAction());
            if (action != null) {
                return action.execute(data);
            }
            return null;
        } catch (Exception e) {
            // Throwable cause = e.getCause();
            throw new ServletException("Problem executing the action " + req.getAction(), e);
        }
    }

    private void renderTemplate(Writer out, Map<String, Object> model, String path, Response response) throws ServletException, FileNotFoundException {
        try {
            StringWriter writer = new StringWriter();
            threadData.get().setOut(writer);
            Template.render(path, model, writer);
            model.put("content", writer.toString());
            threadData.get().setOut(out);
            String layoutTemplate1 = "/" + path.substring(1, Math.max(1, path.lastIndexOf('/'))) + "/layout";
            String layoutTemplate2 = "/layout";
            String layoutTemplate = null;
            if (Template.exists(layoutTemplate1)) {
                layoutTemplate = layoutTemplate1;
            } else if (Template.exists(layoutTemplate2)) {
                layoutTemplate = layoutTemplate2;
            }
            boolean isPartial = (response != null && response.partial) || path.substring(path.lastIndexOf('/') + 1).charAt(0) == '_';
            if (layoutTemplate != null && !isPartial) {
                Template.render(layoutTemplate, model, out);
            } else {
                out.write(writer.toString());
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Problem rendering the page " + path, e);
        }
    }

}
