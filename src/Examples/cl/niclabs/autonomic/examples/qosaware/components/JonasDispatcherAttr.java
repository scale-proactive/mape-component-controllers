package cl.niclabs.autonomic.examples.qosaware.components;

import org.objectweb.fractal.api.control.AttributeController;

public interface JonasDispatcherAttr extends AttributeController {

	/** returns the number of available workers */
	public double getWorkers();

	/** set the number of available workers */
	public void setWorkers(double numberOfWorkers);

}
