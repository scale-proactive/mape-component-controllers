package cl.niclabs.autonomic.examples.qosaware.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface SpringooItf {

	public Wrapper<String> makeRequest(byte[] hash, int maxLength);

}
