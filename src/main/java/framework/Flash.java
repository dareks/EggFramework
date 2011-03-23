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
