package tests;

import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.FScript;
import org.objectweb.fractal.fscript.FScriptEngine;
import org.objectweb.fractal.fscript.FScriptException;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.AGCMModel;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.MetricNode;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.RuleNode;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

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

    @Test(expected = IllegalArgumentException.class)
    public void setInvalidSubscription() {
        node.setSubscription(0.0);
    }

    @Test
    public void checkRuleFunctions() {
    	assert("foo".equals(node.getProperty("name")));
    	assert(Alarm.WARNING.toString().equals(node.getProperty("check")));
    	
    	assert(node.getSubscriptions().size() == 0);
    	node.setSubscription("avgInc");
    	assert(node.getSubscriptions().size() == 1);

    	try {
    		@SuppressWarnings("rawtypes")
			MetricNode mnode = (MetricNode) ((Set) engine.execute("$this/metric::avgOut;")).toArray()[0];
			 node.setSubscription(mnode);
		    assert(node.getSubscriptions().size() == 2);
		} catch (FScriptException e) {
			e.printStackTrace();
			Assert.fail();
		}
    }
    
    @SuppressWarnings("rawtypes")
	@Test
    public void checkAddRule() {
    	try {
			assert(((Set) engine.execute("$this/rule::foo2;")).size() == 0);
			engine.execute("new-rule($this, \"foo2\", \"tests.rules.FooRule\");");
			assert(((Set) engine.execute("$this/rule::foo2;")).size() == 1);
		} catch (FScriptException e) {
			e.printStackTrace();
			Assert.fail();
		}
    }
}
