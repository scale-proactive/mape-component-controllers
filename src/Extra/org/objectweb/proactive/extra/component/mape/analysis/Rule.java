package org.objectweb.proactive.extra.component.mape.analysis;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;


public abstract class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<String> subscribedMetrics = new HashSet<String>();

	protected boolean subscribeToMetric(String metricName) {
		return subscribedMetrics.add(metricName);
	}

	protected boolean unsubscribeFromMetric(String metricName) {
		return subscribedMetrics.remove(metricName);
	}

	protected boolean isSubscribedToMetric(String metricName) {
		return subscribedMetrics.contains(metricName);
	}

	public abstract boolean isSatisfied(MonitorController monitor);

	public abstract boolean shouldPrintAlarm();
	public abstract String getAlarm();

}
