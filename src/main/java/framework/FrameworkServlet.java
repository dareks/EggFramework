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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esotericsoftware.reflectasm.MethodAccess;

public class FrameworkServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final Routing ROUTING = new Routing();
    static Object application;
    static ServletContext SERVLET_CONTEXT;
    boolean started;

    static String FRONT_CONTROLLER_CLASS_NAME = "framework.FrontController";
    static String FRONT_CONTROLLER_METHOD_NAME = "service";

    // just for production fields:
    private Object frontController;
    private MethodAccess methodAccess;
    private int methodIndex;

    private void createAndStartApplication() throws ServletException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> appClass = Thread.currentThread().getContextClassLoader().loadClass("services.Application");
        application = appClass.newInstance();
        application.getClass().getMethod("start").invoke(application);
        ROUTING.addRule(new Rule("/$controller/$action/$id"));
        ROUTING.addRule(new Rule("/$controller/$action"));
        ROUTING.close();
        SERVLET_CONTEXT = getServletContext();
        started = true;

        if (Config.isInProductionMode()) {
            Class<?> controllerClass = Thread.currentThread().getContextClassLoader().loadClass(FRONT_CONTROLLER_CLASS_NAME);
            methodAccess = MethodAccess.get(controllerClass);
            methodIndex = methodAccess.getIndex(FRONT_CONTROLLER_METHOD_NAME, HttpServletRequest.class, HttpServletResponse.class, ServletContext.class, Routing.class);
            frontController = createFrontController();
        }
    }

    @Override
    public void destroy() {
        try {
            application.getClass().getMethod("stop").invoke(application);
        } catch (Exception e) {
            Loggers.CONTROLLER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        synchronized (this) {
            if (!started) {
                try {
                    createAndStartApplication();
                } catch (Exception e) {
                    throw new ServletException("Problem when creating application instance", e);
                }
            }
        }
        try {
            Object controller = getFrontController();
            invoke(FRONT_CONTROLLER_METHOD_NAME, controller, req, resp, getServletContext(), ROUTING);
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

    private Object getFrontController() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return Config.isInProductionMode() ? frontController : createFrontController();
    }

    private Object createFrontController() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> controllerClass = Thread.currentThread().getContextClassLoader().loadClass(FRONT_CONTROLLER_CLASS_NAME);
        Object controller = controllerClass.newInstance();
        return controller;
    }

    private void invoke(String methodName, Object controller, HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Routing routing) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (Config.isInProductionMode()) {
            methodAccess.invoke(controller, methodIndex, req, resp, ctx, routing);
        } else {
            Method method = controller.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, ServletContext.class, Routing.class);
            method.invoke(controller, req, resp, ctx, routing);
        }
    }

}
