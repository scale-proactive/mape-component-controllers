package cl.niclabs.autonomic.examples.qosaware.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface HTTPItf {

	public Wrapper<String> processHttpRequest(int val);

}
