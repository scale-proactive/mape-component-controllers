package cl.niclabs.autonomic.examples.balancer.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface SolverItf {

	public Wrapper<String> solve(byte[] hash, int maxLength, long from, long to);

}
