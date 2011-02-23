package framework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Action {

	public final String action;
	public final Class<?> clazz;
	public final Method method;

	public Action(String action, Class<?> clazz, Method method) {
		super();
		this.action = action;
		this.clazz = clazz;
		this.method = method;
	}

	public synchronized Response execute(ThreadData data) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Object controller = clazz.newInstance();
		// IoC by name or type
		Field[] fields = controller.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equals("application") || field.getType().getName().equals("services.Application")) {
				field.setAccessible(true);
				field.set(controller, FrameworkServlet.application);
				break;
			}
		}
		return (Response) method.invoke(controller);
	}
}
