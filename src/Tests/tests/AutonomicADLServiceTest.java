package tests;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.etsi.uri.gcm.api.control.MonitorController;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.ACConstants;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class AutonomicADLServiceTest extends CommonSetup {

	Factory adlFactory;
	Component composite;

	@Before
	 public void setUp() throws Exception {
		super.setUp();
		adlFactory = AFactoryFactory.getAFactory();
		composite = (Component) adlFactory.newComponent("tests.components.Composite", new HashMap<String, Object>());
	}

	@Test
    public void nfInterfacesAddition() {
		
		boolean found = false;
		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if (itf instanceof PAInterface
					&& ((PAInterface) itf).getFcItfName().equals("test-itf" + ACConstants.INTERNAL_CLIENT_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
				assert(type.getFcItfName().equals("test-itf" + ACConstants.INTERNAL_CLIENT_SUFFIX));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.INTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				found = true;
				break;
			}
		}
		assert(found);

		found = false;
		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if (itf instanceof PAInterface
					&& ((PAInterface) itf).getFcItfName().equals("multicast-itf" + ACConstants.EXTERNAL_CLIENT_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
			assert(type.getFcItfName().equals("multicast-itf" + ACConstants.EXTERNAL_CLIENT_SUFFIX));
			assert(type.getGCMCardinality().equals(PAGCMTypeFactory.MULTICAST_CARDINALITY));
			assert(type.getFcItfSignature().equals(MonitorControllerMulticast.class.getName()));
			assert(type.isInternal() == PAGCMTypeFactory.EXTERNAL);
			assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				found = true;
				break;
			}
		}
		assert(found);
		
		found = false;
		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if (itf instanceof PAInterface
					&& ((PAInterface) itf).getFcItfName().equals(ACConstants.INTERNAL_SERVER_NFITF)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
				assert(type.getFcItfName().equals(ACConstants.INTERNAL_SERVER_NFITF));
				assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
				assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
				assert(type.isInternal() == PAGCMTypeFactory.INTERNAL);
				assert(type.isFcClientItf() == PAGCMTypeFactory.SERVER);
				found = true;
				break;
			}
		}
		assert(found);
		
		Component master = null;
		try {
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				if (((PAComponent) subComp).getComponentParameters().getName().equals("Master")) {
					master = subComp;
				}
			}
		} catch (NoSuchInterfaceException e) {
			fail(e.getMessage());
		}
		assert(master != null);
	
		found = false;
		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if (itf instanceof PAInterface
					&& ((PAInterface) itf).getFcItfName().equals("master" + ACConstants.EXTERNAL_CLIENT_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
			assert(type.getFcItfName().equals("master" + ACConstants.EXTERNAL_CLIENT_SUFFIX));
			assert(type.getGCMCardinality().equals(PAGCMTypeFactory.SINGLETON_CARDINALITY));
			assert(type.getFcItfSignature().equals(MonitorController.class.getName()));
			assert(type.isInternal() == PAGCMTypeFactory.EXTERNAL);
			assert(type.isFcClientItf() == PAGCMTypeFactory.CLIENT);
				found = true;
				break;
			}
		}
		assert(found);
	}
	
	@Test
    public void nfControllersAddition() {

		for (Object itf : ((PAComponent) composite).getFcInterfaces()) {
			if (itf instanceof PAInterface) {
				System.out.println(((PAGCMInterfaceType) ((PAInterface) itf).getFcItfType()).getFcItfName());
			}
		}
		try {
			Utils.getPAMembraneController(composite).startMembrane();
			Remmos.addMonitoring(composite);
			Remmos.addAnalysis(composite);
			Remmos.addPlannerController(composite);
			Remmos.addExecutorController(composite);
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				Utils.getPAMembraneController(subComp).startMembrane();
				Remmos.addMonitoring(subComp);
				Remmos.addAnalysis(subComp);
				Remmos.addPlannerController(subComp);
				Remmos.addExecutorController(subComp);
			}
			
			Remmos.enableMonitoring(composite);
			assert( (boolean) Remmos.getExecutorController(composite).execute("true();").getObject());
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				assert( (boolean) Remmos.getExecutorController(subComp).execute("true();").getObject());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
