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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;

public class Request {

	private HttpServletRequest request;
	private final String controller;
	private final String action;
	
	public Request(HttpServletRequest request, String controller, String action) {
		this.request = request;
		this.controller = controller;
		this.action = action;
	}
	
	AsyncContext startAsync() {
		return request.startAsync();
	}
	
	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}
	
	public String getURL() {
		return request.getRequestURL().toString();
	}
	
	public String getController() {
		return controller;
	}
	
	public String getAction() {
		return action;
	}
	
	public Request set(String key, Object value) {
		request.setAttribute(key, value);
		return this;
	}
	
	public <T> T get(String key) {
		return (T) request.getAttribute(key);
	}

	public void set(Map<String, Object> map) {
		for (Entry<String, Object> entry: map.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}
	
	public Map<String, Object> getAttributes() {
		Map<String, Object> map = Maps.newHashMap();
		Enumeration names = request.getAttributeNames();
		while(names.hasMoreElements()) {
			String key = (String) names.nextElement();
			Object value = request.getAttribute(key);
			map.put(key, value);
		}
		return map;
	}
	
}
