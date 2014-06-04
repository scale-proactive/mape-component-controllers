package examples.md5cracker.cracker.solver;

import java.io.Serializable;

public class MD5Hash implements Serializable {

	private static final long serialVersionUID = 1L;

	byte[] hash;
	
	public MD5Hash(byte[] h) {
		hash = h;
	}
	
	public byte[] getHash() {
		return hash;
	}
}
