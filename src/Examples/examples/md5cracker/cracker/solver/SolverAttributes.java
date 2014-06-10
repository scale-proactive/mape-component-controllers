package examples.md5cracker.cracker.solver;

import org.objectweb.fractal.api.control.AttributeController;

public interface SolverAttributes extends AttributeController {

	public static final String ITF_NAME = "solver-attributes-itf";

	public void setNumberOfWorkers(double number);
	public double getNumberOfWorkers();

	public void setId(double number);
	public double getId();

}
