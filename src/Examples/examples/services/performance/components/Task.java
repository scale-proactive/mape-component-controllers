package examples.services.performance.components;

import java.io.Serializable;

public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	long from, to;
	byte[] hash;
	int maxLength;

	public Task(long from, long to, byte[] hash, int maxLength) {
		this.from = from;
		this.to = to;
		this.hash = hash;
		this.maxLength = maxLength;
	}

}
