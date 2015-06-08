package cl.niclabs.autonomic.examples.qosaware.components;

import java.util.ArrayList;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.qosaware.SpringooCST;


//public class BalancerImpl implements BalancerAttr, CrackerItf, BindingController {
public class FrontalImpl implements SpringooItf, JonasRetItf, BindingController {

	private double p1 = 0.334;
	private double p2 = 0.667;

//	private SolverItf s1, s2, s3;
	private HTTPItf http, https;

	private int turn = 0;

	@Override
	public Wrapper<String> makeRequest(byte[] hash, int maxLength) {

		if (SpringooCST.DEBUG) System.out.println("[FrontalImpl] Making request ...");

		if (SpringooCST.DEBUG) System.out.println("[Frontal] Making a call to " + (turn==1?"HTTPS":"HTTP") + " ... ");

		long pos = 0;
		for (int i = 1; i <= maxLength; i++) pos += Math.pow(SpringooCST.ALPHABET.length(), i);

		ArrayList<Wrapper<String>> results = new ArrayList<Wrapper<String>>();

/*
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

		if (SpringooCST.DEBUG) System.out.println("... checking results en balancer ...");
		Wrapper<String> ok = null;
		for (Wrapper<String> r : results) {
			if (r.isValid()) {
				ok = r;
			}
		}
*/
		Wrapper<String> response;
		try {
			Thread.sleep(3000);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	Wrapper<String> ret = new ValidWrapper<String>("[Frontal] Returned from " + (turn==1?"HTTPS":"HTTP"), "Good");
		if(turn==1) {
			response = https.processHttpRequest(SpringooCST.ALPHABET.length());
		}
		else {
			response = http.processHttpRequest(SpringooCST.ALPHABET.length());
		}
		turn = (turn+1)%2;
		return ret;

//		return ok != null ? ok : new WrongWrapper<String>("FAIL", "Solution not found...");

	}

	@Override
	public Wrapper<String> receiveResponse(int val) {
		try {
			Thread.sleep(1000);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
		//System.out.println("[Frontal] ");
		return new ValidWrapper<String>("[FrontalImpl] DONE! Response " + val + " received!!", "Good! Received!");
	}

	//@Override
	public String getPoints() {
		return p1 + "u" + p2;
	}

	//@Override
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

		System.out.println("[WARNING] Frontal set points \"" + points + "\" fails");
	}

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_FRONTAL_HTTP)) {
			http = (HTTPItf) itf;
		} else if (name.equals(SpringooCST.ITF_FRONTAL_HTTPS)) {
			https = (HTTPItf) itf;
		} else {
			throw new NoSuchInterfaceException("itf not found on Balancer: " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				SpringooCST.ITF_FRONTAL_HTTP, 
				SpringooCST.ITF_FRONTAL_HTTPS
		};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_FRONTAL_HTTP)) {
			return http;
		} else if (name.equals(SpringooCST.ITF_FRONTAL_HTTPS)) {
			return https;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_FRONTAL_HTTP)) {
			http = null;
		} else if (name.equals(SpringooCST.ITF_FRONTAL_HTTPS)) {
			https = null;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

}
