package cl.niclabs.autonomic.examples.balancer.components;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import cl.niclabs.autonomic.examples.balancer.BalancerCST;


public class WorkerImpl implements WorkerItf {

	private MessageDigest md5;

	@Override
	public Wrapper<String> workOn(Task task) {
        
		try {
			if (md5 == null) md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			new WrongWrapper<String>("This shouldnt happen");
		}
		
		for(long i = task.from; i <= task.to; i++) {
			String option = converToString(i);
			do {
				byte[] proposal = md5.digest(option.getBytes());
				if (Arrays.equals(proposal, task.hash) && compare(task.hash, option, md5)) {
					return new ValidWrapper<String>(option);
				}
				option = BalancerCST.ALPHABET.charAt(0) + option;
			} while(option.length() <= task.maxLength);

		}

        return new WrongWrapper<String>("FAIL3", "Not found");
	}

	private String converToString(long decimal) {
	
		String value = decimal == 0 ? "" + BalancerCST.ALPHABET.charAt(0) : "";
		int base = BalancerCST.ALPHABET.length();
		while( decimal != 0 ) {  
			int mod = (int) (decimal % base);  
			value = BalancerCST.ALPHABET.substring(mod, mod + 1) + value;  
			decimal = decimal / base;  
		}

	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5) {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }

}
