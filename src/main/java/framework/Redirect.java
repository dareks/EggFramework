package framework;

import java.util.Map;

public class Redirect {

	public Response action(String action) {
		return action(action, (Map<String, Object>) null);
	}

	public Response action(String action, Map<String, Object> params) {
		Response redirect = new Response();
		redirect.redirect = Config.get("app.url") + action + ".html";
		return redirect;
	}

	public Response action(String controller, String action) {
		return action(controller, action, null);
	}

	public Response action(String controller, String action, Map<String, Object> params) {
		Response redirect = new Response();
		redirect.redirect = Config.get("app.url") + controller + "/" + action + ".html";
		if (params != null) {
			redirect.redirect += GlobalHelpers.generateQueryString(params);
		}
		return redirect;
	}

	public Response url(String url) {
		Response redirect = new Response();
		redirect.redirect = url;
		return redirect;
	}

}
