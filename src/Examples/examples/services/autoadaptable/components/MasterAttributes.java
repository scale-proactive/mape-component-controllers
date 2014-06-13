package examples.services.autoadaptable.components;

import org.objectweb.fractal.api.control.AttributeController;

public interface MasterAttributes extends AttributeController {

	public double getSlavesNumber();
	public void setSlavesNumber(double slavesNumber);

}
