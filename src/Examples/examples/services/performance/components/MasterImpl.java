package examples.services.performance.components;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import examples.services.Service;
import examples.services.performance.PSCST;


public class MasterImpl implements Service, MasterAttributes, BindingController {

	public static final String COMP_NAME = "solver-manager-comp";
	
	private SlaveMulticast slaves;

	private double numberOfWorkers = 1, idNumber = 0;


	@Override
	public Wrapper<String> crack(byte[] hash, int maxLength) {

		long possibilities = 0;
		for (int i = 1; i <= maxLength; i++) {
			possibilities += Math.pow(PSCST.ALPHA.length(), i);
		}

		List<Task> insts = new ArrayList<Task>();
		double slice = possibilities*1.0/numberOfWorkers;
		for (int w = 0; w < numberOfWorkers; w++) {
			long start = (long) Math.ceil(w*slice);
			long end = (long) Math.floor((w+1)*slice);
			insts.add(new Task(start, end, hash, maxLength));
		}

		List<Wrapper<String>> results = slaves.workOn(insts);
	
		for( Wrapper<String> w : results ) {
			if (w.isValid()) {
				return w;
			}
		}
		
		System.out.println("[SolverManager][Warning] key not found....");
		return new WrongWrapper<String>("Fail");
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
		if (name.equals(PSCST.WORKER_MULTICAST)) {
			slaves = (SlaveMulticast) itf;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				PSCST.WORKER_MULTICAST,
			};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(PSCST.WORKER_MULTICAST)) {
			return slaves;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(PSCST.WORKER_MULTICAST)) {
			slaves = null;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

}
