package cl.niclabs.autonomic.examples.qosaware.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface JonasRetItf {

	public Wrapper<String> receiveResponse(int val);

}
