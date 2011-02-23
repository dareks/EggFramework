package framework;

public class Redirect {

	public Response action(String action) {
		Response redirect = new Response(); 
		redirect.redirect = Config.get("app.url") + action + ".html";
		return redirect;
	}
}
