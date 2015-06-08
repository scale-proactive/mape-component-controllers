package cl.niclabs.autonomic.examples.qosaware.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface JonasItf {

	public Wrapper<String> processJonasCall(int val);

}
