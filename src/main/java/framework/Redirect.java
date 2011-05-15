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

import java.util.Map;
import static framework.GlobalHelpers.*;

public class Redirect {

	public Response action(String action) {
		return action(action, (Map<String, Object>) null);
	}

	public Response action(String action, Map<String, Object> params) {
		return action(req().getController(), action, params);
	}

	public Response action(String controller, String action) {
		return action(controller, action, null);
	}

	public Response action(String controller, String action, Map<String, Object> params) {
		Response redirect = new Response();
		redirect.redirect = Config.get("app.url") + controller + "/" + action + ".html";
		if (params != null) {
			redirect.redirect += generateQueryString(params);
		}
		return redirect;
	}

	public Response url(String url) {
		Response redirect = new Response();
		redirect.redirect = url;
		return redirect;
	}

}
