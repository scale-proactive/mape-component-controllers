package examples.services;

import org.objectweb.proactive.extra.component.mape.utils.ObjectWrapper;

public interface Service {

	public ObjectWrapper crack(byte[] hash, int maxLength);

}
