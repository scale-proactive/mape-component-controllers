package org.objectweb.proactive.extensions.autonomic.exceptions;

public class NotAutonomicException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotAutonomicException(String message) {
		super(message);
	}

	public NotAutonomicException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAutonomicException(Throwable cause) {
		super(cause);
	}

}
