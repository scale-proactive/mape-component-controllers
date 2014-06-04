package examples.md5cracker.actions;


import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.extra.component.mape.reconfiguration.Action;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import examples.md5cracker.cracker.solver.Solver;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.cracker.solver.Worker;
import examples.md5cracker.cracker.solver.WorkerImpl;
import examples.md5cracker.cracker.solver.WorkerMulticast;
import examples.md5cracker.metrics.NumberOfWorkers;
import examples.md5cracker.metrics.UpgradabilityStatusMetric;



public class AddWorkerAction extends Action {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_NAME = "add-worker-action";

	private Object[] workerArgs;
	private Node node;
	private int maxWorkersNumber;
	private boolean upgradability;


	public AddWorkerAction(Object[] workerArgs, Node node, int maxWorkersNumber) {
		this.workerArgs = workerArgs;
		this.node = node;
		this.maxWorkersNumber = maxWorkersNumber;
		this.upgradability = true; // assume true, it will be checked anyway
	}

	@Override
	public void execute(Component solverComp, PAGCMTypeFactory tf, PAGenericFactory cf) {
		try {			
			if (!upgradability) {
				return; // nothing to do
			}
	
			Component solverManager = this.getBindComponent(solverComp, Solver.ITF_NAME);
			SolverAttributes attributes = (SolverAttributes) solverManager.getFcInterface(SolverAttributes.ITF_NAME);
			int workersNumber = 1 + attributes.getNumberOfWorkers();
	
			if (workersNumber > maxWorkersNumber) {
				System.out.println("[EXECUTION_CONTROLLER] Imposible to add new Worker, the maximum " 
						+ " number of workers had been reached. Finish.");
				upgradability = false;
				return;
			}

			// Add new worker
			Component workerComp = createWorker(node, tf, cf);
			Utils.getPAGCMLifeCycleController(solverComp).stopFc();
			Utils.getPAContentController(solverComp).addFcSubComponent(workerComp);
			Utils.getPABindingController(solverManager).bindFc(WorkerMulticast.ITF_NAME, workerComp.getFcInterface(Worker.ITF_NAME));
			
			// Set up new worker
			Utils.getPAGCMLifeCycleController(workerComp).startFc();
			Worker worker = (Worker) workerComp.getFcInterface(Worker.ITF_NAME);
			worker.setAlphabet((String) workerArgs[0], (Integer) workerArgs[1]);

			// Set up solver
			Utils.getPAGCMLifeCycleController(solverComp).startFc();
			attributes.setNumberOfWorkers(workersNumber);
	
			upgradability = workersNumber < maxWorkersNumber;
			MonitorController solverMonitor = Remmos.getMonitorController(solverComp);
			solverMonitor.setMetricValue(UpgradabilityStatusMetric.DEFAULT_NAME, upgradability);
			solverMonitor.setMetricValue(NumberOfWorkers.DEFAULT_NAME, workersNumber);

			System.out.println("[EXECUTION_CONTROLLER] Adding Worker Finished.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Component createWorker(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(Worker.ITF_NAME, Worker.class.getName(),
						PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY)
			};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription("Worker", Constants.PRIMITIVE),
				new ContentDescription(WorkerImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}
}
