package examples.md5cracker.cracker;

import org.objectweb.fractal.api.control.AttributeController;

public interface CrackerManagerAttributes extends AttributeController {

	public int getNumberOfSolvers();
	public void setNumberOfSolvers(int numberOfsolvers);

}
