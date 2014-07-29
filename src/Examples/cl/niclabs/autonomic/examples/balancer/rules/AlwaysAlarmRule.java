package cl.niclabs.autonomic.examples.balancer.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;

public class AlwaysAlarmRule extends Rule {

	private static final long serialVersionUID = 1L;

	public AlwaysAlarmRule() {
		this.subscribeToMetric("points");
	}

	@Override
	public Alarm check(MonitorController monitor) {
		return Alarm.VIOLATION;
	}

}
