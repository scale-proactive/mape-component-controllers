package examples.services.performance.components;

import java.io.Serializable;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

import examples.services.Service;
import examples.services.performance.PSCST;
import examples.services.performance.ServiceAttributes;

@DefineGroups({ @Group(name = "G1", selfCompatible = true) })
public class ManagerImpl implements Service, ServiceAttributes, BindingController, Serializable {
	
	private static final long serialVersionUID = 1L;

	private Serializable mutex = new Serializable() { private static final long serialVersionUID = 1L; };

	// shows the availability of a solver. If false, could mean both the solver is busy, or there is no solver.
	private boolean enable[] = new boolean[3];
	private Service solver[] = new Service[3];

	private double numberOfSolvers = 0;


	@SuppressWarnings("unused")
	private int counter = 0;

	@Override
	@MemberOf("G1")
	public Wrapper<String> crack(byte[] hash, int maxLength) {

		int id = -1;

		synchronized (mutex) {
			counter ++;
			while (true) {
				id = getEnableIndex();
				
				if (id >= 0) {
					enable[id] = false;
					break;
				}

				try {
					mutex.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// Force serialization
		// System.out.println("======== using solver" + (id + 1) + " activeTasks: " + counter);
		String result = solver[id].crack(hash, maxLength).getValue();
		
		synchronized (mutex) {
			counter --;
			enable[id] = true;
			mutex.notifyAll();
		}

		return new ValidWrapper<String>(result);
	}

	/** Returns the id of a enable solver, or -1 if there are all busy or disable */
	private int getEnableIndex() {
		for (int i = 0; i < enable.length ; i++)
			if (enable[i]) return i;

		return -1;
	}


	// CRACKER ATTRIBUTES
	
	@Override
	public double getNumberOfSolvers() {
		return numberOfSolvers;
	}

	@Override
	public void setNumberOfSolvers(double numberOfSolvers) {
		System.out.println("NUMBER OF SOLVERS CHANGES::::: OLD = " + this.numberOfSolvers + " ----> NEW = " + numberOfSolvers);
		this.numberOfSolvers = numberOfSolvers;
	}

	// BINDING CONTROLLER

	@Override
	public String[] listFc() {
		return new String[] {
				PSCST.SOLVER_C1,
				PSCST.SOLVER_C2,
				PSCST.SOLVER_C3,
			};
	}

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(PSCST.SOLVER_C1)) {
			return solver[0];
		} else if (clientItfName.equals(PSCST.SOLVER_C2)) {
			return solver[1];
		} else if (clientItfName.equals(PSCST.SOLVER_C3)) {
			return solver[2];
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
		if (clientItfName.equals(PSCST.SOLVER_C1)) {
			solver[0] = (Service) serverItf;
			enable[0] = true;
		} else if (clientItfName.equals(PSCST.SOLVER_C2)) {
			solver[1] = (Service) serverItf;
			enable[1] = true;
		} else if (clientItfName.equals(PSCST.SOLVER_C3)) {
			solver[2] = (Service) serverItf;
			enable[2] = true;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
		
		setNumberOfSolvers(getNumberOfSolvers() + 1);

	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(PSCST.SOLVER_C1)) {
			solver[0] = null;
			enable[0] = false;
		} else if (clientItfName.equals(PSCST.SOLVER_C2)) {
			solver[1] = null;
			enable[1] = false;
		} else if (clientItfName.equals(PSCST.SOLVER_C3)) {
			solver[2] = null;
			enable[2] = false;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
		
		setNumberOfSolvers(getNumberOfSolvers() - 1);
	}

}
