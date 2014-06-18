package examples.md5cracker.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.WrongMetricValueException;

import examples.md5cracker.metrics.CrackerMetric;


public class MinPerformanceRule extends Rule {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "min-performance-rule";

	private double minSPM;


	public MinPerformanceRule(double minSPM) {
		this.minSPM = minSPM;
		this.subscribeToMetric(CrackerMetric.DEFAULT_NAME);
	}
	
	@Override
	public Alarm check(MonitorController monitor) {
		try {
			// WARN: Do not recalculate the metric value. Since this rule is subscribed to the metric,
			// a recalculation would call this method again, falling in a infinite loop.
			if (minSPM > (Double) monitor.getMetricValue(CrackerMetric.DEFAULT_NAME).getValue()) {
				return Alarm.VIOLATION;
			}

		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
		return null;
	}

}
