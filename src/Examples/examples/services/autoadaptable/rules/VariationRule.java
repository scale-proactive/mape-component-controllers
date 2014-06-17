package examples.services.autoadaptable.rules;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;

import examples.services.autoadaptable.AASCST;

public class VariationRule extends Rule {

	private static final long serialVersionUID = 1L;
	

	public VariationRule(double thresholdRatio) {
		this.subscribeToMetric(AASCST.OPTIMAL_POINTS_METRIC);
	}

	@Override
	public Alarm check(MonitorController monitor) {
		
			System.out.println("IT WORKS");
			return Alarm.OK;

	}

}
