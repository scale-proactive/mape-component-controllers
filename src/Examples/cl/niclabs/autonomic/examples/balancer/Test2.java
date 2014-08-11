package cl.niclabs.autonomic.examples.balancer;

import java.net.URL;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import cl.niclabs.autonomic.examples.balancer.actions.AddWorkerAction;
import cl.niclabs.autonomic.examples.balancer.components.BalancerAttr;
import cl.niclabs.autonomic.examples.balancer.components.BalancerImpl;
import cl.niclabs.autonomic.examples.balancer.components.CrackerItf;
import cl.niclabs.autonomic.examples.balancer.components.DispatcherAttr;
import cl.niclabs.autonomic.examples.balancer.components.DispatcherImpl;
import cl.niclabs.autonomic.examples.balancer.components.SolverItf;
import cl.niclabs.autonomic.examples.balancer.components.WorkerMulticastItf;
import cl.niclabs.autonomic.examples.balancer.metrics.PointsMetric;
import cl.niclabs.autonomic.examples.balancer.plans.UpdatePointsPlan;
import cl.niclabs.autonomic.examples.balancer.rules.AlwaysAlarmRule;

public class Test2 {

	public static void main(String[] args) throws Exception {
		
		// Factory components
		Component boot = Utils.getBootstrapComponent();
		PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
		PAGenericFactory cf = Utils.getPAGenericFactory(boot);
		
		// Remmos
		Remmos remmos = new Remmos(tf, cf);
		
		// ---> createGCMItfType(STRING.NAME, STRING.SIGNATURE, BOOLEAN.IS_CLIENT, BOOLEAN.IS_OPT,
		//                       STRING.{"singleton", "multicast"})
		
		// Cracker ------------------------------------------------------
		PAGCMInterfaceType[] crackerItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("cracker", CrackerItf.class.getName(), false, false, "singleton")};
		Component crackerComp = remmos.newFcInstance(remmos.createFcType(crackerItftypes, "composite"),
				new ControllerDescription("Cracker", "composite"), null, null);

		Utils.getPAMembraneController(crackerComp).startMembrane();
		Remmos.addMonitoring(crackerComp);
		Remmos.addAnalysis(crackerComp);
		Remmos.addPlannerController(crackerComp);
		Remmos.addExecutorController(crackerComp);
		
		// Balancer -----------------------------------------------------
		PAGCMInterfaceType[] balancerItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("cracker", CrackerItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-1", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-2", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-3", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", BalancerAttr.class.getName(), false, false, "singleton")};
		Component balancerComp = remmos.newFcInstance(remmos.createFcType(balancerItftypes, "primitive"),
				new ControllerDescription("Balancer", "primitive"), new ContentDescription(BalancerImpl.class.getName()), null);
	
		Utils.getPAMembraneController(balancerComp).startMembrane();
		Remmos.addMonitoring(balancerComp);

		// Solver 1 -----------------------------------------------------
		PAGCMInterfaceType[] solver1Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver1Comp = remmos.newFcInstance(remmos.createFcType(solver1Itftypes, "composite"),
				new ControllerDescription("Solver1", "composite"), null, null);

		Utils.getPAMembraneController(solver1Comp).startMembrane();
		Remmos.addMonitoring(solver1Comp);
		Remmos.addExecutorController(solver1Comp);

		PAGCMInterfaceType[] dispatcher1Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher1Comp = remmos.newFcInstance(remmos.createFcType(dispatcher1Itftypes, "primitive"),
				new ControllerDescription("Dispatcher1", "primitive"), new ContentDescription(DispatcherImpl.class.getName()), null);

		Utils.getPAMembraneController(dispatcher1Comp).startMembrane();
		Remmos.addMonitoring(dispatcher1Comp);

		// Solver 2 -----------------------------------------------------
		PAGCMInterfaceType[] solver2Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver2Comp = remmos.newFcInstance(remmos.createFcType(solver2Itftypes, "composite"),
				new ControllerDescription("Solver2", "composite"), null, null);

		Utils.getPAMembraneController(solver2Comp).startMembrane();
		Remmos.addMonitoring(solver2Comp);
		Remmos.addExecutorController(solver2Comp);

		PAGCMInterfaceType[] dispatcher2Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher2Comp = remmos.newFcInstance(remmos.createFcType(dispatcher2Itftypes, "primitive"),
				new ControllerDescription("Dispatcher2", "primitive"), new ContentDescription(DispatcherImpl.class.getName()), null);

		Utils.getPAMembraneController(dispatcher2Comp).startMembrane();
		Remmos.addMonitoring(dispatcher2Comp);
	
		// Solver 3 ----------------------------------------------------
		PAGCMInterfaceType[] solver3Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver3Comp = remmos.newFcInstance(remmos.createFcType(solver3Itftypes, "composite"),
				new ControllerDescription("Solver3", "composite"), null, null);
	
		Utils.getPAMembraneController(solver3Comp).startMembrane();
		Remmos.addMonitoring(solver3Comp);
		Remmos.addExecutorController(solver3Comp);
	
		PAGCMInterfaceType[] dispatcher3Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher3Comp = remmos.newFcInstance(remmos.createFcType(dispatcher3Itftypes, "primitive"),
				new ControllerDescription("Dispatcher3", "primitive"), new ContentDescription(DispatcherImpl.class.getName()), null);

		Utils.getPAMembraneController(dispatcher3Comp).startMembrane();
		Remmos.addMonitoring(dispatcher3Comp);

		// Binding 1 ---------------------------------------------------
		Utils.getPAContentController(solver1Comp).addFcSubComponent(dispatcher1Comp);
		Utils.getPABindingController(solver1Comp).bindFc("solver", dispatcher1Comp.getFcInterface("solver"));

		Utils.getPAContentController(solver2Comp).addFcSubComponent(dispatcher2Comp);
		Utils.getPABindingController(solver2Comp).bindFc("solver", dispatcher2Comp.getFcInterface("solver"));

		Utils.getPAContentController(solver3Comp).addFcSubComponent(dispatcher3Comp);
		Utils.getPABindingController(solver3Comp).bindFc("solver", dispatcher3Comp.getFcInterface("solver"));

		Utils.getPAContentController(crackerComp).addFcSubComponent(balancerComp);
		Utils.getPAContentController(crackerComp).addFcSubComponent(solver1Comp);
		Utils.getPAContentController(crackerComp).addFcSubComponent(solver2Comp);
		Utils.getPAContentController(crackerComp).addFcSubComponent(solver3Comp);
		Utils.getPABindingController(crackerComp).bindFc("cracker", balancerComp.getFcInterface("cracker"));
		Utils.getPABindingController(balancerComp).bindFc("solver-1", solver1Comp.getFcInterface("solver"));
		Utils.getPABindingController(balancerComp).bindFc("solver-2", solver2Comp.getFcInterface("solver"));
		Utils.getPABindingController(balancerComp).bindFc("solver-3", solver3Comp.getFcInterface("solver"));

		// Workers -----------------------------------------------------


		System.out.println("Loading Application Descriptor......................");

		GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(Test2.class.getResource("Workers.xml"));
		System.out.println("Starting deployment...................................");
		gcmad.startDeployment();
		System.out.println("Waiting ready ...............................");
		gcmad.waitReady();

		System.out.println("Getting Node on VN1.........................");
		GCMVirtualNode VN1 = gcmad.getVirtualNode("VN1");
		VN1.waitReady();
		Node N1 = VN1.getANode();

		GCMVirtualNode VN2 = gcmad.getVirtualNode("VN2");
		VN2.waitReady();
		Node N2 = VN2.getANode();

        GCMVirtualNode VN3 = gcmad.getVirtualNode("VN3");
        VN3.waitReady();
        Node N3 = VN3.getANode();

		Remmos.getExecutorController(solver1Comp).addAction("addWorker", new AddWorkerAction("Dispatcher1", N1));
		Remmos.getExecutorController(solver2Comp).addAction("addWorker", new AddWorkerAction("Dispatcher2", N2));
		Remmos.getExecutorController(solver3Comp).addAction("addWorker", new AddWorkerAction("Dispatcher3", N3));

		// Init ------------------------------------------
		System.out.println("Enabling Monitoring..........................");
		Remmos.enableMonitoring(crackerComp);
		Thread.sleep(2000);
		System.out.println("Starting Cracker ................................");

	   	MonitorController mon = Remmos.getMonitorController(crackerComp);
    	mon.startGCMMonitoring();
    	Thread.sleep(1000);

    	PAContentController cc = Utils.getPAContentController(crackerComp);
    	for (Component subComp : cc.getFcSubComponents()) {
    		Remmos.getMonitorController(subComp).setRecordStoreCapacity(16);
    	}

    	//mon.addMetric("points", new PointsMetric());
    	//mon.enableMetric("points");

    	// RULE
    	Remmos.getAnalyzerController(crackerComp).addRule("always", new AlwaysAlarmRule());
    	
    	// PLAN
    	Remmos.getPlannerController(crackerComp).setPlan(new UpdatePointsPlan());
    	
    	// EXECUTOR
    	ExecutorController exec = Remmos.getExecutorController(crackerComp);
    
    	String path = "file:///user/mibanez/mape-component-controllers/src/Examples"
    			+ "/cl/niclabs/autonomic/examples/balancer/actions/utils.fscript";
    	exec.load((new URL(path)).toURI().getPath());
		exec.execute("wos($this)");

    	Utils.getPAGCMLifeCycleController(crackerComp).startFc();
    	System.out.println("Cracker started......____");

    	System.out.println("*\n*\n* Cracker ready: " + ((PAComponent) crackerComp).getID().toString() + "\n*\n*");

    	while(true) {
    	    try {
    	        Thread.sleep(10000);
    	    } catch (InterruptedException e) {
    	        e.printStackTrace();
    	    }
    	}
	}
}
