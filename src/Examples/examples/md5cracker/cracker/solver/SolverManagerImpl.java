package examples.md5cracker.cracker.solver;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import examples.md5cracker.cracker.CCST;
import examples.md5cracker.cracker.Cracker;


public class SolverManagerImpl implements Cracker, SolverAttributes, BindingController {

	public static final String COMP_NAME = "solver-manager-comp";
	
	private WorkerMulticast workers;

	private int alphabetBase = "abcdefghijklmnopqrstuvwxyz".length();

	private double numberOfWorkers = 1, idNumber = 0;


	@Override
	public StringWrapper crack(byte[] hash, int maxLength) {

		long possibilities = 0;
		for (int i = 1; i <= maxLength; i++) {
			possibilities += Math.pow(alphabetBase, i);
		}

		List<Instruction> insts = new ArrayList<Instruction>();
		double slice = possibilities*1.0/numberOfWorkers;
		for (int w = 0; w < numberOfWorkers; w++) {
			long start = (long) Math.ceil(w*slice);
			long end = (long) Math.floor((w+1)*slice);
			insts.add(new Instruction(start, end, hash, maxLength));
		}

		List<Wrapper<String>> results = workers.solve(insts);
	
		for( Wrapper<String> w : results ) {
			if (w.isValid()) {
				return new StringWrapper(w.getValue());
			}
		}
		
		System.out.println("[SolverManager][Warning] key not found....");
		return new StringWrapper("Fail");
	}

	// ATTRIBUTES CONTROLLER

	@Override
	public void setNumberOfWorkers(double number) {
		System.out.println(" NUMBER OF WORKERS CHANGES:::: OLD = " + numberOfWorkers  + " ---> NEW = " + number);
		numberOfWorkers = number;
	}

	@Override
	public double getNumberOfWorkers() {
		return numberOfWorkers;
	}

	@Override
	public void setId(double number) {
		idNumber = number;
	}

	@Override
	public double getId() {
		return idNumber;
	}

	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(CCST.WORKER_MULTICAST)) {
			workers = (WorkerMulticast) itf;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				CCST.WORKER_MULTICAST,
			};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(CCST.WORKER_MULTICAST)) {
			return workers;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(CCST.WORKER_MULTICAST)) {
			workers = null;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

}
