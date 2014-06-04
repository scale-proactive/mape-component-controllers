package examples.md5cracker.cracker;


public interface Cracker {

	public static final String ITF_NAME = "cracker-itf";
	
	void start(String alphabet, int maxLength);

}
