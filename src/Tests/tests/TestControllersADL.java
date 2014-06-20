package tests;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class TestControllersADL extends CommonSetup {
	
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
