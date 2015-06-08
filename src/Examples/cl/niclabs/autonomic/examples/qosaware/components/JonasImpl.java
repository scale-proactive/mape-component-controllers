package cl.niclabs.autonomic.examples.qosaware.components;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.qosaware.SpringooCST;


public class JonasImpl implements JonasItf, BindingController {

	private MessageDigest md5;
	private JonasRetItf retValue;

	@Override
	public Wrapper<String> processJonasCall(int val) {
//	public Wrapper<String> workOn(Task task) {
        
        if (SpringooCST.DEBUG) System.out.println("[JonasImpl] Making JONAS call");
		/*try {
			if (md5 == null) md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			new WrongWrapper<String>("This shouldnt happen");
		}*/
		
		/*for(long i = task.from; i <= task.to; i++) {
			String option = converToString(i);
			do {
				byte[] proposal = md5.digest(option.getBytes());
				if (Arrays.equals(proposal, task.hash) && compare(task.hash, option, md5)) {
					return new ValidWrapper<String>(option);
				}
				option = SpringooCST.ALPHABET.charAt(0) + option;
			} while(option.length() <= task.maxLength);

		}*/
		if (SpringooCST.DEBUG) System.out.println("[JonasImpl] Sending value to Frontal");

		Wrapper<String> returnValue = retValue.receiveResponse(val*2);


		try {
			Thread.sleep(1500);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	
        return new ValidWrapper<String>("[JonasImpl] Jonas Done " + val + ", " + (val*2), "JonasDone!");
	}

	private String converToString(long decimal) {
	
		String value = decimal == 0 ? "" + SpringooCST.ALPHABET.charAt(0) : "";
		int base = SpringooCST.ALPHABET.length();
		while( decimal != 0 ) {  
			int mod = (int) (decimal % base);  
			value = SpringooCST.ALPHABET.substring(mod, mod + 1) + value;  
			decimal = decimal / base;  
		}

	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5) {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }


	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_JONAS_SERVER_RETURN)) {
			retValue = (JonasRetItf) itf;
		} else {
			throw new NoSuchInterfaceException("itf not found on JonasImpl: " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				SpringooCST.ITF_JONAS_SERVER_RETURN
		};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_JONAS_SERVER_RETURN)) {
			return retValue;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(SpringooCST.ITF_JONAS_SERVER_RETURN)) {
			retValue = null;
		} else {
			throw new NoSuchInterfaceException("not found itf: " + name);
		}
	}


}
