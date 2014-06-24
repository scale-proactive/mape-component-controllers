package tests;

import org.junit.Before;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.extensions.autonomic.adl.AFactory;
import org.objectweb.proactive.extensions.autonomic.adl.AFactoryFactory;

public class CommonSetup {

    protected static AFactory adlFactory;
    protected static Component composite;

    @Before
    public void setUp() throws Exception {
    	System.setProperty("gcm.provider", "org.objectweb.proactive.core.component.Fractive");
        if (adlFactory == null || composite == null) {
        	adlFactory = (AFactory) AFactoryFactory.getAFactory();
        	composite = (Component) adlFactory.newAutonomicComponent("tests.components.Composite", null);
        }
    }

}
