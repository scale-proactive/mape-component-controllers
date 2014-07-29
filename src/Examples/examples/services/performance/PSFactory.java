package examples.services.performance;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import examples.services.Service;
import examples.services.performance.components.ManagerImpl;
import examples.services.performance.components.MasterAttributes;
import examples.services.performance.components.MasterImpl;
import examples.services.performance.components.Slave;
import examples.services.performance.components.SlaveImpl;
import examples.services.performance.components.SlaveMulticast;


public class PSFactory {

	public static final String TASK_REPOSITORY_NAME = "TaskRepository";
	public static final String RESULT_REPOSITORY_NAME = "ResultRepository";
	
	static String SC = PAGCMTypeFactory.SINGLETON_CARDINALITY;
	static String MC = PAGCMTypeFactory.MULTICAST_CARDINALITY;
	static boolean MND = PAGCMTypeFactory.MANDATORY;
	static boolean OPT = PAGCMTypeFactory.OPTIONAL;
	static boolean CLI = PAGCMTypeFactory.CLIENT;
	static boolean SRV = PAGCMTypeFactory.SERVER;

	private static Remmos remmos;
	
	private static void checkRemmos(PAGCMTypeFactory tf, PAGenericFactory cf) throws InstantiationException {
		if (remmos == null) {
			remmos = new Remmos(tf, cf);
		}
	}

	// CRACKER
	public static Component createCracker(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SERVICE, Service.class.getName(), SRV, MND, SC),
			};
		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.COMPOSITE),
				new ControllerDescription(PSCST.CRACKER_COMP, Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).policyServing(new ServiceServingPolicy(), 3);
					}
				}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		Remmos.addAnalysis(comp);
		Remmos.addPlannerController(comp);
		Remmos.addExecutorController(comp);
		return comp;
	}

	// CrackerManager
	public static Component createCrackerManager(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(PSCST.CRACKER_MANAGER_ITF, Service.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, ServiceAttributes.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SOLVER_C1, Service.class.getName(), CLI, OPT, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SOLVER_C2, Service.class.getName(), CLI, OPT, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SOLVER_C3, Service.class.getName(), CLI, OPT, SC),
		};
		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.PRIMITIVE),
				new ControllerDescription(PSCST.CRACKER_MANAGER_COMP, Constants.PRIMITIVE),
				new ContentDescription(ManagerImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).policyServing(new ServiceServingPolicy(), 3);
					}
				}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		return comp;
	}


	// SOLVER
	public static Component createSolver(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {

		PAGCMInterfaceType[] fTypes  = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SOLVER, Service.class.getName(), SRV, MND, SC),
			};	

		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes,  Constants.COMPOSITE),
				new ControllerDescription(PSCST.SOLVER_COMP, Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		return comp;
	}

	public static Component createSolverManager(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(PSCST.SOLVER_MANAGER, Service.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, MasterAttributes.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(PSCST.WORKER_MULTICAST, SlaveMulticast.class.getName(), CLI, MND, MC),
			};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription(PSCST.SOLVER_MANAGER_COMP, Constants.PRIMITIVE),
				new ContentDescription(MasterImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}

	public static Component createWorker(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(PSCST.WORKER, Slave.class.getName(), SRV, MND, SC),
			};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription(PSCST.WORKER_COMP, Constants.PRIMITIVE),
				new ContentDescription(SlaveImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}

	public static void bindSolver(Component solver, Component solverManager, Component[] workers) throws Exception {
	
		PAContentController solverCC = Utils.getPAContentController(solver);
		solverCC.addFcSubComponent(solverManager);

		// solver --> solverManager
		PABindingController solverBC = Utils.getPABindingController(solver);
		solverBC.bindFc(PSCST.SOLVER, solverManager.getFcInterface(PSCST.SOLVER_MANAGER));
		
		// solverManager --> workers
		PABindingController solverManagerBC = Utils.getPABindingController(solverManager);
		for (Component worker : workers) {
			solverCC.addFcSubComponent(worker);
			solverManagerBC.bindFc(PSCST.WORKER_MULTICAST,
					worker.getFcInterface(PSCST.WORKER));
		}
	}

	public static void bindCracker(Component cracker, Component crackerManager, Component[] solvers) throws Exception {
		
		PAContentController crackerCC = Utils.getPAContentController(cracker);
		crackerCC.addFcSubComponent(crackerManager);

		Utils.getPABindingController(cracker).bindFc(PSCST.SERVICE, crackerManager.getFcInterface(PSCST.CRACKER_MANAGER_ITF));
		
		PABindingController crackerManagerBC = Utils.getPABindingController(crackerManager);
		
		if (solvers.length >= 1) {
			crackerCC.addFcSubComponent(solvers[0]);
			crackerManagerBC.bindFc(PSCST.SOLVER_C1, solvers[0].getFcInterface(PSCST.SOLVER));
		}
		
		if (solvers.length >= 2) {
			crackerCC.addFcSubComponent(solvers[1]);
			crackerManagerBC.bindFc(PSCST.SOLVER_C2, solvers[1].getFcInterface(PSCST.SOLVER));
		}
		
		if (solvers.length >= 3) {
			crackerCC.addFcSubComponent(solvers[2]);
			crackerManagerBC.bindFc(PSCST.SOLVER_C3, solvers[2].getFcInterface(PSCST.SOLVER));
		}
	}
}
