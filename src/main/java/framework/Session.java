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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Session {

    HttpServletRequest request;

    public boolean exists() {
        return request.getSession(false) != null;
    }

    public Session set(String key, Object value) {
        request.getSession().setAttribute(key, value);
        return this;
    }

    public void invalidate() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public <T> T get(String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (T) session.getAttribute(key);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{<br />\n");
        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                builder.append(" &nbsp; ").append(name).append(" = ").append(session.getAttribute(name)).append("<br />\n");
            }
        }
        return builder.append("}").toString();
    }

}
