/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
