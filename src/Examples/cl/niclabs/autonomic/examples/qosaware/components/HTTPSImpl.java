package cl.niclabs.autonomic.examples.qosaware.components;

import java.util.ArrayList;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.qosaware.SpringooCST;


//public class BalancerImpl implements BalancerAttr, CrackerItf, BindingController {
public class HTTPSImpl implements HTTPItf, BindingController {

	private double p1 = 0.334;
	private double p2 = 0.667;

	private JonasItf jonas;


	@Override
	public Wrapper<String> processHttpRequest(int val) {

		if (SpringooCST.DEBUG) System.out.println("[HTTPS] Making HTTPS Request");

		long pos = 0;
		for (int i = 1; i <= val; i++) pos += Math.pow(SpringooCST.ALPHABET.length(), i);

		ArrayList<Wrapper<String>> results = new ArrayList<Wrapper<String>>();

		//make Jonas call
		Wrapper<String> response = jonas.processJonasCall(200);

		if (SpringooCST.DEBUG) System.out.println("[HTTPS] HTTPS Request finished");
		//Wrapper<String> ok = null;
		//for (Wrapper<String> r : results) {
		//	if (r.isValid()) {
		//		ok = r;
		//	}
		//}
		try {
			Thread.sleep(2000);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
		
		//return ok != null ? ok : new WrongWrapper<String>("FAIL", "Solution not found...");
		return response;
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

		System.out.println("[WARNING] HTTPS set points \"" + points + "\" fails");
	}

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_APACHE_SERVER_JONAS)) {
			jonas = (JonasItf) itf;
		} else {
			throw new NoSuchInterfaceException("itf not found on HTTPS: " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				SpringooCST.ITF_APACHE_SERVER_JONAS
		};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_APACHE_SERVER_JONAS)) {
			return jonas;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_APACHE_SERVER_JONAS)) {
			jonas = null;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

}
