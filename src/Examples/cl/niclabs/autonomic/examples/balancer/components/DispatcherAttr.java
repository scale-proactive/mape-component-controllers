package cl.niclabs.autonomic.examples.balancer.components;

import org.objectweb.fractal.api.control.AttributeController;

public interface DispatcherAttr extends AttributeController {

	/** returns the number of available workers */
	public double getWorkers();

	/** set the number of available workers */
	public void setWorkers(double numberOfWorkers);

}
