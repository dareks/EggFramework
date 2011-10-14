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

import framework.validation.Errors;

// TODO Add support for getting HTTP headers
public class ThreadData {

    public final Request request;
    public final Params params = new Params();
    public final Session session = new Session();
    public final Flash flash = new Flash();
    public final Cookies cookies = new Cookies();
    public HttpServletResponse resp;
    private Writer out;
    private Errors errors;

    public ThreadData(Request request, HttpServletResponse servletResponse) {
        this.request = request;
        HttpServletRequest servletRequest = request.getRequest();
        params.request = servletRequest;
        cookies.request = servletRequest;
        cookies.response = servletResponse;
        session.request = servletRequest;
        this.resp = servletResponse;
        this.errors = new Errors();
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
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
