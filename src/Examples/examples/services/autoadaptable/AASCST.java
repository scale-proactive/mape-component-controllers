package examples.services.autoadaptable;

public interface AASCST {

	public static final String SERVICE = "service-itf";
	public static final String MANAGER = "manager-itf";

	public static final String SOLVER = "solver-itf";
	public static final String SOLVER_C1 = "solver-c1-itf";
	public static final String SOLVER_C2 = "solver-c2-itf";
	public static final String SOLVER_C3 = "solver-c3-itf";
	
	public static final String MASTER = "master-itf";
	public static final String SLAVE = "slave-itf";
	public static final String SLAVE_MULTICAST = "slave-multicast-itf";

	public static final String ALPHA = "abcdefghijklmnopqrstuvwxyz0123456789";
	
	public static final String SERVICE_COMP_NAME = "Service";
	public static final String MANAGER_COMP_NAME = "Manager";
	public static final String SOLVER_COMP_NAME = "Solver";
	public static final String MASTER_COMP_NAME = "Master";
	public static final String SLAVE_COMP_NAME = "Slave";

	public static final String OPTIMAL_POINTS_METRIC = "optimal-points-metric";
	public static final String RESPONSE_TIME_METRIC = "response-time-metric";
	public static final String VARIATION_RULE = "variation-rule";
	public static final String ADD_SLAVE_ACTION = "add-slave-action";


}
