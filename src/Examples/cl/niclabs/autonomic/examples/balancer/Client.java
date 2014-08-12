package cl.niclabs.autonomic.examples.balancer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;

import javax.naming.NamingException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

import cl.niclabs.autonomic.examples.balancer.components.CrackerItf;

public class Client {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("c", true, "component url");
		options.addOption("l", false, "print remi objects");
		options.addOption("h", true, "host");
		options.addOption("p", true, "port");
		options.addOption("w", true, "max word length");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);

		int MAX_LENGTH = 4;
		if(cmd.hasOption("w")) {
			MAX_LENGTH = Integer.parseInt(cmd.getOptionValue("w"));
		}
		
		int PORT = 1099;
		if(cmd.hasOption("p")) {
			PORT = Integer.parseInt(cmd.getOptionValue("p"));
		}

		String HOST = "localhost";
		if (cmd.hasOption("h")) {
			HOST = cmd.getOptionValue("h");
		}

		if (cmd.hasOption("c")) {
			String url = cmd.getOptionValue("c");
			try {
				Component c = Fractive.lookup(url);
				CrackerItf cracker = (CrackerItf) c.getFcInterface("cracker");

		    	MessageDigest md5 = MessageDigest.getInstance("MD5");
		    	while (true) {
			    	String word = getRandomWord(MAX_LENGTH);
			    	long init = System.currentTimeMillis();
					Wrapper<String> ow = cracker.crack(md5.digest(word.getBytes()), MAX_LENGTH);
					if (ow.isValid()) {
						double time = (System.currentTimeMillis() - init) * 1.0 / 1000;
						//System.out.println("Time: " + time + " [s] (" + word + " -> " + md5.digest(word.getBytes()) + " -> " + ow.getValue() + ")");
						System.out.println("Time: " + time + " [s]");
					} else {
						System.out.println("[FAIL] " + ow.getMessage());
					}
					
					Thread.sleep(100);
		    	}
			} catch (NamingException ne) {
				System.err.println("url not found: " + url);
			}
		}

		if (cmd.hasOption("l")) {
			Registry registry = LocateRegistry.getRegistry(HOST, PORT);
			String[] boundNames = registry.list();
			for (String name : boundNames) { System.out.println("\t" + name); }
		}

	}

	private static String getRandomWord(int maxLength) {
		String word = "";
		int base = BalancerCST.ALPHABET.length();
		for (int i = 0; i < maxLength; i ++) {
			word += BalancerCST.ALPHABET.charAt((int) (Math.random() * base));
		}
		return word;
	}

}
