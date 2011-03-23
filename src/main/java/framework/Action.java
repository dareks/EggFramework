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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static framework.GlobalHelpers.*;

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
		Field field = findInheritedField(controller.getClass(), "app");
		if (field != null) {
			field.setAccessible(true);
			field.set(controller, FrameworkServlet.application);
		}
		Object response = null;
		if (method.getParameterTypes().length  == 0) {
			response = method.invoke(controller);
		} else {
			Object[] actionData = data.request.get(ACTION_DATA);
			response = method.invoke(controller, actionData);
			// TODO ADD FEATURE TO AUTOMATICALLY PASS HTTP PARAMETERS AS BEAN
		}
		if (response instanceof Response) {
			return (Response) response;
		} else if (response != null) {
			Response singleObjectResponse = new Response();
			singleObjectResponse.singleObject = response;
			return singleObjectResponse;
		} else {
			return null;
		}
	}
}
