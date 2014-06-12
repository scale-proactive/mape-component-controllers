package examples.md5cracker.cracker.solver;

import java.io.Serializable;

public class Instruction implements Serializable {

	private static final long serialVersionUID = 1L;

	int maxLength;
	long from, until;
	byte[] hash;
	
	Instruction(long from, long until, byte[] hash, int maxLength) {
		this.from = from;
		this.until = until;
		this.hash = hash;
		this.maxLength = maxLength;
	}

}
