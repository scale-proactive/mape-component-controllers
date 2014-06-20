package tests;

import org.junit.Test;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class TestMonitorNode extends CommonSetup {

	@Test
	public void TestMetrics() throws NoSuchInterfaceException {
		
		ExecutorController executor = Remmos.getExecutorController(composite);
		
		String result = executor.execute("name($this/child::Master/metric::avcInc);").getValue();
		System.out.println(result);
		assert "avcInc".equals(result);
	}
}
