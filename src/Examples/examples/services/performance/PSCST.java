package examples.services.performance;

import examples.services.SCST;

public interface PSCST extends SCST {

	public static final String CRACKER_MANAGER_ITF = "cracker-manager";

	public static final String CRACKER_COMP = "Cracker";
	public static final String CRACKER_MANAGER_COMP = "CrackerManager";
	
	public static final String SOLVER = "solver";
	public static final String SOLVER_C1 = "solver-1";
	public static final String SOLVER_C2 = "solver-2";
	public static final String SOLVER_C3 = "solver-3";
	public static final String SOLVER_COMP = "Solver";

	public static final String SOLVER_MANAGER = "solver-manager";
	public static final String SOLVER_MANAGER_COMP = "SolverManager";

	public static final String WORKER = "worker";
	public static final String WORKER_MULTICAST = "worker-multicast";
	public static final String WORKER_COMP = "Worker";

	public static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

}
