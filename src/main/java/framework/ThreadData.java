package framework;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThreadData {

	public final Request request;
	public final Params params = new Params();
	public final Session session = new Session();
	public final Flash flash = new Flash();
	public final Cookies cookies = new Cookies();
	public HttpServletResponse resp;
	private Writer out;

	public ThreadData(HttpServletRequest req, HttpServletResponse resp, String controller, String action) {
		params.request = req;
		request = new Request(req, controller, action);
		cookies.request = req;
		cookies.response = resp;
		flash.request = req;
		session.request = req;
		this.resp = resp;
	}
	
	public void setOut(Writer out) {
		this.out = out;
	}
	
	public Writer getOut() {
		if (out == null) {
			try {
				out = resp.getWriter();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return out;
	}

}
