package org.objectweb.proactive.extensions.autonomic.controllers.utils;

import java.io.Serializable;

public class ValidWrapper<T extends Serializable> implements Wrapper<T> {

	private static final long serialVersionUID = 1L;
	private T value;
	private String msg;

	public ValidWrapper(T value) {
		this(value, "valid");
	}
	
	public ValidWrapper(T value, String message) {
		this.value = value;
		this.msg = message;
	}

	/** {@inheritDoc} */
	@Override
	public T get() {
		return value;
	}

    /** {@inheritDoc} */
	@Override
	public boolean isValid() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return msg;
	}

}
