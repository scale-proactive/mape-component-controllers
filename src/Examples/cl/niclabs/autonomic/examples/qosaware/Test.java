package cl.niclabs.autonomic.examples.qosaware;

import java.net.URL;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;

import cl.niclabs.autonomic.examples.qosaware.actions.AddWorkerAction;
import cl.niclabs.autonomic.examples.qosaware.actions.RemoveWorkerAction;
//import cl.niclabs.autonomic.examples.qosaware.components.BalancerAttr;
//import cl.niclabs.autonomic.examples.qosaware.components.DispatcherAttr;
import cl.niclabs.autonomic.examples.qosaware.components.FrontalImpl;
import cl.niclabs.autonomic.examples.qosaware.components.HTTPImpl;
import cl.niclabs.autonomic.examples.qosaware.components.HTTPItf;
import cl.niclabs.autonomic.examples.qosaware.components.HTTPSImpl;
import cl.niclabs.autonomic.examples.qosaware.components.JonasDispatcherAttr;
import cl.niclabs.autonomic.examples.qosaware.components.JonasDispatcherImpl;
import cl.niclabs.autonomic.examples.qosaware.components.JonasImpl;
import cl.niclabs.autonomic.examples.qosaware.components.JonasItf;
import cl.niclabs.autonomic.examples.qosaware.components.JonasRetItf;
//import cl.niclabs.autonomic.examples.qosaware.components.SolverItf;
import cl.niclabs.autonomic.examples.qosaware.components.SpringooItf;
//import cl.niclabs.autonomic.examples.qosaware.components.WorkerMulticastItf;
import cl.niclabs.autonomic.examples.qosaware.metrics.PointsMetric;
import cl.niclabs.autonomic.examples.qosaware.metrics.TimesMetric;
import cl.niclabs.autonomic.examples.qosaware.plans.UpdatePointsPlan;
import cl.niclabs.autonomic.examples.qosaware.rules.PointsChangeRule;

//TO REMOVE
//import org.etsi.uri.gcm.util.GCM;


public class Test {

	public static void main(String[] args) throws Exception {
		
		// Factory components
		Component boot = Utils.getBootstrapComponent();
		PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
		PAGenericFactory cf = Utils.getPAGenericFactory(boot);
		
		// Remmos
		Remmos remmos = new Remmos(tf, cf);
		
		// ---> createGCMItfType(STRING.NAME, STRING.SIGNATURE, BOOLEAN.IS_CLIENT, BOOLEAN.IS_OPT,
		//                       STRING.{"singleton", "multicast"})
		
		// Springoo ------------------------------------------------------
		PAGCMInterfaceType[] springooItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("clientReq", SpringooItf.class.getName(), false, false, "singleton")};
		Component springooComp = remmos.newFcInstance(remmos.createFcType(springooItftypes, "composite"),
				new ControllerDescription("Springoo", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(springooComp).startMembrane();
		Remmos.addMonitoring(springooComp);
		Remmos.addAnalysis(springooComp);
		Remmos.addPlannerController(springooComp);
		Remmos.addExecutorController(springooComp);
		
		// Frontal -----------------------------------------------------
		PAGCMInterfaceType[] frontalItftypes = new PAGCMInterfaceType[] {
			(PAGCMInterfaceType) tf.createGCMItfType("clientReq", SpringooItf.class.getName(), false, false, "singleton"),
			(PAGCMInterfaceType) tf.createGCMItfType("jeeReturn", JonasRetItf.class.getName(), false, false, "singleton"),
			(PAGCMInterfaceType) tf.createGCMItfType("httpReq", HTTPItf.class.getName(), true, false, "singleton"),
			(PAGCMInterfaceType) tf.createGCMItfType("httpsReq", HTTPItf.class.getName(), true, false, "singleton")};
		Component frontalComp = remmos.newFcInstance(remmos.createFcType(frontalItftypes, "primitive"),
				new ControllerDescription("Frontal", "primitive"),
				new ContentDescription(FrontalImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(frontalComp).startMembrane();
		Remmos.addMonitoring(frontalComp);

		// Apache -----------------------------------------------------
		PAGCMInterfaceType[] apacheItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("httpReq", HTTPItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("httpsReq", HTTPItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), true, false, "singleton")};
		Component apacheComp = remmos.newFcInstance(remmos.createFcType(apacheItftypes, "composite"),
				new ControllerDescription("Apache", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(apacheComp).startMembrane();
		Remmos.addMonitoring(apacheComp);
		Remmos.addExecutorController(apacheComp);

		// HTTP -----------------------------------------------------
		PAGCMInterfaceType[] httpItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("httpReq", HTTPItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), true, false, "singleton")};
		Component httpComp = remmos.newFcInstance(remmos.createFcType(httpItftypes, "primitive"),
				new ControllerDescription("HTTP", "primitive"),
				new ContentDescription(HTTPImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(httpComp).startMembrane();
		Remmos.addMonitoring(httpComp);

		// HTTPS -----------------------------------------------------
		PAGCMInterfaceType[] httpsItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("httpsReq", HTTPItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), true, false, "singleton")};
		Component httpsComp = remmos.newFcInstance(remmos.createFcType(httpsItftypes, "primitive"),
				new ControllerDescription("HTTPS", "primitive"),
				new ContentDescription(HTTPSImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(httpsComp).startMembrane();
		Remmos.addMonitoring(httpsComp);


		// JONAS -----------------------------------------------------
		PAGCMInterfaceType[] jonasItftypes = new PAGCMInterfaceType[] {
				//TODO: change interface jeeReq to multicast
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReturn", JonasRetItf.class.getName(), true, false, "singleton")};
		Component jonasComp = remmos.newFcInstance(remmos.createFcType(jonasItftypes, "composite"),
				new ControllerDescription("Jonas", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(jonasComp).startMembrane();
		Remmos.addMonitoring(jonasComp);
		Remmos.addAnalysis(jonasComp);
		Remmos.addPlannerController(jonasComp);
		Remmos.addExecutorController(jonasComp);

		// JONAS Dispatcher -----------------------------------------------------
		PAGCMInterfaceType[] jonasDispatcherItftypes = new PAGCMInterfaceType[] {
				//TODO: change interface jeeReq to multicast
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", JonasDispatcherAttr.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReqm", JonasItf.class.getName(), true, false, "singleton")};
		Component jonasDispatcherComp = remmos.newFcInstance(remmos.createFcType(jonasDispatcherItftypes, "composite"),
				new ControllerDescription("JonasDispatcher", "primitive"),
				new ContentDescription(JonasDispatcherImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(jonasDispatcherComp).startMembrane();
		Remmos.addMonitoring(jonasDispatcherComp);

		// JonasInstance -----------------------------------------------------
		PAGCMInterfaceType[] jonas1Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReq", JonasItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("jeeReturn", JonasRetItf.class.getName(), true, false, "singleton")};
		Component jonas1Comp = remmos.newFcInstance(remmos.createFcType(jonas1Itftypes, "primitive"),
				new ControllerDescription("Jonas1", "primitive"),
				new ContentDescription(JonasImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(jonas1Comp).startMembrane();
		Remmos.addMonitoring(jonas1Comp);

/*
		// Balancer -----------------------------------------------------
		PAGCMInterfaceType[] balancerItftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("clientReq", SpringooItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-1", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-2", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("solver-3", SolverItf.class.getName(), true, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", BalancerAttr.class.getName(), false, false, "singleton")};
		Component balancerComp = remmos.newFcInstance(remmos.createFcType(balancerItftypes, "primitive"),
				new ControllerDescription("Balancer", "primitive"),
				new ContentDescription(BalancerImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);
	
		Utils.getPAMembraneController(balancerComp).startMembrane();
		Remmos.addMonitoring(balancerComp);

		// Solver 1 -----------------------------------------------------
		PAGCMInterfaceType[] solver1Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver1Comp = remmos.newFcInstance(remmos.createFcType(solver1Itftypes, "composite"),
				new ControllerDescription("Solver1", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(solver1Comp).startMembrane();
		Remmos.addMonitoring(solver1Comp);
		Remmos.addExecutorController(solver1Comp);

		PAGCMInterfaceType[] dispatcher1Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher1Comp = remmos.newFcInstance(remmos.createFcType(dispatcher1Itftypes, "primitive"),
				new ControllerDescription("Dispatcher1", "primitive"),
				new ContentDescription(DispatcherImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(dispatcher1Comp).startMembrane();
		Remmos.addMonitoring(dispatcher1Comp);

		// Solver 2 -----------------------------------------------------
		PAGCMInterfaceType[] solver2Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver2Comp = remmos.newFcInstance(remmos.createFcType(solver2Itftypes, "composite"),
				new ControllerDescription("Solver2", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(solver2Comp).startMembrane();
		Remmos.addMonitoring(solver2Comp);
		Remmos.addExecutorController(solver2Comp);

		PAGCMInterfaceType[] dispatcher2Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher2Comp = remmos.newFcInstance(remmos.createFcType(dispatcher2Itftypes, "primitive"),
				new ControllerDescription("Dispatcher2", "primitive"),
				new ContentDescription(DispatcherImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(dispatcher2Comp).startMembrane();
		Remmos.addMonitoring(dispatcher2Comp);
	
		// Solver 3 ----------------------------------------------------
		PAGCMInterfaceType[] solver3Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton")};
		Component solver3Comp = remmos.newFcInstance(remmos.createFcType(solver3Itftypes, "composite"),
				new ControllerDescription("Solver3", "composite"),
				new ContentDescription(Composite.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);
	
		Utils.getPAMembraneController(solver3Comp).startMembrane();
		Remmos.addMonitoring(solver3Comp);
		Remmos.addExecutorController(solver3Comp);
	
		PAGCMInterfaceType[] dispatcher3Itftypes = new PAGCMInterfaceType[] {
				(PAGCMInterfaceType) tf.createGCMItfType("solver", SolverItf.class.getName(), false, false, "singleton"),
				(PAGCMInterfaceType) tf.createGCMItfType("worker-multicast", WorkerMulticastItf.class.getName(), true, true, "multicast"),
				(PAGCMInterfaceType) tf.createGCMItfType("attribute-controller", DispatcherAttr.class.getName(), false, false, "singleton")};
		Component dispatcher3Comp = remmos.newFcInstance(remmos.createFcType(dispatcher3Itftypes, "primitive"),
				new ControllerDescription("Dispatcher3", "primitive"),
				new ContentDescription(DispatcherImpl.class.getName(), null, new ComponentRunActive() {
					@Override
					public void runComponentActivity(Body body) {
						(new ComponentMultiActiveService(body)).multiActiveServing();
					}
				}, null),
				null);

		Utils.getPAMembraneController(dispatcher3Comp).startMembrane();
		Remmos.addMonitoring(dispatcher3Comp);

*/

		// Binding 1 ---------------------------------------------------
		Utils.getPAContentController(apacheComp).addFcSubComponent(httpComp);
		Utils.getPAContentController(apacheComp).addFcSubComponent(httpsComp);
		Utils.getPABindingController(apacheComp).bindFc("httpReq", httpComp.getFcInterface("httpReq"));
		Utils.getPABindingController(apacheComp).bindFc("httpsReq", httpsComp.getFcInterface("httpsReq"));
		Utils.getPABindingController(httpComp).bindFc("jeeReq", apacheComp.getFcInterface("jeeReq"));
		Utils.getPABindingController(httpsComp).bindFc("jeeReq", apacheComp.getFcInterface("jeeReq"));

		Utils.getPAContentController(jonasComp).addFcSubComponent(jonas1Comp);
		Utils.getPAContentController(jonasComp).addFcSubComponent(jonasDispatcherComp);
		Utils.getPABindingController(jonasComp).bindFc("jeeReq", jonasDispatcherComp.getFcInterface("jeeReq"));
		Utils.getPABindingController(jonasDispatcherComp).bindFc("jeeReqm", jonas1Comp.getFcInterface("jeeReq"));
		Utils.getPABindingController(jonas1Comp).bindFc("jeeReturn", jonasComp.getFcInterface("jeeReturn"));


		Utils.getPAContentController(springooComp).addFcSubComponent(frontalComp);
		Utils.getPAContentController(springooComp).addFcSubComponent(apacheComp);
		Utils.getPAContentController(springooComp).addFcSubComponent(jonasComp);
		Utils.getPABindingController(springooComp).bindFc("clientReq", frontalComp.getFcInterface("clientReq"));
		Utils.getPABindingController(frontalComp).bindFc("httpReq", apacheComp.getFcInterface("httpReq"));
		Utils.getPABindingController(frontalComp).bindFc("httpsReq", apacheComp.getFcInterface("httpsReq"));
		Utils.getPABindingController(apacheComp).bindFc("jeeReq", jonasComp.getFcInterface("jeeReq"));
		Utils.getPABindingController(jonasComp).bindFc("jeeReturn", frontalComp.getFcInterface("jeeReturn"));

		// Workers -----------------------------------------------------


		System.out.println("Loading Application Descriptor......................");

		GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(Test.class.getResource("Workers.xml"));
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

		// Init ------------------------------------------
		//System.out.println("[Test] Checking sanity of monitor infrastructure .....");
		//Component[] compControllers = Utils.getPAMembraneController(jonas1Comp).nfGetFcSubComponents();
		//for (Component comp : compControllers) {
		//	System.out.println("[Test] ... jonas1Comp." + GCM.getNameController(comp).getFcName());
		//}

		System.out.println("Enabling Monitoring..........................");
		Remmos.enableMonitoring(springooComp);
		Thread.sleep(5000);
		System.out.println("Setting up MAPE Loop on Springoo ................................");

	   	MonitorController mon = Remmos.getMonitorController(springooComp);
    	mon.startGCMMonitoring();
    	Thread.sleep(1000);

    	int RECORDS_CAPACITY = 5;
    	mon.setRecordStoreCapacity(RECORDS_CAPACITY);
    	PAContentController cc = Utils.getPAContentController(springooComp);
    	for (Component subComp : cc.getFcSubComponents()) {
    		Remmos.getMonitorController(subComp).setRecordStoreCapacity(RECORDS_CAPACITY);
    	}

    	mon.addMetric("times", new TimesMetric());
    	mon.addMetric("points", new PointsMetric(RECORDS_CAPACITY));
    	mon.enableMetric("times");
    	//mon.enableMetric("points");

    	// RULE
    	//Remmos.getAnalyzerController(springooComp).addRule("always", new PointsChangeRule());
    	
    	// PLAN
    	Remmos.getPlannerController(springooComp).setPlan(new UpdatePointsPlan());
    	
    	// EXECUTOR
		//Remmos.getExecutorController(solver1Comp).addAction("addWorker", new AddWorkerAction("Dispatcher1", N1));
		//Remmos.getExecutorController(solver2Comp).addAction("addWorker", new AddWorkerAction("Dispatcher2", N2));
		//Remmos.getExecutorController(solver3Comp).addAction("addWorker", new AddWorkerAction("Dispatcher3", N3));

		//Remmos.getExecutorController(solver1Comp).addAction("removeWorker", new RemoveWorkerAction("Dispatcher1"));
		//Remmos.getExecutorController(solver2Comp).addAction("removeWorker", new RemoveWorkerAction("Dispatcher2"));
		//Remmos.getExecutorController(solver3Comp).addAction("removeWorker", new RemoveWorkerAction("Dispatcher3"));

    	ExecutorController exec = Remmos.getExecutorController(springooComp);
    
    	//String path = "file:///home/mibanez/Taller/memoria/mape-component-controllers/src/Examples"
    	//		+ "/cl/niclabs/autonomic/examples/balancer/actions/utils.fscript";
    	String path = "file:///user/cruz/git/mape-component-controllers/src/Examples"
    	   		+ "/cl/niclabs/autonomic/examples/qosaware/actions/utils.fscript";
    	path = "file://"+System.getProperty("user.home")+"/src/Examples"
    			+ "/cl/niclabs/autonomic/examples/qosaware/actions/utils.fscript";
    	exec.load((new URL(path)).toURI().getPath());
    	//exec.execute("wos($this)");
		//exec.execute("wos2($this)");
		//exec.execute("wos3($this)");
		//exec.execute("wos3($this)");
		//exec.execute("wos3($this)");
		System.out.println("Starting Springoo Lifecycle ................................");
    	Utils.getPAGCMLifeCycleController(springooComp).startFc();
    	System.out.println("Springoo started......____");

    	System.out.println("*\n*\n* Springoo ready: " + ((PAComponent) springooComp).getID().toString() + "\n*\n*");

    	while(true) {
    	    try {
    	        Thread.sleep(10000);
    	    } catch (InterruptedException e) {
    	        e.printStackTrace();
    	    }
    	}
	}
}
