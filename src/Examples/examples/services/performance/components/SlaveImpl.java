package examples.services.performance.components;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;

import examples.services.performance.PSCST;


public class SlaveImpl implements Slave {

	public static final String COMP_NAME = "worker-comp";

	@Override
	public Wrapper<String> workOn(Task task) {

		// ALGORITHM BASED ON http://code.google.com/p/javamd5cracker/ solution
        try {
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        for(long i = task.from; i <= task.to; i++) {
				String option = convertToBase(i);
	            do {
	            	byte[] proposal = md5.digest(option.getBytes());
					if (Arrays.equals(proposal, task.hash) && compare(task.hash, option, md5)) {
						return new ValidWrapper<String>(option);
					}
					option = 0 + option;
	            } while(option.length() <= task.maxLength);
	        }
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
        return new WrongWrapper<String>("not found");
	}

	private String convertToBase(long decimal) {
		String value = decimal == 0 ? "0" : "";  
		int mod = 0;  
		while( decimal != 0 ) {  
			mod = (int) (decimal % PSCST.ALPHA.length());  
			value = PSCST.ALPHA.substring(mod, mod + 1) + value;  
			decimal = decimal / PSCST.ALPHA.length();  
		}
	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5)
    		throws NoSuchAlgorithmException {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }
  
}
