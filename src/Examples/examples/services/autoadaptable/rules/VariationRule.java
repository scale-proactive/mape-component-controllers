package examples.services.autoadaptable.rules;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;

import examples.services.autoadaptable.AASCST;

public class VariationRule extends Rule {

	private static final long serialVersionUID = 1L;

	public VariationRule() {
		this.subscribeToMetric(AASCST.OPTIMAL_POINTS_METRIC);
	}

	@Override
	public Alarm check(MonitorController monitor) {
		return Alarm.VIOLATION;
	}

}
