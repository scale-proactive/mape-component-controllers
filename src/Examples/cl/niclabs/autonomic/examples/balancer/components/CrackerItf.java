package cl.niclabs.autonomic.examples.balancer.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface CrackerItf {

	public Wrapper<String> crack(byte[] hash, int maxLength);

}
