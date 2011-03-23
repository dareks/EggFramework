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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Maps;

public class Flash {

	HttpServletRequest request;
	static final String PREFIX = "__flash_";
	
	public synchronized Flash set(String key, Object value) {
		request.getSession().setAttribute(PREFIX + key, value);
		return this;
	}
	
	public synchronized <T> T get(String key) {
		if (request.getSession(false) != null) {
			return (T) request.getSession().getAttribute(PREFIX + key);
		}
		return null;
	}
	
	public synchronized Map<String, Object> pop() {
		Map<String, Object> map = Maps.newHashMap();
		HttpSession session = request.getSession(false);
		if (session != null) {
			Enumeration names = session.getAttributeNames();
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				if (key.startsWith(PREFIX)) {
					map.put(key.substring(PREFIX.length()), session.getAttribute(key));
					session.removeAttribute(key);
				}
			}
		}
		return map;
	}
	
}
