package tests;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.fscript.FScript;
import org.objectweb.fractal.fscript.FScriptEngine;
import org.objectweb.fractal.fscript.FScriptException;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.AGCMModel;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.MetricNode;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.RuleNode;
import org.objectweb.proactive.extensions.autonomic.gcmscript.model.SubscriptionAxis;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

import tests.rules.FooRule;

public class TestSubscriptions extends CommonSetup {

	protected FScriptEngine engine;
	protected AGCMModel model;
	
	protected RuleNode node;

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
        
        node = (RuleNode) ((Set<?>) engine.execute("$this/rule::foo;")).toArray()[0];
    }

	@Test
	public void subscription() {
		SubscriptionAxis sa = new SubscriptionAxis(model);
		assert(sa.selectFrom(node).size() == 0);

		MetricNode mnode;
		try {
			mnode = (MetricNode) ((Set<?>) engine.execute("$this/metric::avgOut;")).toArray()[0];
		} catch (FScriptException e) {
			e.printStackTrace();
			fail();
			return;
		}

		sa.connect(node, mnode);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<Node> set = sa.selectFrom(node);
		System.out.println(set.size());
		assert(set.size() == 1);
		assert(set.toArray(new Node[1])[0].getKind().getName().equals("metric"));
	}
}
