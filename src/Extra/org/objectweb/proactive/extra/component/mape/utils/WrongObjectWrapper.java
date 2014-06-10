package org.objectweb.proactive.extra.component.mape.utils;

public class WrongObjectWrapper implements ObjectWrapper {

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

	@Override
	public Object getObject() throws WrongValueException {
		throw exception;
	}

	@Override
	public Object getObjectOrNull() {
		return null;
	}

}
