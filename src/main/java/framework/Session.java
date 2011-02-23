package framework;

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

	public <T> T get(String key) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return (T) session.getAttribute(key);
		}
		return null;
	}

}
