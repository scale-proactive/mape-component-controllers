package examples.md5cracker;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import examples.md5cracker.cracker.CCST;
import examples.md5cracker.cracker.Cracker;

public class Client implements Serializable, Runnable {

	private static final long serialVersionUID = 1L;

	private Component crackerComp;
	private int maxLength;
	private String myName;

	private MessageDigest md5;
	private String alphabet = "0Aa1BbCc2DdEe3FfGg4HhIi5JjKk6LlMm7NnOo8PpQq9RrSsTtUuVvWwXxYyZz";


	public Client(Component crackerComponent, int maxLength, String clientName) throws NoSuchAlgorithmException {
		this.crackerComp = crackerComponent;
		this.maxLength = maxLength;
		this.md5 = MessageDigest.getInstance("MD5");
		
		this.myName = clientName;
	}

	@Override
	public void run() {
		Cracker cracker;
		try {
			cracker = (Cracker) crackerComp.getFcInterface(CCST.CRACKER_ITF);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			//long init  = System.currentTimeMillis();
			String word = cracker.crack(md5.digest(getRandomWord().getBytes()), maxLength).getStringValue();
			//double time = 60.0/((System.currentTimeMillis() - init)/1000.0);
			//System.out.println(myName + ": " + "[" + time + "]");
		}
	}

	private String getRandomWord() {
		String word = "";
		for (int i = 0; i < maxLength; i ++) {
			word += alphabet.charAt((int) (Math.random() * alphabet.length()));
		}
		return word;
	}

}
