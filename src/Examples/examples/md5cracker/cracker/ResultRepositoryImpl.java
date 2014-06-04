package examples.md5cracker.cracker;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import examples.md5cracker.cracker.solver.MD5Hash;
import examples.md5cracker.cracker.solver.ResultRepository;



public class ResultRepositoryImpl implements ResultRepository {

	public static final String COMP_NAME = "result-repository-comp";

	private LinkedList<Long> queue = new LinkedList<Long>();
	private long lastTime = -1L;

	@Override
	public void setResult(String result, MD5Hash hash) {
		//System.out.println("[ResultRepository] Result received: " + hash.getHash() + " --> " + result);
		//printSPM();
	}
	
	private void printSPM() {
		if (lastTime == -1L) {
			lastTime = System.currentTimeMillis();
			return;
		}
		long time = System.currentTimeMillis();
		
		queue.add(time - lastTime);
		lastTime = time;
		while (queue.size() >= 10) {
			queue.remove();
		};

		Iterator<Long> iterator = queue.iterator();
		double total = 0;
		while (iterator.hasNext()) {
			total += iterator.next();
		}
		total = total/queue.size();
		total = 60/(total/1000);
		System.out.println("Result rEPO spm = "  + total);
	}

}
