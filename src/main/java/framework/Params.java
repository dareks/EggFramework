package framework;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class Params {

	HttpServletRequest request;

	public String get(String name) {
		return request.getParameter(name);
	}

	public String[] getValues(String name) {
		return request.getParameterValues(name);
	}
	
	public Map<String, String[]> getMap() {
		return request.getParameterMap();
	}
	
}
