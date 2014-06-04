package examples.md5cracker.cracker.solver;

import java.io.Serializable;

public class Wrapper<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean valid;
	private T value;
	
	public Wrapper(boolean isValid, T value) {
		valid = isValid;
		this.value = value;
	}
	
	boolean isValid() {
		return valid;
	}
	
	T getValue() {
		return value;
	}
}
