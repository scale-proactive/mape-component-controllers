package cl.niclabs.autonomic.examples.balancer.components;

import java.util.ArrayList;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.balancer.BalancerCST;


public class BalancerImpl implements BalancerAttr, CrackerItf, BindingController {

	private double p1 = 0.334;
	private double p2 = 0.667;

	private SolverItf s1, s2, s3;


	@Override
	public Wrapper<String> crack(byte[] hash, int maxLength) {

		if (BalancerCST.DEBUG) System.out.println("... Entrando a balancer ...");

		long pos = 0;
		for (int i = 1; i <= maxLength; i++) pos += Math.pow(BalancerCST.ALPHABET.length(), i);

		ArrayList<Wrapper<String>> results = new ArrayList<Wrapper<String>>();

		// to solver 1
		long start = 0;
		long end = (long) Math.floor(pos * p1);
		results.add(s1.solve(hash, maxLength, start, end));

		// to solver 2
		start = (long) Math.ceil(pos * p1);
		end = (long) Math.floor(pos * p2);
		if (start == end) start++;
		results.add(s2.solve(hash, maxLength, start, end));

		// to solver 3
		start = (long) Math.ceil(pos * p2);
		end = pos;
		if (start == end) start++;
		results.add(s3.solve(hash, maxLength, start, end));

		if (BalancerCST.DEBUG) System.out.println("... checking results en balancer ...");
		Wrapper<String> ok = null;
		for (Wrapper<String> r : results) {
			if (r.isValid()) {
				ok = r;
			}
		}
		
		return ok != null ? ok : new WrongWrapper<String>("FAIL", "Solution not found...");
	}

	@Override
	public String getPoints() {
		return p1 + "u" + p2;
	}

	@Override
	public void setPoints(String points) {
		String[] split = points.split("u");
		if (split.length == 2) {
			double np1 = Double.parseDouble(split[0]);
			double np2 = Double.parseDouble(split[1]);
			if (np1 <= 1 && np2 <= 1 && np1 <= np2) {
				p1 = np1;
				p2 = np2;
				return;
			}
		}

		System.out.println("[WARNING] Balancer set points \"" + points + "\" fails");
	}

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_SOLVER_1)) {
			s1 = (SolverItf) itf;
		} else if (name.equals(BalancerCST.ITF_SOLVER_2)) {
			s2 = (SolverItf) itf;
		} else if (name.equals(BalancerCST.ITF_SOLVER_3)) {
			s3 = (SolverItf) itf;
		} else {
			throw new NoSuchInterfaceException("itf not found on Balancer: " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				BalancerCST.ITF_SOLVER_1, 
				BalancerCST.ITF_SOLVER_2,
				BalancerCST.ITF_SOLVER_3
		};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_SOLVER_1)) {
			return s1;
		} else if (name.equals(BalancerCST.ITF_SOLVER_2)) {
			return s2;
		} else if (name.equals(BalancerCST.ITF_SOLVER_3)) {
			return s3;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(BalancerCST.ITF_SOLVER_1)) {
			s1 = null;
		} else if (name.equals(BalancerCST.ITF_SOLVER_2)) {
			s2 = null;
		} else if (name.equals(BalancerCST.ITF_SOLVER_3)) {
			s3 = null;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

}
