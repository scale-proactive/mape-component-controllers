package examples.services.performance.components;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public interface Slave {

	public Wrapper<String> workOn(Task task);

}
