package examples.md5cracker;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.component.mape.analysis.AnalyzerController;
import org.objectweb.proactive.extra.component.mape.execution.Action;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;

import examples.md5cracker.actions.AddSolverAction;
import examples.md5cracker.actions.RemoveSolverAction;
import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.CrackerAttributes;
import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.metrics.GlobalSPMMetric;
import examples.md5cracker.metrics.LocalSPMMetric;
import examples.md5cracker.plans.QoSPlan;
import examples.md5cracker.rules.MaxPerformanceRule;
import examples.md5cracker.rules.MinPerformanceRule;


public class Main {

	static String DESCRIPTOR_PATH = "file:///user/mibanez/home/Taller/memoria-tests/src/test2/md5cracker/GCMApp.xml";

	private static boolean MANAGED = true;
	private static int N_OF_SOLVERS = 1;
	private static int MAX_SOLVERS = 3;

	private static int N_OF_WORKERS = 1;
	private static int MAX_WORKERS = 3;

	private static long DELAY = 50000;
	private static long PERFORMANCE = 100000;
	PAGCMTypeFactory tf;
	PAGenericFactory cf;
	Active active;

	Node N0;
	Node[] nodes;

	public Main() throws InstantiationException, NoSuchInterfaceException {
		Component boot = Utils.getBootstrapComponent();
		tf = Utils.getPAGCMTypeFactory(boot);
		cf = Utils.getPAGenericFactory(boot);

		/*
		File appDescriptor = new File((new URL(DESCRIPTOR_PATH)).toURI().getPath());
		
		GCMApplication gcmad;
		gcmad = PAGCMDeployment.loadApplicationDescriptor(appDescriptor);
		gcmad.startDeployment();
		gcmad.waitReady();
		
		GCMVirtualNode VN0 = gcmad.getVirtualNode("VN0");
		GCMVirtualNode[] vnodes = new GCMVirtualNode[MAX_SOLVERS];
		for (int i = 0; i < MAX_SOLVERS; i++) {
			vnodes[i] = gcmad.getVirtualNode("VN" + (i+1));
		}
		VN0.waitReady();
	    for (int i = 0; i < MAX_SOLVERS; i++) {
			vnodes[i].waitReady();
	    }  
	    */
		//Node N0 = VN0.getANode();
		N0 = null;
		
		nodes = new Node[MAX_SOLVERS];
	    for (int i = 0; i < MAX_SOLVERS; i++) {
	    	//nodes[i] = vnodes[i].getANode();
	    	nodes[i] = null;
	    }
	}

	public void run() throws Exception {

		// COMPONENTS CREATION
	    Component cracker = CrackerFactory.createCracker(N0, tf, cf);
	    Component crackerManager = CrackerFactory.createCrackerManager(N0, tf, cf);
	    Component taskRepo = CrackerFactory.createTaskRepo(N0, tf, cf);
	    Component resultRepo = CrackerFactory.createResultRepo(N0, tf, cf);
	    
	    Component[] solvers = createSolvers(nodes);
		Component[] solverManagers = createSolverManagers(nodes);
		Component[][] workers = createWorkers(nodes);
		
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			CrackerFactory.bindSolver(solvers[i], solverManagers[i], workers[i]);
		}
		CrackerFactory.bindCracker(cracker, crackerManager, taskRepo, resultRepo, solvers);

		Utils.getPAGCMLifeCycleController(taskRepo).startFc();
		Utils.getPAGCMLifeCycleController(resultRepo).startFc();
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			Utils.getPAGCMLifeCycleController(solvers[i]).startFc();
			Utils.getPAGCMLifeCycleController(solverManagers[i]).startFc();
			for (int j = 0; j < N_OF_WORKERS; j++) {
				Utils.getPAGCMLifeCycleController(workers[i][j]).startFc();
			}
		}
		Utils.getPAGCMLifeCycleController(crackerManager).startFc();
		Utils.getPAGCMLifeCycleController(cracker).startFc();

		
		// CONFIGURE ATTRIBUTES
		((CrackerAttributes) GCM.getAttributeController(crackerManager)).setNumberOfSolvers(N_OF_SOLVERS);
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			SolverAttributes solverAttributes = (SolverAttributes) GCM.getAttributeController(solverManagers[i]);
			solverAttributes.setNumberOfWorkers(N_OF_WORKERS);
			solverAttributes.setId(i + 1); // [!] ---- represent the number of the virtual node, "VNi", for value i.
		}
		
		// CONFIGURE CONTORLLERS
		Remmos.enableMonitoring(cracker); // [!] too much important, maybe merge with MonitoController.startMontioring?
		MonitorController crackerMonitor = (MonitorController) cracker.getFcInterface(Constants.MONITOR_CONTROLLER);
		crackerMonitor.startGCMMonitoring();
		Thread.sleep(2000);

		// Metrics
		crackerMonitor.addMetric(GlobalSPMMetric.DEFAULT_NAME, new GlobalSPMMetric());
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			Remmos.getMonitorController(solvers[i]).addMetric(LocalSPMMetric.DEFAULT_NAME, new LocalSPMMetric());
		}

		// Rules
		Remmos.getAnalyzerController(cracker).addRule(MinPerformanceRule.DEFAULT_NAME, new MinPerformanceRule(PERFORMANCE));
	
		// Plans
		Remmos.getPlannerController(cracker).setPlan(new QoSPlan(MAX_WORKERS, MAX_SOLVERS, DELAY));

		// Actions
		ExecutorController crackerExecutor = Remmos.getExecutorController(cracker);
		crackerExecutor.load(Main.class.getResource("actions/QoS.fscript").getPath());
		crackerExecutor.addAction(AddSolverAction.DEFAULT_NAME, new AddSolverAction(nodes));
		crackerExecutor.addAction(RemoveSolverAction.DEFAULT_NAME, new RemoveSolverAction());
	
		// RUN
		printIntro();		
		Cracker brutus = (Cracker) cracker.getFcInterface(Cracker.ITF_NAME);
		brutus.start();		
		Thread.sleep(2000);
		
		// MAPE RUN 
		
		long startTime = System.currentTimeMillis();

		int c = 0;
		while (true) {
			
			c++;
			if (c == 120) {
				AnalyzerController analyzer = Remmos.getAnalyzerController(cracker);
				analyzer.removeRule(MinPerformanceRule.DEFAULT_NAME);
				analyzer.addRule(MaxPerformanceRule.DEFAULT_NAME, new MaxPerformanceRule(0));
			}
			
			String headMsg = "" + ((System.currentTimeMillis() - startTime)/60000.0);
			headMsg += "\t" + ((Double) crackerMonitor.calculateMetric(GlobalSPMMetric.DEFAULT_NAME).getValue()).doubleValue();
			System.out.println(headMsg);
			Thread.sleep(5000);
		}
	}


	private Component[] createSolvers(Node[] nodes) throws Exception {
		Component[] result = new Component[N_OF_SOLVERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			result[i] = CrackerFactory.createSolver(nodes[i], tf, cf);
		}
		return result;
	}

	private Component[] createSolverManagers(Node[] nodes) throws Exception {
		Component[] result = new Component[N_OF_SOLVERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			result[i] = CrackerFactory.createSolverManager(nodes[i], tf, cf);
		}
		return result;
	}

	private Component[][] createWorkers(Node[] nodes) throws Exception {
		Component[][] result = new Component[N_OF_SOLVERS][N_OF_WORKERS];
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			for (int j = 0; j < N_OF_WORKERS; j++) {
				result[i][j] = CrackerFactory.createWorker(nodes[i], tf, cf);
			}
		}
		return result;
	}

	private void printIntro() {
		System.out.println("[MD5Cracker]");
		System.out.println("* * * * * * * * * * * MD5Cracker * * * * * * * * * * * * * *");
		System.out.println("MANAGED = " + MANAGED);
		System.out.println("N_OF_SOLVERS = " + N_OF_SOLVERS);
		System.out.println("N_OF_WORKERS = " + N_OF_WORKERS);
		System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
	}

	public static void main(String[] args) throws Exception {
		
    	if(args.length != 0) {
    		MANAGED = Boolean.parseBoolean(args[0]);
    		N_OF_SOLVERS = Integer.parseInt(args[1]);
    		N_OF_WORKERS = Integer.parseInt(args[2]);
    	}

    	(new Main()).run();
	}
}
