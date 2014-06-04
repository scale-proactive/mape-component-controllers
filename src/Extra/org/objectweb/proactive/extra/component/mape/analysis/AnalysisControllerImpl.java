package org.objectweb.proactive.extra.component.mape.analysis;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.analysis.AnalysisController;
import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.reconfiguration.ExecutionController;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricEventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;


public class AnalysisControllerImpl extends AbstractPAComponentController
		implements AnalysisController, MetricEventListener, BindingController {

	private static final long serialVersionUID = 1L;

	private ExecutionController executionController;
	private MonitorController monitorController;
	
	// rule name --> rule
	private Map<String, Rule> rules = new HashMap<String, Rule>();
	
	// rule name --> action name
	private Map<String, String> actions = new HashMap<String, String>();


	@Override
	public void addRule(String name, Rule rule, String actionName) {
		rules.put(name, rule);
		actions.put(name, actionName);
	}
	
	@Override
	public void addRule(String name, Rule rule) {
		this.addRule(name, rule, null);
	}

	@Override
	public void removeRule(String name) {
		rules.remove(name);
		actions.remove(name);
	}

	@Override
	public Boolean checkRule(String ruleName) {
		if (rules.containsKey(ruleName)) {
			return rules.get(ruleName).isSatisfied(monitorController);
		}
		return false;
	}

	private void checkRuleAndTriggerAction(String rule) {
		if (rules.get(rule).isSatisfied(monitorController)) {
			return;
		}
		if (actions.containsKey(rule)) {
			executionController.executeAction(actions.get(rule));
		}
	}

	// METRIC EVENT LISTENER
	
	@Override
	public void notifyMetricUpdate(String metricName) {
		for (Map.Entry<String, Rule> entry : rules.entrySet()) {
			if (entry.getValue().isSubscribedToMetric(metricName)) {
				checkRuleAndTriggerAction(entry.getKey());
			}
		}
	}

	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(ExecutionController.ITF_NAME)) {
			executionController = (ExecutionController) itf;
		} else if (name.equals(MonitorController.ITF_NAME)) {
			monitorController = (MonitorController) itf;
		} else {
			throw new NoSuchInterfaceException("AnalysisController:" + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] { MonitorController.ITF_NAME, ExecutionController.ITF_NAME };
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if(name.equals(MonitorController.ITF_NAME)) {
			return monitorController;
		} else if (name.equals(ExecutionController.ITF_NAME)) {
			return executionController;
		}
		throw new NoSuchInterfaceException("AnalysisController:" + name);
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if(name.equals(MonitorController.ITF_NAME)) {
			monitorController = null;
		} else if (name.equals(ExecutionController.ITF_NAME)) {
			executionController = null;
		} else {
			throw new NoSuchInterfaceException("AnalysisController:" + name);
		}
	}

}
