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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.map.ObjectMapper;

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
				builder.append(URLEncoder.encode(entry.getKey(), "iso-8859-1"));
				builder.append("=");
				builder.append(URLEncoder.encode(String.valueOf(entry.getValue()), "iso-8859-1"));
				builder.append("&");
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static String link(String action, String text, Map<String, Object> params) {
		return String.format("<a href='%s%s/%s.html%s'>%s</a>", config("app.url"), req().getController(), action, generateQueryString(params), text);
	}

	/**
	 * Generates link (&lt;a href...&gt;)
	 */
	public static String link(String controller, String action, String text) {
		return String.format("<a href='%s%s/%s.html'>%s</a>", config("app.url"), controller, action, text);
	}
	
	public static String action(String action) {
		return String.format("%s%s/%s.html", config("app.url"), req().getController(), action);
	}
	
	public static String action(String controller, String action) {
		return String.format("%s%s/%s.html", config("app.url"), controller, action);
	}
	
	public static String resource(String name) {
		return String.format("%s%s%s", Config.get("app.url"), "resources/", name);
	}

	public static String config(String key) {
		return Config.get(key);
	}

	public static CharSequence parse(String file) {
		return parse(file, null);
	}

	public static CharSequence parse(String file, Map<String, Object> model) {
		StringWriter writer = new StringWriter();
		try {
			Template.render(file, model, writer);
		} catch (Exception e) {
			e.printStackTrace();
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
		return renderAction("/" + req().getController(), action);
	}

	public static Response renderPartial(String partial) {
		Response response = new Response();
		response.template = "/" + req().getController() + "/" + partial;
		response.partial = true;
		System.out.println(response.template);
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
	
	// TODO Optimize
	public static Response renderJSON(Object object) {
		Response response = new Response();
		response.contentType = "text/plain; charset=utf-8";
		response.text = toJSON(object);
		return response;
	}
	
	// TODO Optimize
	public static String toJSON(Object object) {
		try{
			return new ObjectMapper().writeValueAsString(object);
		} catch (Exception e) {
			e.printStackTrace();
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
		return req().get(key);
	}

	public static Flash flash(String key, Object value) {
		return flash().set(key, value);
	}

	public static <T> T flash(String key) {
		return flash().get(key);
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

	public static String session(String name) {
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

	public static int paramAsInt(String name) {
		return Integer.valueOf(param(name));
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

	// end of validation

	// REFLECTION METHODS

	// TODO PONIZSZE 2 METODY POWINNY SPRAWDZAC NAJPIERW CZY JEST GETTER/SETTER
	// A DOPIERO POZNIEJ SZUKAC POLA!
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
			field = object.getClass().getDeclaredField(name);
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
			throw new RuntimeException(f("Problem when setting property/field named %s from object %s", name, object), e);
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
		Map<String, Object> map = Maps.newHashMap();
		if (keyValuePairs != null) {
			if (keyValuePairs.length % 2 != 0) {
				throw new RuntimeException("The number of parameters given to map function need to be even");
			}
			for (int t = 0; t < keyValuePairs.length; t += 2) {
				map.put(String.valueOf(keyValuePairs[t]), keyValuePairs[t + 1]);
			}
		}
		return map;
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
	
	public static String nvl(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	
	public static int sum(Iterable<Integer> numbers) {
		int result = 0;
		for (Integer integer : numbers) {
			result += integer;
		}
		return result;
	}
	
}
