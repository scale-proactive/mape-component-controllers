package examples.md5cracker;

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
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.CrackerManagerAttributes;
import examples.md5cracker.cracker.CrackerManagerImpl;
import examples.md5cracker.cracker.ResultRepositoryImpl;
import examples.md5cracker.cracker.SolverMulticast;
import examples.md5cracker.cracker.TaskRepositoryImpl;
import examples.md5cracker.cracker.solver.ResultRepository;
import examples.md5cracker.cracker.solver.Solver;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.cracker.solver.SolverManager;
import examples.md5cracker.cracker.solver.SolverManagerImpl;
import examples.md5cracker.cracker.solver.TaskRepository;
import examples.md5cracker.cracker.solver.Worker;
import examples.md5cracker.cracker.solver.WorkerImpl;
import examples.md5cracker.cracker.solver.WorkerMulticast;


public class CrackerFactory {

	static String SC = PAGCMTypeFactory.SINGLETON_CARDINALITY;
	static String MC = PAGCMTypeFactory.MULTICAST_CARDINALITY;
	static boolean MND = PAGCMTypeFactory.MANDATORY;
	static boolean CLI = PAGCMTypeFactory.CLIENT;
	static boolean SRV = PAGCMTypeFactory.SERVER;

	private static Remmos remmos;
	
	private static void checkRemmos(PAGCMTypeFactory tf, PAGenericFactory cf) throws InstantiationException {
		if (remmos == null) {
			remmos = new Remmos(tf, cf);
		}
	}

	public static Component createCracker(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(Cracker.ITF_NAME, Cracker.class.getName(), SRV, MND, SC),
			};
		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.COMPOSITE),
				new ControllerDescription("Cracker", Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		Remmos.addAnalysis(comp);
		Remmos.addExecution(comp);
		return comp;
	}

	public static Component createCrackerManager(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType(Cracker.ITF_NAME, Cracker.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, CrackerManagerAttributes.class.getName(), SRV, MND, SC),
			(PAGCMInterfaceType) tf.createGCMItfType(SolverMulticast.ITF_NAME, SolverMulticast.class.getName(), CLI, MND, MC),
		};
		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes, Constants.PRIMITIVE),
				new ControllerDescription("CrackerManager", Constants.PRIMITIVE),
				new ContentDescription(CrackerManagerImpl.class.getName(), null, new ComponentRunActive() {
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

	public static Component createTaskRepo(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(TaskRepository.ITF_NAME, TaskRepository.class.getName(), SRV, MND, SC),
			};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription("TaskRepository", Constants.PRIMITIVE),
				new ContentDescription(TaskRepositoryImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}
	
	public static Component createResultRepo(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(ResultRepository.ITF_NAME, ResultRepository.class.getName(), SRV, MND, SC),
		};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription("ResultRepository", Constants.PRIMITIVE),
				new ContentDescription(ResultRepositoryImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}

	public static Component createSolver(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes  = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(Solver.ITF_NAME, Solver.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(TaskRepository.ITF_NAME, TaskRepository.class.getName(), CLI, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(ResultRepository.ITF_NAME, ResultRepository.class.getName(), CLI, MND, SC),
			};	

		checkRemmos(tf, cf);
		Component comp = remmos.newFcInstance(
				remmos.createFcType(fTypes,  Constants.COMPOSITE),
				new ControllerDescription("Solver", Constants.COMPOSITE),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);

		Utils.getPAMembraneController(comp).startMembrane();
		Remmos.addMonitoring(comp);
		Remmos.addExecution(comp);
		return comp;
	}

	public static Component createSolverManager(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(Solver.ITF_NAME, Solver.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(Constants.ATTRIBUTE_CONTROLLER, SolverAttributes.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(SolverManager.SERVER_ITF_NAME, SolverManager.class.getName(), SRV, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(SolverManager.CLIENT_ITF_NAME, SolverManager.class.getName(), CLI, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(TaskRepository.ITF_NAME, TaskRepository.class.getName(), CLI, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(ResultRepository.ITF_NAME, ResultRepository.class.getName(), CLI, MND, SC),
				(PAGCMInterfaceType) tf.createGCMItfType(WorkerMulticast.ITF_NAME, WorkerMulticast.class.getName(), CLI, MND, MC),
			};

		return cf.newFcInstance(
				tf.createFcType(fTypes),
				new ControllerDescription("SolverManager", Constants.PRIMITIVE),
				new ContentDescription(SolverManagerImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				node);
	}

	public static Component createWorker(Node node, PAGCMTypeFactory tf, PAGenericFactory cf) throws Exception {
		PAGCMInterfaceType[] fTypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType(Worker.ITF_NAME, Worker.class.getName(), SRV, MND, SC),
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

	public static void bindSolver(Component solver, Component solverManager, Component[] workers) throws Exception {
		PAContentController solverCC = Utils.getPAContentController(solver);
		solverCC.addFcSubComponent(solverManager);

		PABindingController solverBC = Utils.getPABindingController(solver);
		solverBC.bindFc(Solver.ITF_NAME, solverManager.getFcInterface(Solver.ITF_NAME));
		
		PABindingController solverManagerBC = Utils.getPABindingController(solverManager);
		solverManagerBC.bindFc(SolverManager.CLIENT_ITF_NAME, solverManager.getFcInterface(SolverManager.SERVER_ITF_NAME));
		solverManagerBC.bindFc(TaskRepository.ITF_NAME, solver.getFcInterface(TaskRepository.ITF_NAME));
		solverManagerBC.bindFc(ResultRepository.ITF_NAME, solver.getFcInterface(ResultRepository.ITF_NAME));
	
		for (Component worker : workers) {
			solverCC.addFcSubComponent(worker);
			solverManagerBC.bindFc(WorkerMulticast.ITF_NAME, worker.getFcInterface(Worker.ITF_NAME));
		}
	}

	public static void bindCracker(Component cracker, Component crackerManager, Component taskRepo, Component resultRepo,
			Component[] solvers) throws Exception {
		
		PAContentController crackerCC = Utils.getPAContentController(cracker);
		crackerCC.addFcSubComponent(crackerManager);
		crackerCC.addFcSubComponent(taskRepo);
		crackerCC.addFcSubComponent(resultRepo);

		Utils.getPABindingController(cracker).bindFc(Cracker.ITF_NAME, crackerManager.getFcInterface(Cracker.ITF_NAME));
		
		PABindingController crackerManagerBC = Utils.getPABindingController(crackerManager);
		for (Component solver : solvers) {
			crackerCC.addFcSubComponent(solver);
			crackerManagerBC.bindFc(SolverMulticast.ITF_NAME, solver.getFcInterface(Solver.ITF_NAME));
			
			PABindingController solverBC = Utils.getPABindingController(solver);
			solverBC.bindFc(TaskRepository.ITF_NAME, taskRepo.getFcInterface(TaskRepository.ITF_NAME));
			solverBC.bindFc(ResultRepository.ITF_NAME, resultRepo.getFcInterface(ResultRepository.ITF_NAME));
		}
	}
	
}
