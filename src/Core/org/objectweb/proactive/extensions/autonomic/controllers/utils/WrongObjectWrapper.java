package org.objectweb.proactive.extensions.autonomic.controllers.utils;

public class WrongObjectWrapper extends ObjectWrapper {

	private static final long serialVersionUID = 1L;
	private WrongValueException exception;

	public WrongObjectWrapper(String msg) {
		exception = new WrongValueException(msg);
	}

	public WrongObjectWrapper(Throwable cause) {
		exception = new WrongValueException(cause);
	}

	public WrongObjectWrapper(String msg, Throwable cause) {
		exception = new WrongValueException(msg, cause);
	}

	public Object getObject() throws WrongValueException {
		throw exception;
	}

	public boolean isValid() {
		return false;
	}

}
