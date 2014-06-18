package org.objectweb.proactive.extensions.autonomic.controllers.utils;

public class WrongValueException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongValueException(String msg) {
		super(msg);
	}

    public WrongValueException(Throwable cause) {
        super(cause);
    }

    public WrongValueException(String message, Throwable cause) {
        super(message, cause);
    }

}
