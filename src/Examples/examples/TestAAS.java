package examples;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

import examples.services.ServiceClient;
import examples.services.autoadaptable.AASCST;
import examples.services.autoadaptable.AASFactory;
import examples.services.autoadaptable.actions.AddSlaveAction;
import examples.services.autoadaptable.actions.RemoveSlaveAction;
import examples.services.autoadaptable.metrics.OptimalPointsMetric;
import examples.services.autoadaptable.plans.AdaptationPlan;
import examples.services.autoadaptable.rules.VariationRule;

public class TestAAS extends AbstractTestService {

	TestAAS() throws Exception {
		super("file:///home/mibanez/Taller/memoria/mape-component-controllers/src/Examples/examples/GCMALocal.xml");
	}

	public void run() {
		try {
			/*
			GCMVirtualNode VN0 = gcma.getVirtualNode("VN0"); 
			GCMVirtualNode[] vnodes = new GCMVirtualNode[3];
			for (int i = 0; i < 3; i++) {
				vnodes[i] = gcma.getVirtualNode("VN" + (i+1));
			}
			VN0.waitReady();
		    for (int i = 0; i < 3; i++) {
				vnodes[i].waitReady();
		    }
		 	*/
		    Node N0 = null; //VN0.getANode();
			Node[] nodes = new Node[3];
		    for (int i = 0; i < 3; i++) {
		    	nodes[i] = null; //vnodes[i].getANode();    	
		    }

			Component service = AASFactory.createService(N0, patf, pagf);
			Component manager = AASFactory.createManager(N0, patf, pagf);
			Component solver1 = AASFactory.createCompleteSolver(1, nodes[0], patf, pagf);
			Component solver2 = AASFactory.createCompleteSolver(1, nodes[1], patf, pagf);
			Component solver3 = AASFactory.createCompleteSolver(1, nodes[2], patf, pagf);
			
			AASFactory.bindService(service, manager, new Component[] { solver1, solver2, solver3 });

			Utils.getPAGCMLifeCycleController(service).startFc();

			Remmos.enableMonitoring(service); // [!] too much important, maybe merge with MonitoController.startMontioring?
			MonitorController monitor = Remmos.getMonitorController(service);
			monitor.startGCMMonitoring();
			Thread.sleep(2000);
			
			
			monitor.addMetric(AASCST.OPTIMAL_POINTS_METRIC, new OptimalPointsMetric());
			monitor.enableMetric(AASCST.OPTIMAL_POINTS_METRIC);

			MonitorController mons1, mons2, mons3;
	
			mons1 = Remmos.getMonitorController(solver1);
			mons1.setRecordStoreCapacity(8);
			
			mons2 = Remmos.getMonitorController(solver2);
			mons2.setRecordStoreCapacity(8);
	
			mons3 = Remmos.getMonitorController(solver3);
			mons3.setRecordStoreCapacity(8);
			
			Remmos.getAnalyzerController(service).addRule(AASCST.VARIATION_RULE, new VariationRule());
			Remmos.getPlannerController(service).setPlan(new AdaptationPlan());
			
			Remmos.getExecutorController(solver1).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(nodes[0]));
			Remmos.getExecutorController(solver2).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(nodes[1]));
			Remmos.getExecutorController(solver3).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(nodes[2]));

			Remmos.getExecutorController(solver1).addAction(AASCST.REMOVE_SLAVE_ACTION, new RemoveSlaveAction(nodes[0]));
			Remmos.getExecutorController(solver2).addAction(AASCST.REMOVE_SLAVE_ACTION, new RemoveSlaveAction(nodes[1]));
			Remmos.getExecutorController(solver3).addAction(AASCST.REMOVE_SLAVE_ACTION, new RemoveSlaveAction(nodes[2]));
			
			(new Thread(new ServiceClient(service, 4, "client1"))).start();
		
			long initTime = System.currentTimeMillis();
			int counter = 0;
			while (true) {
				//Object obj = monitor.calculateMetric(AASCST.OPTIMAL_POINTS_METRIC).getValue();
				//double time = (System.currentTimeMillis() - initTime)/60000.0;
				// System.out.printf("%.3f\t" + obj + "\n", time);

				System.out.printf("%.3f, %.3f, %.3f\n",
						mons1.calculateMetric("avgInc").getValue(),
						mons2.calculateMetric("avgInc").getValue(),
						mons3.calculateMetric("avgInc").getValue());
				
				Thread.sleep(3000);
				
				counter++;
				if (counter == 15) {
					addSlave(solver1);
					//addSlave(solver1);
					//addSlave(solver1);
				}
				if (counter == 50) {
					addSlave(solver3);
					addSlave(solver3);
					addSlave(solver3);
				}
				if (counter == 75) {
					removeSlave(solver1);
					removeSlave(solver1);
					removeSlave(solver1);
					removeSlave(solver3);
					removeSlave(solver3);
					removeSlave(solver3);
					addSlave(solver2);
					addSlave(solver2);
					addSlave(solver2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addSlave(Component solver) throws NoSuchInterfaceException {
		Wrapper<Boolean> ow = Remmos.getExecutorController(solver).executeAction(AASCST.ADD_SLAVE_ACTION);
		if (ow.isValid() && ow.getValue()) {
			System.out.println("[OK, slave added on solver  ]");
		} else {
			System.out.println("[FAIL, when trying to add a slave to solver ... ! ]");
		}
	}
	
	private void removeSlave(Component solver) throws NoSuchInterfaceException {
		Wrapper<Boolean> ow = Remmos.getExecutorController(solver).executeAction(AASCST.REMOVE_SLAVE_ACTION);
		if (ow.isValid() && ow.getValue()) {
			System.out.println("[OK, slave removed on solver  ]");
		} else {
			System.out.println("[FAIL, when trying to remove a slave to solver ... ! ]");
		}
	}
	public static void main(String[] args) throws Exception {
		(new TestAAS()).run();
	}

}
