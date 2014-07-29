package examples.services.performance;

import org.objectweb.fractal.api.control.AttributeController;

public interface ServiceAttributes extends AttributeController {

	public double getNumberOfSolvers();
	public void setNumberOfSolvers(double numberOfSolvers);

}
