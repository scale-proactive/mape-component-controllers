package examples.services.autoadaptable.components;


import org.objectweb.proactive.extra.component.mape.utils.ObjectWrapper;

public interface Slave {

	public ObjectWrapper workOn(Task task);
}
