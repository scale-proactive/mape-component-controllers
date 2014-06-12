package examples.md5cracker.cracker;

import java.io.Serializable;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

@DefineGroups({ @Group(name = "G1", selfCompatible = true) })
public class CrackerManagerImpl implements Cracker, CrackerAttributes, BindingController, Serializable {
	
	private static final long serialVersionUID = 1L;

	private Serializable mutex = new Serializable() { private static final long serialVersionUID = 1L; };

	// shows the availability of a solver. If false, could mean both the solver is busy, or there is no solver.
	private boolean enable[] = new boolean[3];
	private Cracker solver[] = new Cracker[3];

	private double numberOfSolvers = 0;


	private int counter = 0;

	@Override
	@MemberOf("G1")
	public StringWrapper crack(byte[] hash, int maxLength) {

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
		StringWrapper result = new StringWrapper(solver[id].crack(hash, maxLength).getStringValue());
		
		synchronized (mutex) {
			counter --;
			enable[id] = true;
			mutex.notifyAll();
		}

		return result;
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
				CCST.SOLVER_C1,
				CCST.SOLVER_C2,
				CCST.SOLVER_C3,
			};
	}

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(CCST.SOLVER_C1)) {
			return solver[0];
		} else if (clientItfName.equals(CCST.SOLVER_C2)) {
			return solver[1];
		} else if (clientItfName.equals(CCST.SOLVER_C3)) {
			return solver[2];
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
		if (clientItfName.equals(CCST.SOLVER_C1)) {
			solver[0] = (Cracker) serverItf;
			enable[0] = true;
		} else if (clientItfName.equals(CCST.SOLVER_C2)) {
			solver[1] = (Cracker) serverItf;
			enable[1] = true;
		} else if (clientItfName.equals(CCST.SOLVER_C3)) {
			solver[2] = (Cracker) serverItf;
			enable[2] = true;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
		
		setNumberOfSolvers(getNumberOfSolvers() + 1);

	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(CCST.SOLVER_C1)) {
			solver[0] = null;
			enable[0] = false;
		} else if (clientItfName.equals(CCST.SOLVER_C2)) {
			solver[1] = null;
			enable[1] = false;
		} else if (clientItfName.equals(CCST.SOLVER_C3)) {
			solver[2] = null;
			enable[2] = false;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
		
		setNumberOfSolvers(getNumberOfSolvers() - 1);
	}

}
