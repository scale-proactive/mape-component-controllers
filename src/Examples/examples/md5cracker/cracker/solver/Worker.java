package examples.md5cracker.cracker.solver;

public interface Worker {

	public static final String ITF_NAME = "worker-itf";

	Wrapper<String> solve(Instruction inst);

}
