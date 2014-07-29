package examples.services.performance.components;

import org.objectweb.fractal.api.control.AttributeController;

public interface MasterAttributes extends AttributeController {

	public void setNumberOfWorkers(double number);
	public double getNumberOfWorkers();

	public void setId(double number);
	public double getId();

}
