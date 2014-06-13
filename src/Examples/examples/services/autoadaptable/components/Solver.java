package examples.services.autoadaptable.components;

import org.objectweb.proactive.extra.component.mape.utils.ObjectWrapper;

public interface Solver {

	public ObjectWrapper crack(long from, long to, byte[] hash, int maxLength);

}
