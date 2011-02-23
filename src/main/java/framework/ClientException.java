package framework;

/**
 * If this exception is thrown by action then 400 error is sent to the browser
 */
public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientException() {
		super();
	}

	public ClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientException(String message) {
		super(message);
	}

	public ClientException(Throwable cause) {
		super(cause);
	}

}
