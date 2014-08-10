package tests;

import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.fscript.FScript;
import org.objectweb.fractal.fscript.FScriptEngine;
import org.objectweb.fractal.fscript.FScriptException;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.AGCMModel;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.ActionNode;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import tests.actions.FooAction;

public class TestActionNode extends CommonSetup {

	protected FScriptEngine engine;
	protected AGCMModel model;
	protected ActionNode node;


    @SuppressWarnings("rawtypes")
	@Before
    public void setUp() throws Exception {
    	super.setUp();

    	Remmos.getExecutorController(composite).addAction("foo", new FooAction());

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
        
        System.out.println(engine.execute("print-jactions($this);"));
        node = (ActionNode) ((Set) engine.execute("$this/jaction::foo;")).toArray()[0];
        System.out.println("-------------------------- " + node.getName());
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullAnalyzerController() {
    	new ActionNode(model, null, "action");
    }

    @Test(expected = NullPointerException.class)
    public void createWithNullRuleName() {
		new ActionNode(model,composite, null);
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
    	assert("It works!".equals(node.getProperty("execute")));
    }
    
    @SuppressWarnings("rawtypes")
	@Test
    public void checkAddAction() {
    	try {
			assert(((Set) engine.execute("$this/jaction::foo2;")).size() == 0);
			engine.execute("add-jaction($this, \"foo2\", \"tests.actions.FooAction\");");
			assert(((Set) engine.execute("$this/jaction::foo2;")).size() == 1);
			
			engine.execute("remove-jaction($this/jaction::foo2);");
			assert(((Set) engine.execute("$this/jaction::foo2;")).size() == 0);
		} catch (FScriptException e) {
			e.printStackTrace();
			Assert.fail();
		}
    }

}
