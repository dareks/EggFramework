package framework;

import java.beans.Beans;
import java.beans.beancontext.BeanContextSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.commons.beanutils.BeanUtils;

import com.google.code.morphia.utils.ReflectionUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.bcel.internal.generic.Type;

import framework.validation.ActionValidationConfig;
import framework.validation.DecimalNumberValidator;
import framework.validation.Errors;
import framework.validation.ObjectValidationConfig;
import framework.validation.Validator;
import groovy.lang.Closure;

public class GlobalHelpers {

	public static String link(String action, String text) {
		return String.format("<a href='%s%s.html'>%s</a>", config("app.url"), action, text);
	}

	public static String resource(String name) {
		return String.format("%s%s", config("app.url") + "resources/", name);
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

	public static Response render(String action) {
		Response response = new Response();
		response.forward = action;
		return response;
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
		return FrontController.threadData.get().out;
	}

	public static void out(String f, Object... args) throws IOException {
		FrontController.threadData.get().out.write(f(f, args));
	}

	public static Params params() {
		return FrontController.threadData.get().params;
	}

	public static String param(String name) {
		return FrontController.threadData.get().params.get(name);
	}

	public static long paramAsLong(String name) {
		return Long.valueOf(param(name));
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
		return registerValidator(field, decimalNumberValidator);
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
		return ActionValidationConfig.get(action).add(name, validators);
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

	public static final DecimalNumberValidator decimalNumberValidator = new DecimalNumberValidator();

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
	// end of reflection methods

	// closure functions

	public static String call(Closure closure) {
		Writer currentWriter = FrontController.threadData.get().out;
		StringWriter stringWriter = new StringWriter();
		FrontController.threadData.get().out = stringWriter;
		closure.call();
		stringWriter.flush();
		FrontController.threadData.get().out = currentWriter;
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
}
