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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Action {

    public final String action;
    public final Object controller;
    public final Method method;
    public final MethodAccess methodAccess;
    public final int methodIndex;

    public final boolean hasParameters;

    public Action(String action, Object controller, Method method, MethodAccess methodAccess, int methodIndex) {
        super();
        this.action = action;
        this.controller = controller;
        this.method = method;
        this.methodAccess = methodAccess;
        this.methodIndex = methodIndex;

        hasParameters = method != null ? method.getParameterTypes().length != 0 : false;
    }

    public Action(String action, Object controller, Method method) {
        this(action, controller, method, null, -1);
    }

    public Action(String action, Object controller) {
        this(action, controller, null, null, -1);
    }

    public synchronized Response execute(ThreadData data) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (method == null) {
            return null;
        }
        Object response = null;
        if (!hasParameters) {
            if (methodAccess == null) {
                response = method.invoke(controller);
            } else {
                response = methodAccess.invoke(controller, methodIndex);
            }
        } else {
            Object[] actionData = data.request.get(ACTION_DATA);
            if (methodAccess == null) {
                response = method.invoke(controller, actionData);
            } else {
                response = methodAccess.invoke(controller, methodIndex, actionData);
            }
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
