package examples.services;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;

public interface Service {

	public ObjectWrapper crack(byte[] hash, int maxLength);

}
