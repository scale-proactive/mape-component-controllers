package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MetricEventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;


public class AnalyzerControllerImpl extends AbstractPAComponentController implements AnalyzerController,
		MetricEventListener, BindingController {

	private static final long serialVersionUID = 1L;

	private AlarmListener alarmListener;
	private MonitorController monitor;

	private Map<String, Rule> rules = new HashMap<String, Rule>(); // rule name --> rule

	@Override
	public Wrapper<Boolean> addRule(String name, Rule rule) {
		if (rules.containsKey(name)) {
			
		}
		rules.put(name, rule);
		return new ValidWrapper<Boolean>(true);
	}

	@Override
	public Wrapper<Boolean> removeRule(String name) {
		if (rules.remove(name) == null) {
			return new ValidWrapper<Boolean>(false, "A rule with name " + name + " already exist.");
		}
		return new ValidWrapper<Boolean>(true);
	}

	@Override
	public Wrapper<Alarm> checkRule(String ruleName) {
		if (rules.containsKey(ruleName)) {
			return new ValidWrapper<Alarm>(rules.get(ruleName).check(monitor));
		}
		return new WrongWrapper<Alarm>("No rule was found with name \"" + ruleName + "\"");
	}

	@Override
	public Wrapper<HashSet<String>> getRulesNames() {
		return new ValidWrapper<HashSet<String>>(new HashSet<String>(rules.keySet()));
	}

	@Override
	public Wrapper<HashSet<String>> getRuleSubscriptions(String ruleName) {
		if (rules.containsKey(ruleName)) {
			return new ValidWrapper<HashSet<String>>(rules.get(ruleName).getSubscriptions());
		}
		return new WrongWrapper<HashSet<String>>("No rule was found with name \"" + ruleName + "\"");
	}

	@Override
	public Wrapper<Boolean> subscribeRuleTo(String ruleName, String metricName) {
		if (rules.containsKey(ruleName)) {
			boolean subscription = rules.get(ruleName).subscribeToMetric(metricName);
			if (subscription) {
				return new ValidWrapper<Boolean>(true);
			}
			return new ValidWrapper<Boolean>(false, "The subscription failed inside the rule.");
		}
		return new WrongWrapper<Boolean>("No rule was found with name \"" + ruleName + "\"");
	}


	// METRIC EVENT LISTENER

	@Override
	public void notifyMetricUpdate(String metricName) {
		for (Map.Entry<String, Rule> entry : rules.entrySet()) {
			if (entry.getValue().isSubscribedToMetric(metricName)) {
				checkRuleAndNotifyAlarm(entry.getKey(), entry.getValue());
			}
		}
	}

	private void checkRuleAndNotifyAlarm(String ruleName, Rule rule) {
		Alarm alarm = rule.check(monitor);
		if (alarm != null && alarmListener != null) {
			alarmListener.listenAlarm(ruleName, alarm);
		}
	}


	// BINDING CONTROLLER

	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if (name.equals(MonitorController.ITF_NAME)) {
			monitor = (MonitorController) itf;
		} else if (name.equals(AlarmListener.ITF_NAME)) {
			alarmListener = (AlarmListener) itf;
		} else {
			throw new NoSuchInterfaceException("[@AnalyzerController] " + name);
		}
	}

	@Override
	public String[] listFc() {
		return new String[] {
				MonitorController.ITF_NAME,
				AlarmListener.ITF_NAME
			};
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if(name.equals(MonitorController.ITF_NAME)) {
			return monitor;
		} else if (name.equals(AlarmListener.ITF_NAME)) {
			return alarmListener;
		}
		throw new NoSuchInterfaceException("[@AnalyzerController] " + name);
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if(name.equals(MonitorController.ITF_NAME)) {
			monitor = null;
		} else if (name.equals(AlarmListener.ITF_NAME)) {
			alarmListener = null;
		} else {
			throw new NoSuchInterfaceException("[@AnalyzerController] " + name);
		}
	}

}
