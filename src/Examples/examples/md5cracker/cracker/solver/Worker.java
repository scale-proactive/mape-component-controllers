package examples.md5cracker.cracker.solver;

public interface Worker {

	public static final String ITF_NAME = "worker-itf";


	void setAlphabet(String alphabet, int maxLength);

	Wrapper<String> solve(Instruction inst);

}
