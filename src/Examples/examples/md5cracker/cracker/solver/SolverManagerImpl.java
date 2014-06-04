package examples.md5cracker.cracker.solver;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;


public class SolverManagerImpl implements Solver, SolverAttributes, SolverManager, BindingController {

	public static final String COMP_NAME = "solver-manager-comp";
	
	private TaskRepository taskRepo;
	private ResultRepository resultRepo;
	private WorkerMulticast workers;
	private SolverManager mySelf;
	
	private int maxLength = 0;
	private int alphabetBase = 0;
	private int numberOfWorkers = 1;
   
	@Override
	public void start(String alphabet, int maxLength) {
		workers.setAlphabet(alphabet, maxLength);
		mySelf.solve();
		alphabetBase = alphabet.length();
		this.maxLength = maxLength;
	}

	@Override
	public void solve() {
		Wrapper<MD5Hash> hashWrapper = taskRepo.getTask();
		if( !hashWrapper.isValid() ) return;
		
		long possibilities = 0;
		for (int i = 1; i <= maxLength; i++) {
			possibilities += Math.pow(alphabetBase, i);
		}

		List<Instruction> insts = new ArrayList<Instruction>();
		double slice = possibilities*1.0/numberOfWorkers;
		for (int w = 0; w < numberOfWorkers; w++) {
			long start = (long) Math.ceil(w*slice);
			long end = (long) Math.floor((w+1)*slice);
			insts.add(new Instruction(start, end, hashWrapper.getValue()));
		}
		List<Wrapper<String>> results = workers.solve(insts);
		for( Wrapper<String> w : results ) {
			if (w.isValid()) {
				resultRepo.setResult(w.getValue(), hashWrapper.getValue());
				mySelf.solve();
				return;
			}
		}
		System.out.println("[WorkManager][Warning] key not found for " + hashWrapper.getValue().getHash());
		mySelf.solve();
	}

	// ATTRIBUTES CONTROLLER

	@Override
	public void setNumberOfWorkers(int number) {
		numberOfWorkers = number;
	}

	@Override
	public Integer getNumberOfWorkers() {
		return numberOfWorkers;
	}

	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(WorkerMulticast.ITF_NAME)) {
			workers = (WorkerMulticast) itf;
		} else if (name.equals(TaskRepository.ITF_NAME)) {
			taskRepo = (TaskRepository) itf;
		} else if (name.equals(ResultRepository.ITF_NAME)) {
			resultRepo = (ResultRepository) itf;
		} else if (name.equals(SolverManager.CLIENT_ITF_NAME)) {
			mySelf = (SolverManager) itf;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				WorkerMulticast.ITF_NAME,
				TaskRepository.ITF_NAME,
				ResultRepository.ITF_NAME,
				SolverManager.CLIENT_ITF_NAME
			};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if (name.equals(WorkerMulticast.ITF_NAME)) {
			return workers;
		} else if (name.equals(TaskRepository.ITF_NAME)) {
			return taskRepo;
		} else if (name.equals(ResultRepository.ITF_NAME)) {
			return resultRepo;
		} else if (name.equals(SolverManager.CLIENT_ITF_NAME)) {
			return mySelf;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if (name.equals(WorkerMulticast.ITF_NAME)) {
			workers = null;
		} else if (name.equals(TaskRepository.ITF_NAME)) {
			taskRepo = null;
		} else if (name.equals(ResultRepository.ITF_NAME)) {
			resultRepo = null;
		} else if (name.equals(SolverManager.CLIENT_ITF_NAME)) {
			mySelf = null;
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

}
