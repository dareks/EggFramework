package framework;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;

public class Request {

	HttpServletRequest request;
	Map<String, Object> attributes = Maps.newHashMap();

	public Request set(String key, Object value) {
		attributes.put(key, value);
		return this;
	}
	
	public <T> T get(String key) {
		return (T) attributes.get(key);
	}

	public void set(Map<String, Object> map) {
		attributes.putAll(map);
	}
	
}
