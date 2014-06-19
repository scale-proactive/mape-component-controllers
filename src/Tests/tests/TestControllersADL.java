package tests;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class TestControllersADL extends CommonSetup {
	
	AFactory adlFactory;
	Component composite;

	@Before
	 public void setUp() throws Exception {
		super.setUp();
		adlFactory = (AFactory) AFactoryFactory.getAFactory();
	}
	
	@Test
    public void TestControllers() {
		try {
			composite = (Component) adlFactory.newAutonomicComponent("tests.components.Composite", null);

			Remmos.enableMonitoring(composite);
			assert("True".equals(Remmos.getExecutorController(composite).execute("true();").getValue()));
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				assert("True".equals(Remmos.getExecutorController(subComp).execute("true();").getValue()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
