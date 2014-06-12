package examples.md5cracker.rules;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;

import examples.md5cracker.metrics.CrackerMetric;


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
		try {
			
			// WARN: Do not recalculate the metric value. Since this rule is subscribed to the metric,
			// a recalculation would call this method again, falling in a infinite loop.
			double value = (double) monitor.getMetricValue(CrackerMetric.DEFAULT_NAME).getValue();
			if (maxSPM < value) {
				return Alarm.VIOLATION;
			}

		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
		return null;
	}

}
