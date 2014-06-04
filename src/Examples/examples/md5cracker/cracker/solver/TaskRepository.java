package examples.md5cracker.cracker.solver;


public interface TaskRepository {

	public static final String ITF_NAME = "task-repository-itf";


	Wrapper<MD5Hash> getTask();
}
