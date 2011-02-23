package framework;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import services.Application;

public class ThreadData {

	public final Request request = new Request();
	public final Params params = new Params();
	public final Session session = new Session();
	public final Flash flash = new Flash();
	public final Cookies cookies = new Cookies();
	public Writer out;

	public ThreadData(HttpServletRequest req, HttpServletResponse resp, Writer writer) {
		params.request = req;
		request.request = req;
		cookies.request = req;
		cookies.response = resp;
		flash.request = req;
		session.request = req;
		out = writer;
	}

}
