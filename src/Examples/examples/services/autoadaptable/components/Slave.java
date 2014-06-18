package examples.services.autoadaptable.components;


import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;

public interface Slave {

	public ObjectWrapper workOn(Task task);
}
