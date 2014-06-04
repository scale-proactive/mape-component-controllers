package examples.md5cracker.cracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import examples.md5cracker.cracker.solver.MD5Hash;
import examples.md5cracker.cracker.solver.TaskRepository;
import examples.md5cracker.cracker.solver.Wrapper;




public class TaskRepositoryImpl implements TaskRepository {

	public static final String COMP_NAME = "task-repository-comp";
	
	private String alphabet = "0Aa1BbCc2DdEe3FfGg4HhIi5JjKk6LlMm7NnOo8PpQq9RrSsTtUuVvWwXxYyZz";
	private int maxWordLength = 3;
	private MessageDigest md5 = null;

	@Override
	public Wrapper<MD5Hash> getTask() {
		
		if (md5 == null) {
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return new Wrapper<MD5Hash>(true, new MD5Hash(new byte[] {}));
			}
		}

		String word = getRandomWord(maxWordLength);
		return new Wrapper<MD5Hash>(true, new MD5Hash(md5.digest(word.getBytes())));
	}

	private String getRandomWord(int length) {
		String word = "";
		for (int i = 0; i < length; i ++) {
			word += alphabet.charAt((int) (Math.random() * alphabet.length()));
		}
		return word;
	}
	
}
