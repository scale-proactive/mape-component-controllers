package examples.md5cracker.cracker.solver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

public class WorkerImpl implements Worker {

	public static final String COMP_NAME = "worker-comp";
	
	private boolean ready = false;
    private int base;
    private int maxLength;
    private String alphabet;

	@Override
	public void setAlphabet(String alphabet, int maxLength) {
		this.alphabet = alphabet;
		this.maxLength = maxLength;
		base = alphabet.length();
		ready = true;
	}

	@Override
	public Wrapper<String> solve(Instruction inst) {
		long init = inst.from;
		long end = inst.until;
		MD5Hash md5hash = inst.hash;
		if ( !ready ) {
			(new Exception("Workers are not ready. You need to set an alphabet.")).printStackTrace();
			return new Wrapper<String>(false, null);
		}
		// ALGORITHM BASED ON http://code.google.com/p/javamd5cracker/ solution
		
		byte[] hash = md5hash.getHash();
		String word = "";
		boolean found = false;
        try {
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        for(long i = init; i <= end; i++) {
				String option = convertToBase62(i);
	            do {
	            	byte[] proposal = md5.digest(option.getBytes());
					if (Arrays.equals(proposal, hash) && compare(hash, option, md5)) {
						word = option;
						found = true;
					}
					option = 0 + option;
	            } while(option.length() <= maxLength);
	        }
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
        return new Wrapper<String>(found, word);
	}

	private String convertToBase62(long decimal) {
		String value = decimal == 0 ? "0" : "";  
		int mod = 0;  
		while( decimal != 0 ) {  
			mod = (int) (decimal % base);  
			value = alphabet.substring(mod, mod + 1) + value;  
			decimal = decimal / base;  
		}
	    return value;
	}

    private boolean compare(final byte[] hash, final String option, MessageDigest md5)
    		throws NoSuchAlgorithmException {
        return Arrays.equals(md5.digest(option.getBytes()), hash);
    }
  
}
