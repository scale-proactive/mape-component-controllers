package examples;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.extra.component.mape.utils.ObjectWrapper;

import examples.services.ServiceClient;
import examples.services.autoadaptable.AASCST;
import examples.services.autoadaptable.AASFactory;
import examples.services.autoadaptable.actions.AddSlaveAction;
import examples.services.autoadaptable.metrics.ResponseTimeMetric;
import examples.services.autoadaptable.metrics.OptimalPointsMetric;
import examples.services.autoadaptable.plans.AdaptationPlan;
import examples.services.autoadaptable.rules.VariationRule;

public class TestAAS extends TestService {

	TestAAS() throws Exception {
		super(null);

	}

	public void run() {
		try {
			Component service = AASFactory.createService(null, patf, pagf);
			Component manager = AASFactory.createManager(null, patf, pagf);
			Component solver1 = AASFactory.createCompleteSolver(1, null, patf, pagf);
			Component solver2 = AASFactory.createCompleteSolver(1, null, patf, pagf);
			Component solver3 = AASFactory.createCompleteSolver(1, null, patf, pagf);
			
			AASFactory.bindService(service, manager, new Component[] { solver1, solver2, solver3 });

			Utils.getPAGCMLifeCycleController(service).startFc();

			Remmos.enableMonitoring(service); // [!] too much important, maybe merge with MonitoController.startMontioring?
			MonitorController monitor = Remmos.getMonitorController(service);
			monitor.startGCMMonitoring();
			Thread.sleep(2000);
			
			monitor.addMetric(AASCST.OPTIMAL_POINTS_METRIC, new OptimalPointsMetric());
			Remmos.getMonitorController(solver1).addMetric(AASCST.RESPONSE_TIME_METRIC, new ResponseTimeMetric(AASCST.SOLVER));
			Remmos.getMonitorController(solver2).addMetric(AASCST.RESPONSE_TIME_METRIC, new ResponseTimeMetric(AASCST.SOLVER));
			Remmos.getMonitorController(solver3).addMetric(AASCST.RESPONSE_TIME_METRIC, new ResponseTimeMetric(AASCST.SOLVER));
			
			Remmos.getAnalyzerController(service).addRule(AASCST.VARIATION_RULE, new VariationRule(0.3));
			Remmos.getPlannerController(service).setPlan(new AdaptationPlan());
			
			Remmos.getExecutorController(solver1).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(null));
			Remmos.getExecutorController(solver2).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(null));
			Remmos.getExecutorController(solver3).addAction(AASCST.ADD_SLAVE_ACTION, new AddSlaveAction(null));
			
			(new Thread(new ServiceClient(service, 4, "client1"))).start();
		
			long initTime = System.currentTimeMillis();
			int counter = 0;
			while (true) {
				Object obj = monitor.calculateMetric(AASCST.OPTIMAL_POINTS_METRIC).getValue();
				long time = System.currentTimeMillis() - initTime;
				System.out.println(time + "\t" + obj);
				
				Thread.sleep(3000);
				
				counter++;
				if (counter == 10) {
					System.out.println("[Adding new slave on solver1... ]");
					ObjectWrapper ow = Remmos.getExecutorController(solver1).executeAction(AASCST.ADD_SLAVE_ACTION);
					if (ow.isValid() && (boolean) ow.getObjectOrNull()) {
						System.out.println("[OK, slave added on solver1  ]");
					} else {
						System.out.println("[FAIL, when trying to add a slave to solver 1... ! ]");
					}
					
					ow = Remmos.getExecutorController(solver1).executeAction(AASCST.ADD_SLAVE_ACTION);
					if (ow.isValid() && (boolean) ow.getObjectOrNull()) {
						System.out.println("[OK, slave added on solver1  ]");
					} else {
						System.out.println("[FAIL, when trying to add a slave to solver 1... ! ]");
					}
					ow = Remmos.getExecutorController(solver1).executeAction(AASCST.ADD_SLAVE_ACTION);
					if (ow.isValid() && (boolean) ow.getObjectOrNull()) {
						System.out.println("[OK, slave added on solver1  ]");
					} else {
						System.out.println("[FAIL, when trying to add a slave to solver 1... ! ]");
					}

					/*
					Component slave = AASFactory.createSlave(null, patf, pagf);
					Component master = Action.getBindComponent(solver1, AASCST.SOLVER);

					System.out.println("Stopping solver");
					PAGCMLifeCycleController lc = Utils.getPAGCMLifeCycleController(solver1);
					lc.stopFc();

					for (Component subComp : Utils.getPAContentController(solver1).getFcSubComponents()) {
						if (((PAComponent) subComp).getComponentParameters().getName().equals(AASCST.SLAVE_COMP_NAME)) {
							Utils.getPABindingController(master).unbindFc(AASCST.SLAVE);
							Utils.getPAContentController(solver1).removeFcSubComponent(subComp);
						}
					}
				
					System.out.println("add slave to solver");
					Utils.getPAContentController(solver1).addFcSubComponent(slave);
					System.out.println("bind slave to master");
					Utils.getPABindingController(master).bindFc(AASCST.SLAVE, slave.getFcInterface(AASCST.SLAVE));
					System.out.println("setting the attributes");
					System.out.println("Starting solver " + ((PAComponent) solver1).getComponentParameters().getControllerDescription().getName());
					lc.startFc();
					*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		(new TestAAS()).run();
	}

}
