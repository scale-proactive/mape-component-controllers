package examples.services.autoadaptable.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongObjectWrapper;

import examples.services.autoadaptable.AASCST;

public class MasterImpl implements Solver, MasterAttributes, BindingController, Serializable {

	private static final long serialVersionUID = 1L;

	private SlaveMulticast slaves;
	private double numberOfSlaves = 0;

	@Override
	public String[] listFc() { return new String[] { AASCST.SLAVE, AASCST.SLAVE_MULTICAST }; }

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SLAVE_MULTICAST)) {
			return slaves;
		}
		throw new NoSuchInterfaceException("[@ MasterImpl] " + clientItfName);
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SLAVE_MULTICAST)) {
			slaves = (SlaveMulticast) serverItf;
		} else {
			throw new NoSuchInterfaceException("[@ MasterImpl] " + clientItfName);
		}
	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SLAVE_MULTICAST)) {
			slaves = null;
		} else {
			throw new NoSuchInterfaceException("[@ MasterImpl] " + clientItfName);
		}
	}

	@Override
	public ObjectWrapper crack(long from, long to, byte[] hash, int maxLength) {

		if (numberOfSlaves < 1) {
			return new WrongObjectWrapper("I dont have slaves! I cant do nothing");
		}
		
		long pSet = to - from + 1; // +1 because the "to" is included
		double ration = pSet * 1.0 / numberOfSlaves;
		
		List<Task> tasks = new ArrayList<Task>();

		//String msg = "";
		for (int i = 0; i < numberOfSlaves; i++) {

			long start = from + (long) Math.ceil(ration * i);
			long end = i == numberOfSlaves - 1 ? to : from + (long) Math.floor(ration * (i + 1));
			tasks.add(new Task(start, end, hash, maxLength));
			
			//msg += "(" + start + ", " + end + ") ";
		}

		//System.out.println(msg);
		List<ObjectWrapper> results = slaves.workOn(tasks);

		ObjectWrapper validResult = null;

		for (ObjectWrapper ow : results) {
			if (ow.isValid()) {
				validResult = ow;
			} // wait for the others anyway
		}
	
		return validResult != null ? validResult : new WrongObjectWrapper("Not Found");
	}

	@Override
	public double getSlavesNumber() {
		return numberOfSlaves;
	}

	@Override
	public void setSlavesNumber(double slavesNumber) {
		numberOfSlaves = slavesNumber;
	}

}
