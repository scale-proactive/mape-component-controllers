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
					&& ((PAInterface) itf).getFcItfName().equals("test-itf" + ACConstants.ANF_CLIENT_ITF_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
				assert(type.getFcItfName().equals("test-itf" + ACConstants.ANF_CLIENT_ITF_SUFFIX));
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
					&& ((PAInterface) itf).getFcItfName().equals("multicast-itf" + ACConstants.ANF_CLIENT_ITF_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
			assert(type.getFcItfName().equals("multicast-itf" + ACConstants.ANF_CLIENT_ITF_SUFFIX));
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
					&& ((PAInterface) itf).getFcItfName().equals(ACConstants.ANF_INTERNAL_SERVER_ITF)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
				assert(type.getFcItfName().equals(ACConstants.ANF_INTERNAL_SERVER_ITF));
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
					&& ((PAInterface) itf).getFcItfName().equals("master" + ACConstants.ANF_CLIENT_ITF_SUFFIX)) {
				PAGCMInterfaceType type = (PAGCMInterfaceType) ((PAInterface) itf).getFcItfType();
			assert(type.getFcItfName().equals("master" + ACConstants.ANF_CLIENT_ITF_SUFFIX));
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
}
