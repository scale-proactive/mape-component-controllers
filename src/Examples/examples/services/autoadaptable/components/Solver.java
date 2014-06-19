package examples.services.autoadaptable.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface Solver {

	public Wrapper<String> crack(long from, long to, byte[] hash, int maxLength);

}
