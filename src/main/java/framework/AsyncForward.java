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
