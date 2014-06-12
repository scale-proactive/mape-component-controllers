package examples.md5cracker.actions;


import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.component.mape.execution.Action;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;

import examples.md5cracker.CrackerFactory;
import examples.md5cracker.cracker.CCST;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.metrics.SolverMetric;


public class AddSolverAction extends Action {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NAME = "add-solver-action";

	private Node[] nodes;
	
	String SC = PAGCMTypeFactory.SINGLETON_CARDINALITY;
	String MC = PAGCMTypeFactory.MULTICAST_CARDINALITY;
	boolean MND = PAGCMTypeFactory.MANDATORY;
	boolean CLI = PAGCMTypeFactory.CLIENT;
	boolean SRV = PAGCMTypeFactory.SERVER;

	
	public AddSolverAction(Node[] nodes) {

		this.nodes = nodes;
	}

	@Override
	public Object execute(Component cracker, PAGCMTypeFactory tf, PAGenericFactory cf) {
		
		Component crackerManager;
		try {
			crackerManager = this.getBindComponent(cracker, CCST.CRACKER_ITF);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return false;
		}

		try {
			PABindingController bc = Utils.getPABindingController(crackerManager);
			if (bc.lookupFc(CCST.SOLVER_C1) == null) {
				return addSolver(1, CCST.SOLVER_C1, cracker, crackerManager, tf, cf);
			} else if (bc.lookupFc(CCST.SOLVER_C2) == null) {
				return addSolver(2, CCST.SOLVER_C2, cracker, crackerManager, tf, cf);
			} else if (bc.lookupFc(CCST.SOLVER_C3) == null) {
				return addSolver(3, CCST.SOLVER_C3, cracker, crackerManager, tf, cf);
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean addSolver(int id, String clientItfName, Component cracker, Component crackerManager,
			PAGCMTypeFactory tf, PAGenericFactory cf) throws NoSuchInterfaceException {
	
		Component solver;
		try {
			solver = this.createNewSolver(id - 1, tf, cf);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		
		PAGCMLifeCycleController lcc = Utils.getPAGCMLifeCycleController(cracker);
		try {
			lcc.stopFc();
		
			Utils.getPAContentController(cracker).addFcSubComponent(solver);
			Utils.getPABindingController(crackerManager).bindFc(clientItfName, solver.getFcInterface(CCST.SOLVER));
			
			this.configureMonitoring(crackerManager, solver);
			this.configureAttributes(solver, id);
			lcc.startFc();

		} catch (IllegalLifeCycleException | IllegalContentException | IllegalBindingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private Component createNewSolver(int solverIndex, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] creating new solver...");

		Component solver = CrackerFactory.createSolver(nodes[solverIndex], tf, cf);
		Component solverManager = CrackerFactory.createSolverManager(nodes[solverIndex], tf, cf);
		Component worker = CrackerFactory.createWorker(nodes[solverIndex], tf, cf);

		CrackerFactory.bindSolver(solver, solverManager, new Component[] { worker });
		
		return solver;
	}
	
	private void configureMonitoring(Component crackerManager, Component solver) throws NoSuchInterfaceException {
		Remmos.enableMonitoring(crackerManager);
		MonitorController solverMonitor = Remmos.getMonitorController(solver);
		solverMonitor.startGCMMonitoring();
		solverMonitor.addMetric(SolverMetric.DEFAULT_NAME, new SolverMetric());
	}
	
	private void configureAttributes(Component solver, int id) throws NoSuchInterfaceException {
		Component solverManager = this.getBindComponent(solver, CCST.SOLVER);
		SolverAttributes solverAttributes = (SolverAttributes) GCM.getAttributeController(solverManager);
		solverAttributes.setId(id);
		solverAttributes.setNumberOfWorkers(1);
	}
}
