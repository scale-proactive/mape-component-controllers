package org.objectweb.proactive.extra.component.mape.utils;

import java.io.Serializable;


public interface ObjectWrapper extends Serializable {

	public Object getObject() throws WrongValueException;
	public Object getObjectOrNull();

}
