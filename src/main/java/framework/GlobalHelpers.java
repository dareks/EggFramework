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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.http.Cookie;

import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.code.morphia.utils.ReflectionUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import framework.validation.ActionValidationConfig;
import framework.validation.DecimalNumberValidator;
import framework.validation.Errors;
import framework.validation.ObjectValidationConfig;
import framework.validation.Validator;
import groovy.lang.Closure;

public class GlobalHelpers {

    public static final String ACTION_RETURNED_OBJECT = "Egg.ACTION_RETURNED_OBJECT";
    public static final String ACTION_URI = "Egg.ACTION_URI";
    public static final String ACTION_DATA = "Egg.ACTION_DATA";

    /**
     * Generates link (&lt;a href...&gt;) to action in current controller
     */
    public static String link(String action, String text) {
        return link(action, text, new HashMap<String, Object>());
    }

    public static String generateQueryString(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder("?");
        try {
            for (Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof Object[]) {
                    Object[] array = (Object[]) entry.getValue();
                    for (Object object : array) {
                        builder.append(URLEncoder.encode(entry.getKey(), "iso-8859-1"));
                        builder.append("=");
                        String value = String.valueOf(object);
                        builder.append(URLEncoder.encode(value, "iso-8859-1"));
                        builder.append("&");
                    }
                } else {
                    builder.append(URLEncoder.encode(entry.getKey(), "iso-8859-1"));
                    builder.append("=");
                    String value = String.valueOf(entry.getValue());
                    builder.append(URLEncoder.encode(value, "iso-8859-1"));
                    builder.append("&");
                }
            }
            return builder.toString();
        } catch (Exception e) {
            Loggers.TEMPLATE.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    // TODO Experimental
    public static String appendParams(String url, Map<String, Object> params) {
        if (!url.contains("?")) {
            return url + generateQueryString(params);
        } else {
            return url + "&" + generateQueryString(params).substring(1);
        }
    }

    public static String link(String action, String text, Map<String, Object> params) {
        return link(req().getController(), action, text, params);
    }

    /**
     * Generates link (&lt;a href...&gt;)
     */
    public static String link(String controller, String action, String text) {
        return link(controller, action, text, new HashMap<String, Object>());
    }

    public static String link(String controller, String action, String text, Map<String, Object> params) {
        return String.format("<a href='%s%s/%s.html%s'>%s</a>", config("app.url"), controller, action, generateQueryString(params), text);
    }

    public static String action(String action) {
        return String.format("%s%s/%s.html", config("app.url"), req().getController(), action);
    }

    public static String action(String controller, String action) {
        return String.format("%s%s/%s.html", config("app.url"), controller, action);
    }

    public static String action(String controller, String action, Map<String, Object> params) {
        return String.format("%s%s/%s.html%s", config("app.url"), controller, action, generateQueryString(params));
    }

    public static String resource(String name) {
        return String.format("%s%s%s", Config.get("app.url"), "resources/", name);
    }

    public static String config(String key) {
        return Config.get(key);
    }

    public static String config(String key, String defaultValue) {
        return Config.get(key, defaultValue);
    }

    public static CharSequence parse(String file) {
        return parse(file, null);
    }

    public static CharSequence parse(String file, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        try {
            Template.render(file, model, writer);
        } catch (Exception e) {
            Loggers.TEMPLATE.error(e.getMessage(), e);
            return e.getMessage();
        }
        return writer.toString();
    }

    /** Shortcut for String.format */
    public static String f(String str, Object... args) {
        return String.format(str, args);
    }

    public static Response renderAction(String controller, String action) {
        Response response = new Response();
        response.action = "/" + controller + "/" + action + ".html";
        return response;
    }

    public static Response renderAction(String action) {
        return renderAction(req().getController(), action);
    }

    public static Response renderPartial(String partial) {
        return renderPartial(req().getController(), partial);
    }

    public static Response renderPartial(String controller, String partial) {
        Response response = new Response();
        response.template = "/" + controller + "/" + partial;
        response.partial = true;
        return response;
    }

    public static Response render(String template) {
        Response response = new Response();
        response.template = "/" + req().getController() + "/" + template;
        return response;
    }

    public static Response renderText(String text) {
        Response response = new Response();
        response.contentType = "text/plain; charset=utf-8";
        response.text = text;
        return response;
    }

    public static Response renderBytes(byte[] bytes) {
        Response response = new Response();
        response.bytes = bytes;
        return response;
    }

    // TODO Optimize
    public static Response renderJSON(Object object) {
        Response response = new Response();
        response.contentType = "text/plain; charset=utf-8";
        response.text = toJSON(object);
        return response;
    }

    // TODO Optimize
    public static String toJSON(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            Loggers.TEMPLATE.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    public static Redirect redirect() {
        return new Redirect();
    }

    public static Request req() {
        return FrontController.threadData.get().request;
    }

    public static Request attr(String key, Object value) {
        return req().set(key, value);
    }

    public static <T> T attr(String key) {
        return (T) req().get(key);
    }

    public static Flash flash(String key, Object value) {
        return flash().set(key, value);
    }

    public static <T> T flash(String key) {
        return (T) flash().get(key);
    }

    public static Writer out() {
        return FrontController.threadData.get().getOut();
    }

    public static void out(String f, Object... args) throws IOException {
        FrontController.threadData.get().getOut().write(f(f, args));
    }

    public static Params params() {
        return FrontController.threadData.get().params;
    }

    public static String param(String name) {
        return FrontController.threadData.get().params.get(name);
    }

    public static <T> T session(String name) {
        return FrontController.threadData.get().session.get(name);
    }

    public static <T> Session session(String name, T value) {
        return FrontController.threadData.get().session.set(name, value);
    }

    public static Session session() {
        return FrontController.threadData.get().session;
    }

    public static long paramAsLong(String name) {
        return Long.valueOf(param(name));
    }

    public static Long paramAsLong(String name, Long defaultValue) {
        try {
            return paramAsLong(name);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int paramAsInt(String name) {
        return Integer.valueOf(param(name));
    }

    public static Integer paramAsInt(String name, Integer defaultValue) {
        try {
            return paramAsInt(name);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // TODO WHAT ABOUT NESTED PROPERTIES?
    public static <T> T paramsAsBean(Class<T> clazz) {
        Map<String, String[]> map = params().getMap();
        try {
            T bean = clazz.newInstance();
            for (String property : map.keySet()) {
                String[] values = map.get(property);
                if (values.length == 1) {
                    setValueOfFieldOrProperty(bean, property, values[0]);
                } else {
                    setValueOfFieldOrProperty(bean, property, values);
                }
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Flash flash() {
        return FrontController.threadData.get().flash;
    }

    public static Cookies cookies() {
        return FrontController.threadData.get().cookies;
    }

    public static Cookie cookie(String name) {
        return FrontController.threadData.get().cookies.get(name);
    }

    public static String cookieValue(String name) {
        Cookie cookie = FrontController.threadData.get().cookies.get(name);
        return cookie != null ? cookie.getValue() : null;
    }

    private static String getRefererClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String thisClassName = GlobalHelpers.class.getName();
        for (int t = 1; t < stackTrace.length; t++) {
            if (!stackTrace[t].getClassName().startsWith(thisClassName)) {
                return stackTrace[t].getClassName();
            }
        }
        return stackTrace[stackTrace.length - 1].getClassName();
    }

    // validation methods

    public static Validator required(String field) {
        return registerValidator(field, requiredValidator);
    }

    public static <T extends Validator> T registerValidator(String field, T validator) {
        ObjectValidationConfig.get(getRefererClass()).add(field, validator);
        return validator;
    }

    public static void required(String... fields) {
        if (fields != null) {
            for (String field : fields) {
                required(field);
            }
        }
    }

    public static DecimalNumberValidator decimalNumber(String field) {
        return registerValidator(field, new DecimalNumberValidator());
    }

    public static Errors validate(Object o) {
        return ObjectValidationConfig.get(o.getClass().getName()).validate(o);
    }

    public static ActionValidationConfig validateParams(String action, Class<?> clazz) {
        String controllerClass = getRefererClass();
        ObjectValidationConfig config = ObjectValidationConfig.get(clazz.getName());
        String path = createPath(controllerClass, action);
        return ActionValidationConfig.get(path).setValidators(config.getValidators());
    }

    public static String createPath(String controllerClassName, String action) {
        String path = "/" + controllerClassName.substring(controllerClassName.lastIndexOf('.') + 1, controllerClassName.indexOf("Controller")) + "/" + action;
        return path.substring(0, 2).toLowerCase() + path.substring(2);
    }

    public static Errors validateParams(Class<?> clazz) {
        return ObjectValidationConfig.get(clazz.getName()).validate(params());
    }

    public static Errors validate(String fieldName, Object fieldValue, Validator... validators) {
        Errors errors = new Errors();
        if (validators != null) {
            for (Validator validator : validators) {
                validator.validates(fieldName, fieldValue, errors);
            }
        }
        return errors;
    }

    public static ActionValidationConfig validateParam(String action, String name, Validator... validators) {
        String controllerClass = getRefererClass();
        String controller = uncapitalize(controllerClass.substring(controllerClass.lastIndexOf('.') + 1, controllerClass.indexOf("Controller")));
        return ActionValidationConfig.get("/" + controller + "/" + action).add(name, validators);
    }

    public static Errors validateParam(String name, Validator... validators) {
        return validate(name, param(name), validators);
    }

    public static final Validator requiredValidator = new Validator() {

        public void validates(String field, Object value, Errors errors) {
            if (value == null) {
                errors.add(field, field + " is required");
            }
            if (value instanceof String) {
                String str = (String) value;
                if (str.trim().isEmpty()) {
                    errors.add(field, field + " is required");
                }
            }
        }
    };

    public static final <T> EnumValidator<T> enumValidator(T... values) {
        return new EnumValidator<T>(values);
    }

    public static class EnumValidator<T> implements Validator {

        private T[] values;

        public EnumValidator(T... values) {
            this.values = values;
        }

        public void validates(String field, Object value, Errors errors) {
            if (value != null) {
                for (Object val : values) {
                    if (value.equals(String.valueOf(val))) {
                        return;
                    }
                }
                errors.add(field, field + " is not one of: " + values);
            }

        }

    }

    // end of validation

    // REFLECTION METHODS

    // TODO Following two methods should check first if there is a getter/setter
    // (before accessing the field)
    public static Object getValueOfFieldOrProperty(Object object, String name) {
        Field field;
        try {
            field = object.getClass().getDeclaredField(name);
            if (field != null) {
                field.setAccessible(true);
                return field.get(object);
            } else {
                return object.getClass().getDeclaredMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1)).invoke(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(f("Problem when loading property/field named %s from object %s", name, object), e);
        }
    }

    public static void setValueOfFieldOrProperty(Object object, String name, Object value) {
        Field field;
        try {
            field = findInheritedField(object.getClass(), name);
            if (field != null) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (value != null && !type.equals(value.getClass())) {
                    // TODO conversion
                    if (type.equals(String.class)) {
                        value = String.valueOf(value);
                    } else if (type.equals(short.class) || type.equals(Short.class)) {
                        value = Short.valueOf(String.valueOf(value));
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {
                        value = Integer.valueOf(String.valueOf(value));
                    } else if (type.equals(long.class) || type.equals(Long.class)) {
                        value = Long.valueOf(String.valueOf(value));
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        value = Boolean.valueOf(String.valueOf(value));
                    } else if (type.equals(float.class) || type.equals(Float.class)) {
                        value = Float.valueOf(String.valueOf(value));
                    } else if (type.equals(double.class) || type.equals(Double.class)) {
                        value = Double.valueOf(String.valueOf(value));
                    } else if (type.equals(BigDecimal.class)) {
                        value = new BigDecimal(String.valueOf(value));
                    }
                }
                field.set(object, value);
            } else {
                BeanUtils.setProperty(object, name, value);
            }
        } catch (Exception e) {
            // do nth, just skip not existing field (maybe log sth?)
        }
    }

    public static Action findAction(String className, String action) {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Method[] methods = clazz.getMethods();
            Method actionMethod = null;
            for (Method method : methods) {
                if (method.getName().equals(action)) {
                    actionMethod = method;
                }
            }
            if (actionMethod != null) {
                Action instance = new Action(action, clazz, actionMethod);
                return instance;
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Class<?>[] findClassesInPackage(String name) throws ClassNotFoundException {
        String classesDir = "target/classes/";
        File dir = new File(classesDir, name.replace('.', '/'));
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        Class<?>[] classes = new Class[files.length];
        int t = 0;
        for (File file : files) {
            String fullClassName = file.getPath().substring(classesDir.length()).replace('/', '.');
            String substring = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
            classes[t++] = Thread.currentThread().getContextClassLoader().loadClass(substring);
        }
        return classes;
    }

    public static Field findInheritedField(Class<?> clazz, String name) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        if (!clazz.equals(Object.class)) {
            return findInheritedField(clazz.getSuperclass(), name);
        }
        return null;
    }

    // end of reflection methods

    // closure functions

    public static String call(Closure closure) {
        Writer currentWriter = FrontController.threadData.get().getOut();
        StringWriter stringWriter = new StringWriter();
        FrontController.threadData.get().setOut(stringWriter);
        closure.call();
        stringWriter.flush();
        FrontController.threadData.get().setOut(currentWriter);
        return stringWriter.toString();
    }

    // end of closure functions

    public static Map<String, Object> map(Object... keyValuePairs) {
        if (keyValuePairs != null) {
            if (keyValuePairs.length % 2 != 0) {
                throw new RuntimeException("The number of parameters given to map function need to be even");
            }
            Map<String, Object> map = new HashMap<String, Object>(keyValuePairs.length / 2);
            for (int t = 0; t < keyValuePairs.length; t += 2) {
                map.put(String.valueOf(keyValuePairs[t]), keyValuePairs[t + 1]);
            }
            return map;
        }
        return new HashMap<String, Object>(0);
    }

    public static void vardump() throws IOException {
        Set<Entry<String, Object>> entrySet = req().getAttributes().entrySet();
        for (Entry<String, Object> entry : entrySet) {
            out().append("<b>").append(entry.getKey()).append("</b>").append(" = ").append(String.valueOf(entry.getValue())).append("<br />");
        }
    }

    public static String capitalize(Object o) {
        String str = String.valueOf(o);
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String uncapitalize(Object o) {
        String str = String.valueOf(o);
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String setCharAt(String str, int pos, char ch) {
        return str.substring(0, Math.min(pos, str.length())) + ch + str.substring(Math.min(pos + 1, str.length()));
    }

    public static boolean containsReference(Object[] array, Object o) {
        if (array != null) {
            for (Object object : array) {
                if (object == o) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static Object nvl(Object obj, Object replaceWith) {
        return obj == null ? replaceWith : obj;
    }

    public static int sum(Iterable<Integer> numbers) {
        int result = 0;
        for (Integer integer : numbers) {
            result += integer;
        }
        return result;
    }

    public static <T> Map<T, List<T>> group(Collection<T> collection) {
        // TODO OPTIMIZE
        Map<T, List<T>> map = Maps.newHashMap();
        for (T obj : collection) {
            if (!map.containsKey(obj)) {
                map.put(obj, Lists.newArrayList(obj));
            } else {
                map.get(obj).add(obj);
            }
        }
        return map;
    }

    public static <T> T get(Map<?, T> map, Object key, T defaultValue) {
        T val = map.get(key);
        return val == null ? defaultValue : val;
    }

    public static <K, V> Map<K, V> put(Map<K, V> map, K key, V value) {
        if (map != null) {
            map.put(key, value);
        }
        return map;
    }

    /**
     * VERY EXPERIMENTAL FEATURE. Generate piece of code depdening on passed object. It can generate ready to use HTML code for tables, forms, combo boxes etc.
     */
    public static void scaffold(String name, Object o) throws IOException, IllegalArgumentException, IllegalAccessException {
        Writer out = out();

        String type = null;
        Field[] fields = null;
        if (o instanceof Iterable) {
            Iterable iterable = (Iterable) o;
            Object first = Iterables.getFirst(iterable, null);
            fields = ReflectionUtils.getDeclaredAndInheritedFields(first.getClass(), true);
            type = "table";
        } else if (o instanceof Class) {
            fields = ReflectionUtils.getDeclaredAndInheritedFields((Class) o, true);
            type = "form";
        } else {
            fields = ReflectionUtils.getDeclaredAndInheritedFields(o.getClass(), true);
            type = "view";
        }
        out.append(parse("/_scaffold", map("type", type, "name", name, "fields", fields, "object", o)));
    }

    public static AsyncForward asyncForward() {
        AsyncContext ctx = req().startAsync();
        ctx.setTimeout(30000);
        AsyncForward forward = new AsyncForward(ctx);
        forward.action = "/" + req().getController() + "/" + req().getAction() + "Async.html";
        return forward;
    }

    public static Object[] join(Object[] array, Object o) {
        Object[] result = Arrays.copyOf(array, array.length + 1, Object[].class);
        result[array.length] = (Object) o;
        return result;
    }

    // METHODS TO BE USED IN Application.start()

    /**
     * This method should be used in service.Application.start() method. It creates and add a rule to the routing.
     * 
     * @param matchPattern
     *            A pattern which will be matched against an action path. You can use literals and parameters here. Parameter must be prefixed by dollar "$" sign. Sample pattern:
     *            "/someLiteral/$param1". For more details see {@link Rule}
     */
    public static Rule match(String matchPattern) {
        Rule rule = new Rule(matchPattern);
        routing().addRule(rule);
        return rule;
    }

    // END OF METHODS TO BE USED IN Application.start()

    public static Routing routing() {
        return FrameworkServlet.ROUTING;
    }

}
