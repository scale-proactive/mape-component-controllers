package org.objectweb.proactive.extensions.autonomic.controllers.utils;

import java.io.Serializable;

public interface Wrapper<T extends Serializable> extends Serializable {

	/**
	 * Returns the value wrapped by this wrapper.
	 * 
	 * @return the value
	 */
	public T getValue();

	/**
	 * Indicated if this wrapper contains the expected value.
	 * 
	 * @return TRUE if it contains the expected value, FALSE otherwise.
	 */
	public boolean isValid();

	/**
	 * Get the associated message, used to attach information about this value.
	 * 
	 * @return the message
	 */
	public String getMessage();

}
