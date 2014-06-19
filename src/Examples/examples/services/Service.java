package examples.services;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface Service {

	public Wrapper<String> crack(byte[] hash, int maxLength);

}
