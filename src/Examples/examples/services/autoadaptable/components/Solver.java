package examples.services.autoadaptable.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;

public interface Solver {

	public ObjectWrapper crack(long from, long to, byte[] hash, int maxLength);

}
