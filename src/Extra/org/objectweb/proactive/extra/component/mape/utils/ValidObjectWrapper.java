package org.objectweb.proactive.extra.component.mape.utils;

public class ValidObjectWrapper extends ObjectWrapper {

	private static final long serialVersionUID = 1L;
	private Object object;

	public ValidObjectWrapper(Object object) {
		this.object = object;
	}

	public Object getObject() throws WrongValueException {
		return object;
	}

	public boolean isValid() {
		return true;
	}
}
