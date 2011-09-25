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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletResponse;

public class AsyncForward extends Response implements AsyncListener {

    boolean error;
    AsyncContext asyncContext;

    public AsyncForward(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        this.asyncContext.addListener(this);
    }

    public <T> void attr(String name, T value) {
        asyncContext.getRequest().setAttribute(name, value);
    }

    public <T> T attr(String name) {
        return (T) asyncContext.getRequest().getAttribute(name);
    }

    public synchronized boolean isResumePossible() {
        return !error;
    }

    public synchronized void resume() {
        if (isResumePossible()) {
            asyncContext.dispatch(action);
        }
    }

    public void onComplete(AsyncEvent event) throws IOException {
    }

    public void onTimeout(AsyncEvent event) throws IOException {
        synchronized (this) {
            error = true;
        }
        try {
            ((HttpServletResponse) asyncContext.getResponse()).sendError(504);
        } catch (IOException e) {
        } finally {
            asyncContext.complete();
        }
    }

    public void onError(AsyncEvent event) throws IOException {
        synchronized (this) {
            error = true;
        }
        event.getThrowable().printStackTrace();
        try {
            ((HttpServletResponse) asyncContext.getResponse()).sendError(500);
        } catch (IOException e) {
        } finally {
            asyncContext.complete();
        }
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
    }

}
