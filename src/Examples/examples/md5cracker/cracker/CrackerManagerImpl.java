package examples.md5cracker.cracker;

import java.io.Serializable;


import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;





public class CrackerManagerImpl implements Cracker, CrackerAttributes, BindingController, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String COMP_NAME = "cracker-manager-comp";
	
	private SolverMulticast solvers;
	private SolverAttributesMulticast solversAttributes;
	private double numberOfSolvers = 0; // default value

	@Override
	public void start() {
		solvers.start();
	}

	// CRACKER ATTRIBUTES
	
	@Override
	public double getNumberOfSolvers() {
		return numberOfSolvers;
	}

	@Override
	public void setNumberOfSolvers(double numberOfSolvers) {
		this.numberOfSolvers = numberOfSolvers;
	}

	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(SolverMulticast.ITF_NAME)) {
			solvers = (SolverMulticast) itf;
		} else if (name.equals(SolverAttributesMulticast.ITF_NAME)) {
			solversAttributes = (SolverAttributesMulticast) itf;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				SolverMulticast.ITF_NAME, SolverAttributesMulticast.ITF_NAME };
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SolverMulticast.ITF_NAME)) {
			return solvers;
		} else if (name.equals(SolverAttributesMulticast.ITF_NAME)) {
			return solversAttributes;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SolverMulticast.ITF_NAME)) {
			solvers = null;
		} else if (name.equals(SolverAttributesMulticast.ITF_NAME)) {
			solversAttributes = null;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

}
