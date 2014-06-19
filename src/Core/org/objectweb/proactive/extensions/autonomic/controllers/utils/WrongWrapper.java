package org.objectweb.proactive.extensions.autonomic.controllers.utils;

import java.io.Serializable;

public class WrongWrapper<T extends Serializable> implements Wrapper<T> {

	private static final long serialVersionUID = 1L;
	private T value;
	private String msg;

	public WrongWrapper() {
		this(null, "wrong");
	}
	
	public WrongWrapper(String message) {
		this(null, message);
	}

	public WrongWrapper(T value, String message) {
		this.value = value;
		this.msg = message;
	}

	/** {@inheritDoc} */
	@Override
	public T getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return msg;
	}

}
