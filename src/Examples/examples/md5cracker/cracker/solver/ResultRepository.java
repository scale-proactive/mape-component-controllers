package examples.md5cracker.cracker.solver;

public interface ResultRepository {

	public static final String ITF_NAME = "result-repository-itf";


	void setResult(String key, MD5Hash hash);

}
