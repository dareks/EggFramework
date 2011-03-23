package framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

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
