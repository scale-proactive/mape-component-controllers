package examples.md5cracker.cracker;

import java.io.Serializable;


import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;

import examples.md5cracker.cracker.solver.TaskRepository;




public class CrackerManagerImpl implements Cracker, BindingController, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String COMP_NAME = "cracker-manager-comp";

	SolverMulticast solvers;
	SolverAttributesMulticast solversAttributes;
	
	@Override
	public void start(String alphabet, int maxLength) {
		solvers.start(alphabet, maxLength);
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
