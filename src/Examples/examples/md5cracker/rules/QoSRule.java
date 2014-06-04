package examples.md5cracker.rules;


import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;

import examples.md5cracker.metrics.GlobalSPMMetric;



public class QoSRule extends Rule {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "qos-rule";

	private double minSPM;

	public QoSRule(double minSPM) {
		this.minSPM = minSPM;
		this.subscribeToMetric(GlobalSPMMetric.DEFAULT_NAME);
	}
	
	@Override
	public boolean isSatisfied(MonitorController monitor) {
		try {
			// Do not recalculate the metric value. Since this rule is subscribed to the metric, a recalculation
			// would call this method again, falling in a infinite loop.
			if (minSPM > (Double) monitor.getMetricValue(GlobalSPMMetric.DEFAULT_NAME).getValue()) {
				return false;
			}
		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean shouldPrintAlarm() {
		return false;
	}

	@Override
	public String getAlarm() {
		return "[ANALYSIS_CONTROLLER][CRACKER_SPM_RULE][Alarm!] Value below ";
	}

}
