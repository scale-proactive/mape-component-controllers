package examples.md5cracker.actions;


import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.component.mape.execution.Action;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;

import examples.md5cracker.CrackerFactory;
import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.CrackerAttributes;
import examples.md5cracker.cracker.SolverMulticast;
import examples.md5cracker.cracker.solver.ResultRepository;
import examples.md5cracker.cracker.solver.Solver;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.cracker.solver.TaskRepository;
import examples.md5cracker.metrics.LocalSPMMetric;


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
	public Object execute(Component crackerComp, PAGCMTypeFactory tf, PAGenericFactory cf) {
		try {

			System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] getting data...");
			Component crackerManager = this.getBindComponent(crackerComp, Cracker.ITF_NAME);
			Component taskRepo = this.getSubComponent(crackerComp, CrackerFactory.TASK_REPOSITORY_NAME)[0];
			Component resultRepo = this.getSubComponent(crackerComp, CrackerFactory.RESULT_REPOSITORY_NAME)[0];
			if (taskRepo == null || resultRepo == null) {
				throw new Exception("Adding solver action fails: could not find some subComponents");
			}

			CrackerAttributes crackerAttributes = (CrackerAttributes) GCM.getAttributeController(crackerManager);
			int currentNumOfSolvers = ((Double) crackerAttributes.getNumberOfSolvers()).intValue();

			// Create new components
			System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] creating new solver...");
			Component solverComp = CrackerFactory.createSolver(nodes[currentNumOfSolvers], tf, cf);
			Component solverManagerComp = CrackerFactory.createSolverManager(nodes[currentNumOfSolvers], tf, cf);
			Component workerComp = CrackerFactory.createWorker(nodes[currentNumOfSolvers], tf, cf);
			CrackerFactory.bindSolver(solverComp, solverManagerComp, new Component[] { workerComp });
			
			// Add solver
			System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] stopping...");
			Utils.getPAGCMLifeCycleController(crackerComp).stopFc();
			
			System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] adding...");
			Utils.getPAContentController(crackerComp).addFcSubComponent(solverComp);
			Utils.getPABindingController(crackerManager).bindFc(SolverMulticast.ITF_NAME, solverComp.getFcInterface(Solver.ITF_NAME));
			PABindingController bc = Utils.getPABindingController(solverComp);
			bc.bindFc(TaskRepository.ITF_NAME, taskRepo.getFcInterface(TaskRepository.ITF_NAME));
			bc.bindFc(ResultRepository.ITF_NAME, resultRepo.getFcInterface(ResultRepository.ITF_NAME));
			
			System.out.println("[EXECUTOR_CONTROLLER][ADD_SOLVER] starting...");
			Remmos.enableMonitoring(crackerManager); // [!] ----
			Utils.getPAGCMLifeCycleController(workerComp).startFc();
			Utils.getPAGCMLifeCycleController(solverManagerComp).startFc();
			Utils.getPAGCMLifeCycleController(solverComp).startFc();

			MonitorController solverMonitor = Remmos.getMonitorController(solverComp);
			solverMonitor.startGCMMonitoring();
			solverMonitor.addMetric(LocalSPMMetric.DEFAULT_NAME, new LocalSPMMetric());
			
			Utils.getPAGCMLifeCycleController(crackerComp).startFc();

			// CONFIGURE

			crackerAttributes.setNumberOfSolvers(currentNumOfSolvers + 1);

			SolverAttributes solverAttributes = (SolverAttributes) GCM.getAttributeController(solverManagerComp);
			solverAttributes.setId(currentNumOfSolvers + 1);
			solverAttributes.setNumberOfWorkers(1);
		
			((Solver) solverComp.getFcInterface(Solver.ITF_NAME)).start();
			
			System.out.println("[EXECUTION_CONTROLLER] Add solver action success.");
			return true;
	
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
