package examples.md5cracker.cracker.solver;

import java.io.Serializable;

public class Instruction implements Serializable {

	long from, until;
	MD5Hash hash;
	
	Instruction(long from, long until, MD5Hash hash) {
		this.from = from;
		this.until = until;
		this.hash = hash;
	}
	
}
