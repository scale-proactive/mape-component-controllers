package examples.services;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

import examples.services.autoadaptable.AASCST;

public class ServiceClient implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private Component service;
	private int maxLength;
	private String myName;

	private MessageDigest md5;


	public ServiceClient(Component service, int maxLength, String clientName) throws NoSuchAlgorithmException {
		this.service = service;
		this.maxLength = maxLength;
		this.md5 = MessageDigest.getInstance("MD5");
		
		this.myName = clientName;
	}

	@Override
	public void run() {
		Service cracker;
		try {
			cracker = (Service) service.getFcInterface(AASCST.SERVICE);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return;
		}

		double initTime = System.currentTimeMillis();
		long[] times = new long[10];
		int i = 0;
		boolean full = false;

		while (true) {
			String word = getRandomWord();
			long init = System.currentTimeMillis();
			Wrapper<String> ow = cracker.crack(md5.digest(word.getBytes()), maxLength);
			if ( ! ow.isValid() ) {
				System.out.println ("The service is failing.......................");
			}
			long time = System.currentTimeMillis() - init;
			times[i] = time;
			i = (i+1)%10;

			if (!full) {
				if (i == 0) {
					full = true;
				} else {
					int j = i;
					do { times[j++] = time; } while (j < 10);
				}
			}
			
			long t = 0;
			for (int j = 0; j < 10; j++) t += times[j];
			t = t/10;
			
			System.out.printf("%.3f\t%d\n", (System.currentTimeMillis() - initTime)/60000.0, t);
		}
	}

	private String getRandomWord() {
		String word = "";
		int base = AASCST.ALPHA.length();
		for (int i = 0; i < maxLength; i ++) {
			word += AASCST.ALPHA.charAt((int) (Math.random() * base));
		}
		return word;
	}


}
