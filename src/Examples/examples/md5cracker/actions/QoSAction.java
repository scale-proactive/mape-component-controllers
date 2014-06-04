package examples.md5cracker.actions;


import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extra.component.mape.reconfiguration.Action;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;

import examples.md5cracker.CrackerFactory;
import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.SolverMulticast;
import examples.md5cracker.cracker.solver.ResultRepository;
import examples.md5cracker.cracker.solver.Solver;
import examples.md5cracker.cracker.solver.TaskRepository;
import examples.md5cracker.metrics.LocalSPMMetric;
import examples.md5cracker.metrics.NumberOfSolvers;
import examples.md5cracker.metrics.NumberOfWorkers;
import examples.md5cracker.metrics.UpgradabilityStatusMetric;


public class QoSAction extends Action {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NAME = "add-solver-action";

	private Object[] workerArgs;

	private int maxWorkersNumber;
	private int maxSolversNumber;
	private long lastTime, delay;
	private boolean upgradability;

	private Node[] nodes;
	
	String SC = PAGCMTypeFactory.SINGLETON_CARDINALITY;
	String MC = PAGCMTypeFactory.MULTICAST_CARDINALITY;
	boolean MND = PAGCMTypeFactory.MANDATORY;
	boolean CLI = PAGCMTypeFactory.CLIENT;
	boolean SRV = PAGCMTypeFactory.SERVER;

	public QoSAction(Object[] workerArgs, int maxWorkersNumber, int maxSolversNumber, long delay, Node[] nodes) {
		
		this.workerArgs = workerArgs;
		this.maxWorkersNumber = maxWorkersNumber;
		this.maxSolversNumber = maxSolversNumber;
		this.lastTime = System.currentTimeMillis();
		this.delay = delay;
		
		this.nodes = nodes;
		
		this.upgradability = true; // assume true, it will be checked anyway
	}

	@Override
	public void execute(Component crackerComp, PAGCMTypeFactory tf, PAGenericFactory cf) {
		try {
			if (!upgradability) {
				return; // nothing to do
			}
			
			synchronized (this) {
				if (System.currentTimeMillis() - lastTime < delay) {
					return; // is not the moment yet, try later.
				}
				lastTime = Long.MAX_VALUE; // this ensures that nobody else will met the delay condition.
			}
		
			System.out.println("[EXECUTION_CONTROLLER] Init QoSAction...");

			// check first if some solver can be improved.
			Component crackerManager = getBindComponent(crackerComp, Cracker.ITF_NAME);
			Component[] solvers = getMulticastBindComponenents(crackerManager, SolverMulticast.ITF_NAME);
			MetricValue mv;
			
			for (Component solver : solvers) {
				
				mv = Remmos.getMonitorController(solver).getMetricValue(UpgradabilityStatusMetric.DEFAULT_NAME);
				
				if ((Boolean) mv.getValue()) {
					// If this solver can be improved, improve it, and thats all.
					String solverName = GCM.getNameController(solver).getFcName();
					System.out.println("[EXECUTION_CONTROLLER] Improving Solver " + solverName);
					
					Remmos.getExecutionController(solver).executeAction("add-worker-script");

					lastTime = System.currentTimeMillis();
					return;
				}
			}

			
			MonitorController crackerMonitoring = Remmos.getMonitorController(crackerComp);
			int solversNumber = 1 + (Integer) crackerMonitoring.getMetricValue(NumberOfSolvers.DEFAULT_NAME).getValue();
			
			// check is we are satisfying the max number of solvers rule. (For the case where
			// initial number of solvers is equals to the max number of solvers).
			if (solversNumber > maxSolversNumber) {
				System.out.println("[EXECUTION_CONTROLLER] Nothing can be upgraded. Finish.");
				upgradability = false;
				
				lastTime = System.currentTimeMillis();
				return;
			}
			
			// add new solver
			System.out.println("[EXECUTION_CONTROLLER] Adding new Solver");
			
			// Create new components
			Component solverComp = CrackerFactory.createSolver(nodes[solversNumber - 1], tf, cf);
			Component solverManagerComp = CrackerFactory.createSolverManager(nodes[solversNumber - 1], tf, cf);
			Component workerComp = CrackerFactory.createWorker(nodes[solversNumber - 1], tf, cf);
			CrackerFactory.bindSolver(solverComp, solverManagerComp, new Component[] { workerComp });
			
			// Getting needed references
			Component taskRepo = getBindComponent(crackerManager, TaskRepository.ITF_NAME);
			Component resultRepo = getBindComponent(solvers[0], ResultRepository.ITF_NAME);
			
			// Add solver
			Utils.getPAGCMLifeCycleController(crackerComp).stopFc();
			Utils.getPAContentController(crackerComp).addFcSubComponent(solverComp);
			Utils.getPABindingController(crackerManager).bindFc(SolverMulticast.ITF_NAME, solverComp.getFcInterface(Solver.ITF_NAME));
			PABindingController bc = Utils.getPABindingController(solverComp);
			bc.bindFc(TaskRepository.ITF_NAME, taskRepo.getFcInterface(TaskRepository.ITF_NAME));
			bc.bindFc(ResultRepository.ITF_NAME, resultRepo.getFcInterface(ResultRepository.ITF_NAME));
			
			Remmos.enableMonitoring(crackerManager);
			Remmos.addExecution(solverComp);
	
			// setting up solver
			Utils.getPAGCMLifeCycleController(solverComp).startFc();
		
			MonitorController solverMonitor = Remmos.getMonitorController(solverComp);
			solverMonitor.startGCMMonitoring();
			solverMonitor.addMetric(LocalSPMMetric.DEFAULT_NAME, new LocalSPMMetric());
			solverMonitor.addMetric(UpgradabilityStatusMetric.DEFAULT_NAME, new UpgradabilityStatusMetric(true));
			solverMonitor.addMetric(NumberOfWorkers.DEFAULT_NAME, new NumberOfWorkers(1));
		
			Remmos.getExecutionController(solverComp).addAction(AddWorkerAction.DEFAULT_NAME,
					new AddWorkerAction(workerArgs, null, maxWorkersNumber));
			
			// start all and update number of solver
			Utils.getPAGCMLifeCycleController(crackerComp).startFc();
			crackerMonitoring.setMetricValue(NumberOfSolvers.DEFAULT_NAME, solversNumber);

			((Solver) solverComp.getFcInterface(Solver.ITF_NAME)).start((String) workerArgs[0], (int) workerArgs[1]);
			
			System.out.println("[EXECUTION_CONTROLLER] Adding Solver Finish.");
			
			lastTime = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
