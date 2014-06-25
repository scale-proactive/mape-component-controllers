package tests;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.ACConstants;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extensions.autonomic.controllers.planning.PlannerController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class TestADLSupport extends CommonSetup {

	@Test
    public void TestInterfaces() {
		
		int counter = 0;

		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if ( !(itf instanceof PAInterface) )
				continue;

			PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();

			if (type.getFcItfName().equals("test-itf" + ACConstants.INTERNAL_CLIENT_SUFFIX)) {
				assert(type.getFcItfName().equals("test-itf" + ACConstants.INTERNAL_CLIENT_SUFFIX));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.INTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				counter += 1;
			} else if (type.getFcItfName().equals("multicast-itf" + ACConstants.EXTERNAL_CLIENT_SUFFIX)) {
				assert(type.getFcItfName().equals("multicast-itf" + ACConstants.EXTERNAL_CLIENT_SUFFIX));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.MULTICAST_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorControllerMulticast.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.EXTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				counter += 100;
			} else if (type.getFcItfName().equals(ACConstants.INTERNAL_SERVER_NFITF)) {
				assert(type.getFcItfName().equals(ACConstants.INTERNAL_SERVER_NFITF));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.INTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.SERVER);
				counter += 10000;
			}
		}

		assert(counter == 10101);

		Component master = null;
		try {
			master = ((PAInterface) Utils.getPABindingController(composite).lookupFc("test-itf")).getFcItfOwner();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		assert(master != null);

		for (Object itf : ((PAComponent) master).getFcInterfaces()) {
			if ( !(itf instanceof PAInterface) )
				continue;

			PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();

			if (type.getFcItfName().equals("slave" + ACConstants.EXTERNAL_CLIENT_SUFFIX)) {
				assert(type.getFcItfName().equals("slave" + ACConstants.EXTERNAL_CLIENT_SUFFIX));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.EXTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				counter = counter * 2;
			}
		}

		assert(counter == 20202);

	}

	@Test
    public void TestComponentControllers() {

		MonitorController monitor = null;
		try { monitor = Remmos.getMonitorController(composite); }
		catch (Exception e) { e.printStackTrace(); }
		assert(monitor != null);

		AnalyzerController analyzer = null;
		try { analyzer = Remmos.getAnalyzerController(composite); }
		catch (NoSuchInterfaceException e) { e.printStackTrace(); }
		assert(analyzer != null);

		PlannerController planner = null;
		try { planner = Remmos.getPlannerController(composite); }
		catch (NoSuchInterfaceException e) { e.printStackTrace(); }
		assert(planner != null);

		ExecutorController executor = null;
		try { executor = Remmos.getExecutorController(composite); }
		catch (NoSuchInterfaceException e) { e.printStackTrace(); }
		assert(executor != null);
	
		Remmos.enableMonitoring(composite);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int counter = 0;
		for (String metricName : monitor.getMetricList().getValue()) {
			System.out.println("Metric on composite monitor: " + metricName);
			counter++;
		}
		assert(counter > 0);

		Object obj = executor.execute("true();").getValue();
		System.out.println(obj);
		assert("true".equals(obj.toString()));
		try {
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				assert("true".equals(Remmos.getExecutorController(subComp).execute("true();").getValue()));
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}
