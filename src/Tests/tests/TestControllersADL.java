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
