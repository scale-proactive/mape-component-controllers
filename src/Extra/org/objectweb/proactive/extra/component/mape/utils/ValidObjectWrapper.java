package org.objectweb.proactive.extra.component.mape.utils;

public class ValidObjectWrapper implements ObjectWrapper {

	private static final long serialVersionUID = 1L;
	private Object object;

	public ValidObjectWrapper(Object object) {
		this.object = object;
	}

	@Override
	public Object getObject() throws WrongValueException {
		return object;
	}

	@Override
	public Object getObjectOrNull() {
		return object;
	}

	public boolean isValid() {
		return true;
	}
}
