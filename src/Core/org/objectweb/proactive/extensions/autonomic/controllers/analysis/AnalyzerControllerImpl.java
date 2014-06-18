package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MetricEventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;


public class AnalyzerControllerImpl extends AbstractPAComponentController implements AnalyzerController,
		MetricEventListener, BindingController {

	private static final long serialVersionUID = 1L;

	private AlarmListener alarmListener;
	private MonitorController monitor;

	private Map<String, Rule> rules = new HashMap<String, Rule>(); // rule name --> rule

	@Override
	public void addRule(String name, Rule rule) {
		rules.put(name, rule);
	}

	@Override
	public void removeRule(String name) {
		rules.remove(name);
	}

	@Override
	public Alarm checkRule(String ruleName) throws NoSuchElementException {
		if (rules.containsKey(ruleName)) {
			return rules.get(ruleName).check(monitor);
		}
		throw new NoSuchElementException("Could not found rule \"" + ruleName + "\"");
	}

	private void checkRuleAndNotifyAlarm(String ruleName, Rule rule) {
		Alarm alarm = rule.check(monitor);
		if (alarm != null && alarmListener != null) {
			alarmListener.listenAlarm(ruleName, alarm);
		}
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
