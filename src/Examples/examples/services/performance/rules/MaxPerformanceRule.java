package examples.services.performance.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;

import examples.services.performance.metrics.CrackerMetric;


public class MaxPerformanceRule extends Rule {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "max-performance-rule";

	private double maxSPM;

	public MaxPerformanceRule(double maxSPM) {
		this.maxSPM = maxSPM;
		this.subscribeToMetric(CrackerMetric.DEFAULT_NAME);
	}

	@Override
	public Alarm check(MonitorController monitor) {
		// WARN: Do not recalculate the metric value. Since this rule is subscribed to the metric,
		// a recalculation would call this method again, falling in a infinite loop.
		double value = (Double) monitor.getMetricValue(CrackerMetric.DEFAULT_NAME).getValue();
		if (maxSPM < value) {
			return Alarm.VIOLATION;
		}
		return null;
	}

}
