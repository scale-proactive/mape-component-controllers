package examples.md5cracker.cracker;

import org.objectweb.fractal.api.control.AttributeController;

public interface CrackerAttributes extends AttributeController {

	public double getNumberOfSolvers();
	public void setNumberOfSolvers(double numberOfSolvers);

}
