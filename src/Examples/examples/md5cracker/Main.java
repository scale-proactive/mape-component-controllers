package examples.md5cracker;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.reconfiguration.Action;
import org.objectweb.proactive.extra.component.mape.reconfiguration.ExecutionController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;

import examples.md5cracker.actions.AddWorkerAction;
import examples.md5cracker.actions.QoSAction;
import examples.md5cracker.cracker.Cracker;

import examples.md5cracker.cracker.solver.SolverAttributes;
import examples.md5cracker.metrics.GlobalSPMMetric;
import examples.md5cracker.metrics.LocalSPMMetric;
import examples.md5cracker.metrics.NumberOfSolvers;
import examples.md5cracker.metrics.NumberOfWorkers;
import examples.md5cracker.metrics.TotalNumberOfWorkers;
import examples.md5cracker.metrics.UpgradabilityStatusMetric;
import examples.md5cracker.rules.QoSRule;



public class Main {

	static String DESCRIPTOR_PATH = "file:///user/mibanez/home/Taller/memoria-tests/src/test2/md5cracker/GCMApp.xml";

	private static boolean MANAGED = true;
	private static int N_OF_SOLVERS = 1;
	private static int N_OF_WORKERS = 1;
	private static int MAX_WORD_LENGTH = 3;
	
	private static int MAX_SOLVERS = 3;
	private static int MAX_WORKERS = 4;

	private static double EXPECTED_SPM = 3000.0;
	private static long actionDelay = 20000;
	
	private static final String alphabet = "0Aa1BbCc2DdEe3FfGg4HhIi5JjKk6LlMm7NnOo8PpQq9RrSsTtUuVvWwXxYyZz";

	PAGCMTypeFactory tf;
	PAGenericFactory cf;
	Active active;
	
	public Main() throws InstantiationException, NoSuchInterfaceException {
		Component boot = Utils.getBootstrapComponent();
		tf = Utils.getPAGCMTypeFactory(boot);
		cf = Utils.getPAGenericFactory(boot);
	}

	public void run() throws Exception {
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
		Node N0 = null;
		
	    Node[] nodes = new Node[MAX_SOLVERS];
	    for (int i = 0; i < MAX_SOLVERS; i++) {
	    	//nodes[i] = vnodes[i].getANode();
	    	nodes[i] = null;
	    }

	    Component cracker = CrackerFactory.createCracker(N0, tf, cf);
	    Component crackerManager = CrackerFactory.createCrackerManager(N0, tf, cf);
	    Component taskRepo = CrackerFactory.createTaskRepo(N0, tf, cf);
	    Component resultRepo = CrackerFactory.createResultRepo(N0, tf, cf);
	    
	    Component[] solvers = new Component[N_OF_SOLVERS];
		Component[] solverManagers = new Component[N_OF_SOLVERS];
		Component[][] workers = new Component[N_OF_SOLVERS][N_OF_WORKERS];
		
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			solvers[i] = CrackerFactory.createSolver(nodes[i], tf, cf);
			solverManagers[i] = CrackerFactory.createSolverManager(nodes[i], tf, cf);

			for (int j = 0; j < N_OF_WORKERS; j++) {
				workers[i][j] = CrackerFactory.createWorker(nodes[i], tf, cf);
			}
			
			CrackerFactory.bindSolver(solvers[i], solverManagers[i], workers[i]);
		}
		
		CrackerFactory.bindCracker(cracker, crackerManager, taskRepo, resultRepo, solvers);
		
		// START
		Utils.getPAGCMLifeCycleController(cracker).startFc();

		// SETUP
		Remmos.enableMonitoring(cracker); // enable cracker, crackerManager and solvers
		for (Component solverManager : solverManagers) {
			((SolverAttributes) GCM.getAttributeController(solverManager)).setNumberOfWorkers(N_OF_WORKERS);
		}
		
		// MAPE SETUP
		MonitorController mon = (MonitorController) cracker.getFcInterface(Constants.MONITOR_CONTROLLER);
		mon.startGCMMonitoring();
		Thread.sleep(2000);

		mon.addMetric(NumberOfSolvers.DEFAULT_NAME, new NumberOfSolvers(N_OF_SOLVERS));
		mon.addMetric(GlobalSPMMetric.DEFAULT_NAME, new GlobalSPMMetric());
		mon.addMetric(TotalNumberOfWorkers.DEFAULT_NAME, new TotalNumberOfWorkers());
		
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			MonitorController solverMon = (MonitorController) solvers[i].getFcInterface(Constants.MONITOR_CONTROLLER);
			solverMon.addMetric(LocalSPMMetric.DEFAULT_NAME, new LocalSPMMetric());
			solverMon.addMetric(UpgradabilityStatusMetric.DEFAULT_NAME, new UpgradabilityStatusMetric(true));
			solverMon.addMetric(NumberOfWorkers.DEFAULT_NAME, new NumberOfWorkers(N_OF_WORKERS));
			
			// load definition of add-worker action. This action will be caled by QoSAction
			Remmos.getExecutionController(solvers[i]).load(Main.class.getResource("actions/AddWorker1.fscript").getPath());
			
		}
		for (int i = 0; i < N_OF_SOLVERS; i++) {
			Action action = new AddWorkerAction(new Object[] {alphabet, MAX_WORD_LENGTH}, null, MAX_WORKERS);
			Remmos.getExecutionController(solvers[i]).addAction(AddWorkerAction.DEFAULT_NAME, action);
		}
		Action action = new QoSAction(new Object[] {alphabet, MAX_WORD_LENGTH}, MAX_WORKERS, MAX_SOLVERS, actionDelay, nodes);
		Remmos.getExecutionController(cracker).addAction(QoSAction.DEFAULT_NAME, action);
	
		// RUN
		printIntro();		
		Cracker brutus = (Cracker) cracker.getFcInterface(Cracker.ITF_NAME);
		brutus.start(alphabet, MAX_WORD_LENGTH);		
		Thread.sleep(2000);
		
		
		/*
		System.out.println("\n\n\n------");
		ExecutionController ec = Remmos.getExecutionController(solvers[0]);
		ec.load(Main.class.getResource("test.fscript").getPath());
		System.out.println("RESULT = " + ec.execute("stop($this/child::SolverManager;"));
	
		Thread.sleep(10000000);
		*/
	
		// MAPE RUN 
		
		long startTime = System.currentTimeMillis();

		Remmos.getAnalysisController(cracker).addRule(QoSRule.DEFAULT_NAME,
				new QoSRule(EXPECTED_SPM), QoSAction.DEFAULT_NAME);
	
		while (true) {
			String headMsg = "" + ((System.currentTimeMillis() - startTime)/60000.0);
			headMsg += "\t" + ((Double) mon.calculateMetric(GlobalSPMMetric.DEFAULT_NAME).getValue()).doubleValue();
			headMsg += "\t" + (Integer) mon.getMetricValue(NumberOfSolvers.DEFAULT_NAME).getValue();
			headMsg += "\t" + (Integer) mon.calculateMetric(TotalNumberOfWorkers.DEFAULT_NAME).getValue();
			System.out.println(headMsg);

			Thread.sleep(5000);
		}
	}

	public static void main(String[] args) throws Exception {
		
    	if(args.length != 0) {
    		MANAGED = Boolean.parseBoolean(args[0]);
    		N_OF_SOLVERS = Integer.parseInt(args[1]);
    		N_OF_WORKERS = Integer.parseInt(args[2]);
    		MAX_WORD_LENGTH = Integer.parseInt(args[3]);
    	}

    	(new Main()).run();
	}


	private static void printIntro() {
		System.out.println("[MD5Cracker]");
		System.out.println("* * * * * * * * * * * MD5Cracker * * * * * * * * * * * * * *");
		System.out.println("MANAGED = " + MANAGED);
		System.out.println("N_OF_SOLVERS = " + N_OF_SOLVERS);
		System.out.println("N_OF_WORKERS = " + N_OF_WORKERS);
		System.out.println("MAX_WORD_LENGTH = " + MAX_WORD_LENGTH);
		System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
	}

}
