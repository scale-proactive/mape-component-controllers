package org.objectweb.proactive.extra.component.mape.monitoring;

/**
 * Used to notify changes on the metrics value
 * 
 * @author mnip91
 *
 */
public interface MetricEventListener {

	public static final String ITF_NAME = "metric-event-listener-nf";

	/**
	 * Notify a change in the value of a metric;
	 * @param metricName name 
	 */
	public void notifyMetricUpdate(String metricName);

}
