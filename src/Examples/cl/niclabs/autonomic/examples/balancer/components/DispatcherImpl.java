package cl.niclabs.autonomic.examples.balancer.components;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.balancer.BalancerCST;


public class DispatcherImpl implements DispatcherAttr, SolverItf, BindingController {

	private long workersNumber = 0;
	private WorkerMulticastItf workers;

	@Override
	public Wrapper<String> solve(byte[] hash, int maxLength, long from, long to) {
		
		if (workersNumber <= 0) {
			System.err.println("[WARNING] A solver has 0 available workers, this shouldn't happens");
			return new WrongWrapper<String>("0 available workers, nothing to do");
		}

		double slice = 1.0 * (to - from + 1) / workersNumber;		
		List<Task> tasks = new ArrayList<Task>();

		for (int i = 0; i < workersNumber; i++) {
			long start = from + (long) Math.ceil(slice * i);
			long end = i == workersNumber - 1 ? to : from + (long) Math.floor(slice * (i + 1));
			tasks.add(new Task(hash, maxLength, start, end));
		}

		List<Wrapper<String>> results = workers.workOn(tasks);

		Wrapper<String> validResult = null;

		for (Wrapper<String> ow : results) {
			if (ow.isValid()) {
				validResult = ow;
			} // wait for the others anyway
		}

		return validResult != null ? validResult : new WrongWrapper<String>("FAIL", "Not Found");
	}

	@Override
	public double getWorkers() {
		return workersNumber;
	}

	@Override
	public void setWorkers(double numberOfWorkers) {
		workersNumber = Math.round(numberOfWorkers);
	}

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_WORKER_MULTICAST)) {
			workers = (WorkerMulticastItf) itf;
		} else {
			throw new NoSuchInterfaceException("itf not found on Dispatcher: " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] { BalancerCST.ITF_WORKER_MULTICAST };
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_WORKER_MULTICAST)) {
			return workers;
		} else {
			throw new NoSuchInterfaceException("itf not found on Dispatcher: " + name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_WORKER_MULTICAST)) {
			workers = null;
		} else {
			throw new NoSuchInterfaceException("itf not found on Dispatcher: " + name);
		}
	}

}
