package tests;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;
import java.util.Set;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.fscript.FScript;
import org.objectweb.fractal.fscript.FScriptEngine;
import org.objectweb.fractal.fscript.FScriptException;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.AGCMModel;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.MetricNode;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import tests.components.Master;

public class TestMetricNode extends CommonSetup {

	protected FScriptEngine engine;
	protected AGCMModel model;
	
	protected MetricNode node;


    @SuppressWarnings("rawtypes")
	@Before
    public void setUp() throws Exception {
    	super.setUp();
       
        String defaultFcProvider = System.getProperty("fractal.provider");
        if (defaultFcProvider == null) {
        	defaultFcProvider = System.getProperty("gcm.provider");
        	if (defaultFcProvider == null) {
        		 throw new ReconfigurationException("Unable to find neither fractal nor gcm provier");
        	}
		}
      
        System.setProperty("fractal.provider", "org.objectweb.fractal.julia.Julia");
        Component gcmScript = GCMScript.newEngineFromAdl(ExecutorControllerImpl.AGCMSCRIPT_ADL);
        this.engine = FScript.getFScriptEngine(gcmScript);
        this.engine.setGlobalVariable("this", ((GCMNodeFactory) FScript.getNodeFactory(gcmScript))
                .createGCMComponentNode(composite));

        System.setProperty("fractal.provider", defaultFcProvider);
        
        model = new AGCMModel();
        model.startFc();

        node = (MetricNode) ((Set) engine.execute("$this/metric::avgOut;")).toArray()[0];
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMonitorController() {
    	new MetricNode(model, null, "metric");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullMetricName() {
    	try {
			new MetricNode(model, Remmos.getMonitorController(composite), null);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			Assert.fail("shouldn't happens..");
		}
    }

    @Test(expected = NoSuchElementException.class)
    public void readInvalidProperty() {
        node.getProperty("invalid");
    }

    @Test(expected = NoSuchElementException.class)
    public void writeInvalidProperty() {
        node.setProperty("invalid", null);
    }

    @Test
    public void checkMetricFunctions() {
    	// STATE
    	assert(Metric.DISABLED.equals(node.getProperty("state")));
        node.setProperty("state", Metric.ENABLED);
        assert(Metric.ENABLED.equals(node.getProperty("state")));
        node.setProperty("state", "FAIL");
        assert(Metric.ENABLED.equals(node.getProperty("state")));

        // VALUES
        assert((double) node.getProperty("value") == 0.0);
        assert((double) node.getProperty("calculate") == 0.0);
        
        try {
			Remmos.getMonitorController(composite).startGCMMonitoring();
			GCM.getLifeCycleController(composite).startFc();
		
			Master master = (Master) composite.getFcInterface("test-itf");
			int counter = 0;
			System.out.println("Executing some tasks...");
			while (counter < 3) {
				System.out.println("run1: " + master.run());
				System.out.println("run2: " + master.run2());
				counter++;
			}

		} catch (NoSuchInterfaceException | IllegalLifeCycleException e) {
			e.printStackTrace();
			Assert.fail();
		}

        assert((double) node.getProperty("value") > 0.0);
        assert((double) node.getProperty("calculate") > 0.0);
        
    }
}
