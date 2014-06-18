package org.objectweb.proactive.extensions.autonomic.controllers.utils;

import java.io.Serializable;


public abstract class ObjectWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	abstract public Object getObject() throws WrongValueException;
	abstract public boolean isValid();

	public Object getObjectOrNull() {
		try {
			return getObject();
		} catch(WrongValueException e) {
			return null;
		}
	}

}
