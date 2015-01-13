package cl.niclabs.autonomic.examples.balancer;

import java.net.URL;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

import cl.niclabs.autonomic.examples.balancer.metrics.PointsMetric;
import cl.niclabs.autonomic.examples.balancer.plans.UpdatePointsPlan;
import cl.niclabs.autonomic.examples.balancer.rules.PointsChangeRule;

public class Test {

	public static void main(String[] args) throws Exception {

		/*File appDescriptor = new File((new URL("file:///user/mibanez/mape-component-controllers/src/Examples"
    			+ "/cl/niclabs/autonomic/examples/balancer/Balancer.xml")).toURI().getPath());
		GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(appDescriptor);
		gcmad.startDeployment();
		gcmad.waitReady();
		GCMVirtualNode vn = gcmad.getVirtualNode("VN0");
		vn.waitReady();
		List<Node> nodeList = new ArrayList<Node>();
		nodeList.add(vn.getANode());

		Map<String, Object> context = new HashMap<String, Object>();
		context.put(org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider.NODES_ID, nodeList);
		*/
		AFactory factory = (AFactory) AFactoryFactory.getAFactory();
		Component component = (Component) factory.newAutonomicComponent("cl.niclabs.autonomic.examples.balancer"
				+ ".components.Cracker", null);

    	// MONITOR
    	Remmos.enableMonitoring(component);
    	Thread.sleep(1000);
    	MonitorController mon = Remmos.getMonitorController(component);
    	mon.startGCMMonitoring();
    	Thread.sleep(1000);
    	
    	mon.addMetric("points", new PointsMetric());
    	mon.enableMetric("points");

    	PAContentController cc = Utils.getPAContentController(component);
    	for (Component subComp : cc.getFcSubComponents()) {
    		Remmos.getMonitorController(subComp).setRecordStoreCapacity(16);
    	}

    	// RULE
    	Remmos.getAnalyzerController(component).addRule("always", new PointsChangeRule());
    	
    	// PLAN
    	Remmos.getPlannerController(component).setPlan(new UpdatePointsPlan());
    	
    	// EXECUTOR
    	ExecutorController exec = Remmos.getExecutorController(component);
    
    	String path = "file:///user/cruz/git/mape-component-controllers/src/Examples"
    			+ "/cl/niclabs/autonomic/examples/balancer/actions/utils.fscript";
        System.out.println("Loading path to GCMScript engine ... " + path);
    	exec.load((new URL(path)).toURI().getPath());
    	exec.execute("gcma = deploy-gcma(\"src/Examples/cl/niclabs/autonomic/examples/balancer/Workers.xml\");");
		exec.execute("add-worker($this/child::Solver1, $gcma/gcmvn::VN1/gcmnode::*);");
		exec.execute("add-worker($this/child::Solver2, $gcma/gcmvn::VN2/gcmnode::*);");
		exec.execute("add-worker($this/child::Solver3, $gcma/gcmvn::VN3/gcmnode::*);");   
    	Utils.getPAGCMLifeCycleController(component).startFc();
    	
    	System.out.println("*\n*\n* Cracker ready: " + ((PAComponent) component).getID().toString() + "\n*\n*");

    	while(true) {
    	    try {
                System.out.println("Waiting 10 ...");
    	        Thread.sleep(10000);
    	    } catch (InterruptedException e) {
    	        e.printStackTrace();
    	    }
    	}
	}

}
