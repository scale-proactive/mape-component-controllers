package examples.services.autoadaptable.components;

import java.io.Serializable;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongObjectWrapper;

import examples.services.Service;
import examples.services.autoadaptable.AASCST;


public class ManagerImpl implements Service, ManagerAttributes, BindingController, Serializable {

	private static final long serialVersionUID = 1L;

	private Solver solver1, solver2, solver3;
	private double p1 = 1.0/3, p2 = 2.0/3;

	@Override
	public String[] listFc() {
		return new String[] { AASCST.SOLVER_C1, AASCST.SOLVER_C2, AASCST.SOLVER_C3 };
	}

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SOLVER_C1))
			return solver1;
		else if (clientItfName.equals(AASCST.SOLVER_C2))
			return solver2;
		else if (clientItfName.equals(AASCST.SOLVER_C3))
			return solver3;
		else throw new NoSuchInterfaceException(clientItfName);
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SOLVER_C1))
			solver1 = (Solver) serverItf;
		else if (clientItfName.equals(AASCST.SOLVER_C2))
			solver2 = (Solver) serverItf;
		else if (clientItfName.equals(AASCST.SOLVER_C3))
			solver3 = (Solver) serverItf;
		else throw new NoSuchInterfaceException(clientItfName);
	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
		if (clientItfName.equals(AASCST.SOLVER_C1))
			solver1 = null;
		else if (clientItfName.equals(AASCST.SOLVER_C2))
			solver2 = null;
		else if (clientItfName.equals(AASCST.SOLVER_C3))
			solver3 = null;
		else throw new NoSuchInterfaceException(clientItfName);
	}

	@Override
	public ObjectWrapper crack(byte[] hash, int maxLength) {
		
		long possibilities = 0;
		for (int i = 1; i <= maxLength; i++)
			possibilities += Math.pow(AASCST.ALPHA.length(), i);

		assert(p1 <= 1);
		assert(p2 <= 1);
		assert(p1 < p2);
		
		long from = 0;
		long to = (long) Math.floor(possibilities * p1);
		ObjectWrapper r1 = solver1.crack(from, to, hash, maxLength);
		from = (long) Math.ceil(possibilities * p1);
		if (from == to) from++;
		to = (long) Math.floor(possibilities * p2);
		ObjectWrapper r2 = solver2.crack(from, to, hash, maxLength);
		from = (long) Math.ceil(possibilities * p2);
		if (from == to) from++;
		ObjectWrapper r3 = solver3.crack(from, possibilities, hash, maxLength);
	
		boolean b1 = r1.isValid();
		boolean b2 = r2.isValid();
		boolean b3 = r3.isValid();
		
		//System.out.println(msg + " [" + b1 + ", " + b2 + ", " + b3 + "]");
		return b1 ? r1 : b2 ? r2 : b3 ? r3 : new WrongObjectWrapper("solution not found...");
	}

	@Override
	public String getPoints() {
		return p1 + "u" + p2;
	}

	/**
	 * Format: "p1up2", with p1<p2
	 * Example: "0.4u0.7"
	 */
	@Override
	public void setPoints(String points) {
		String[] split = points.split("u");
		if (split.length == 2) {
			double np1 = Double.parseDouble(split[0]);
			double np2 = Double.parseDouble(split[1]);
			if (np1 <= 1 && np2 <= 1 && np1 < np2) {
				p1 = np1;
				p2 = np2;
				return;
			}
		}
		
		System.out.println("MANAGER ATTRIBUTES WARNING: set points \"" + points + "\" fails");
	}

}
