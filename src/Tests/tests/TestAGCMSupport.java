package tests;

import static org.junit.Assert.fail;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

import tests.components.Slave;

public class TestAGCMSupport extends CommonSetup {
    
    @Test
    public void TestAComponentInstantiaton() {
    	ExecutorController executor;
		try {
			executor = Remmos.getExecutorController(composite);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
		String appDescriptor = this.getClass().getResource("GCMALocal.xml").getPath();
    	System.out.println("-- " + executor.execute("gcma = deploy-gcma(\"" + appDescriptor + "\");").getValue());
    	System.out.println("-- " + executor.execute("slave = gcm-new-autonomic(\"tests.components.Slave\", $gcma);").getValue());
    	System.out.println("-- " + executor.execute("set-name($slave, \"Slave2\");").getValue());
    	System.out.println("-- " + executor.execute("stop($this);").getValue());
    	System.out.println("-- " + executor.execute("add($this, $slave);").getValue());
    	System.out.println("-- " + executor.execute("start($this);").getValue());

    	System.out.println("-- " + executor.execute("$this/child::*;").getValue());
	

    	
    	Component slave = null;
    	try {
			for (Component subComp : Utils.getPAContentController(composite).getFcSubComponents()) {
				String name = GCM.getNameController(subComp).getFcName();
				System.out.println("subcomp: " + name);
				if (name.equals("Slave2")) {
					slave = subComp;
					break;
				}
			}
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			
			return;
		}
    	
    	assert (slave != null);
    	
    	try {
    		Slave itf = (Slave) slave.getFcInterface("slave");
    		itf.run2();
			System.out.println(Remmos.getExecutorController(slave).execute("true();").getValue());
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
	@Test
	public void TestMetrics() throws NoSuchInterfaceException {
		
		ExecutorController executor = Remmos.getExecutorController(composite);
		
		String result2 = executor.execute("$this/child::Master/metric::avgInc;").getValue();
		System.out.println(result2);

		String result = executor.execute("name($this/child::Master/metric::avgInc);").getValue();
		System.out.println(result);
		assert "avgInc".equals(result);
	}
}
