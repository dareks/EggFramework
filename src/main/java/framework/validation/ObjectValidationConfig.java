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
package framework.validation;

import static framework.GlobalHelpers.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import framework.Params;

public class ObjectValidationConfig {

    private static final Map<String, ObjectValidationConfig> config = Maps.newHashMap();

    private Map<String, List<Validator>> validators = Maps.newHashMap();

    public Errors validate(Object object) {
        // TODO Add nested validation
        Errors errors = new Errors();
        Set<Entry<String, List<Validator>>> entrySet = validators.entrySet();
        for (Entry<String, List<Validator>> entry : entrySet) {
            List<Validator> fieldValidators = entry.getValue();
            for (Validator validator : fieldValidators) {
                String key = entry.getKey();
                Object fieldValue = key != null ? getValueOfFieldOrProperty(object, key) : object;
                validator.validates(key, fieldValue, errors);
            }
        }
        try {
            Method method = object.getClass().getDeclaredMethod("validate", Errors.class);
            method.invoke(object, errors);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return errors;
    }

    public Errors validate(Params params) {
        Map<String, Object> mapToValidate = Maps.newHashMap();
        Map<String, String[]> map = params().getMap();
        for (Entry<String, String[]> entry : map.entrySet()) {
            Object value = (entry.getValue() != null && entry.getValue().length == 1) ? entry.getValue()[0] : entry.getValue();
            mapToValidate.put(entry.getKey(), value);
        }
        // TODO Add nested validation
        Errors errors = new Errors();
        Set<Entry<String, List<Validator>>> entrySet = validators.entrySet();
        for (Entry<String, List<Validator>> entry : entrySet) {
            List<Validator> fieldValidators = entry.getValue();
            for (Validator validator : fieldValidators) {
                String key = entry.getKey();
                Object fieldValue = key != null ? mapToValidate.get(key) : mapToValidate;
                validator.validates(key, fieldValue, errors);
            }
        }
        return errors;
    }

    public void add(Validator validator) {
        List<Validator> list = validators.get(null);
        if (list == null) {
            validators.put(null, Lists.newArrayList(validator));
        } else {
            list.add(validator);
        }
    }

    public void add(String field, Validator validator) {
        List<Validator> list = validators.get(field);
        if (list == null) {
            validators.put(field, Lists.newArrayList(validator));
        } else {
            list.add(validator);
        }
    }

    public static ObjectValidationConfig get(String className) {
        try {
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        synchronized (ObjectValidationConfig.class) {
            if (!config.containsKey(className)) {
                config.put(className, new ObjectValidationConfig());
            }
        }
        return config.get(className);
    }

    public Map<String, List<Validator>> getValidators() {
        return validators;
    }

}
