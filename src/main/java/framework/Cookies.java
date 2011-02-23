package framework;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Cookies {

	HttpServletRequest request;
	HttpServletResponse response; 

	public Cookie get(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}
	
	public void set(Cookie cookie) {
		response.addCookie(cookie);
	}
	
	public Cookie set(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		set(cookie);
		return cookie;
	}
	
	public Cookie set(String name, String value, String path) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		set(cookie);
		return cookie;
	}
}
