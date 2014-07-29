package cl.niclabs.autonomic.examples.balancer.components;

import java.io.Serializable;

public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	byte[] hash;
	int maxLength;
	long from, to;

	public Task(byte[] hash, int maxLength, long from, long to) {
		this.hash = hash;
		this.maxLength = maxLength;
		this.from = from;
		this.to = to;
	}

}
