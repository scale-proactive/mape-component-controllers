package tests;

import org.junit.Before;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import functionalTests.ComponentTest;

public abstract class CommonSetup extends ComponentTest {

    protected static PAGCMTypeFactory patf;
	protected static PAGenericFactory pagf;


    @Before
    public void setUp() throws Exception {
    	System.setProperty("gcm.provider", "org.objectweb.proactive.core.component.Fractive");
        if (patf == null || pagf == null) {
        	Component boot = Utils.getBootstrapComponent();
    		patf = Utils.getPAGCMTypeFactory(boot);
    		pagf = Utils.getPAGenericFactory(boot);
        }
        
    }

}
