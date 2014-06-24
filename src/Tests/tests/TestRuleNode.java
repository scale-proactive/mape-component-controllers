package tests;

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
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.AGCMModel;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.MetricNode;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.RuleNode;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import tests.components.Master;
import tests.rules.FooRule;

public class TestRuleNode extends CommonSetup {

	protected FScriptEngine engine;
	protected AGCMModel model;
	
	protected RuleNode node;


    @SuppressWarnings("rawtypes")
	@Before
    public void setUp() throws Exception {
    	super.setUp();
    	
    	Remmos.getAnalyzerController(composite).addRule("foo", new FooRule());
  
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
        
        node = (RuleNode) ((Set) engine.execute("$this/rule::foo;")).toArray()[0];
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullAnalyzerController() {
    	new RuleNode(model, null, "rule");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullRuleName() {
    	try {
			new RuleNode(model, Remmos.getAnalyzerController(composite), null);
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
    public void checkRuleFunctions() {
    	assert("foo".equals(node.getProperty("name")));
    	assert(Alarm.WARNING.toString().equals(node.getProperty("check")));
    }
}
