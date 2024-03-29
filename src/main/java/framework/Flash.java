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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Flash {

    public static final String SESSION_PREFIX = "__flash_";
    public static final String FLASHID_PARAM = "__flashid";
    public static final String FLASH_ATTRIBUTE = "__flash";
    private Map<String, Object> previous = new HashMap<String, Object>(2);
    private Map<String, Object> current = new HashMap<String, Object>(2);
    public final long flashId;

    public Flash() {
        flashId = System.currentTimeMillis();
    }

    public void loadFromSession(HttpServletRequest req) {
        String flashIdParam = req.getParameter(FLASHID_PARAM);
        if (flashIdParam != null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                String prefix = SESSION_PREFIX + flashIdParam + "_";
                Enumeration<String> names = session.getAttributeNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    if (name.startsWith(prefix)) {
                        previous.put(name.substring(prefix.length()), session.getAttribute(name));
                        session.removeAttribute(name);
                    }
                }
            }
        }
    }

    public boolean hasCurrentAttributes() {
        return current.size() > 0;
    }

    public void saveToSession(HttpServletRequest req) {
        String prefix = SESSION_PREFIX + flashId + "_";
        for (Entry<String, Object> entry : current.entrySet()) {
            req.getSession().setAttribute(prefix + entry.getKey(), entry.getValue());
        }
    }

    public Flash set(String key, Object value) {
        current.put(key, value);
        return this;
    }

    public <T> T get(String key) {
        if (current.containsKey(key)) {
            return (T) current.get(key);
        } else {
            return (T) previous.get(key);
        }
    }

}
