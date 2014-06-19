package examples.services.autoadaptable.components;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import examples.services.autoadaptable.AASCST;

public class SlaveImpl implements Slave {

	private MessageDigest md5;
	
	@Override
	public Wrapper<String> workOn(Task task) {
        
		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				new WrongWrapper<String>("This shouldnt happen");
			}
		}
		
		for(long i = task.from; i <= task.to; i++) {
			
			String option = converToString(i);
			do {
				byte[] proposal = md5.digest(option.getBytes());
				if (Arrays.equals(proposal, task.hash) && compare(task.hash, option, md5)) {
					return new ValidWrapper<String>(option);
				}
				option = AASCST.ALPHA.charAt(0) + option;
			} while(option.length() <= task.maxLength);

		}

        return new WrongWrapper<String>("Not found");
	}

	private String converToString(long decimal) {
	
		String value = decimal == 0 ? "" + AASCST.ALPHA.charAt(0) : "";
		int base = AASCST.ALPHA.length();
		while( decimal != 0 ) {  
			int mod = (int) (decimal % base);  
			value = AASCST.ALPHA.substring(mod, mod + 1) + value;  
			decimal = decimal / base;  
		}

	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5) {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }
   
}
