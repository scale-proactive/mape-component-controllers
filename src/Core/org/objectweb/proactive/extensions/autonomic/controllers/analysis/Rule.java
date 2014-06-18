package org.objectweb.proactive.extensions.autonomic.controllers.analysis;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;


public abstract class Rule implements Serializable {

	private static final long serialVersionUID = 1L;

	// Set with the name of the metrics
	private Set<String> subscribedMetrics = new HashSet<String>();

	
	/**
	 * It will check this rule state using the monitor controller to get extra
	 * information, and will return the proper alarm to describe this rule state.
	 * <br><br>
	 * If a PlannerController exist, the alarm will be sent to the PlannerController. In the case of null alarm values,
	 * it will be assumed that no alarm was throw, therefore, the PlannerController will not be notified.
	 * 
	 * @param monitor
	 * @return
	 */
	public abstract Alarm check(MonitorController monitor);


	protected boolean subscribeToMetric(String metricName) {
		return subscribedMetrics.add(metricName);
	}

	protected boolean unsubscribeFromMetric(String metricName) {
		return subscribedMetrics.remove(metricName);
	}

	protected boolean isSubscribedToMetric(String metricName) {
		return subscribedMetrics.contains(metricName);
	}

}
