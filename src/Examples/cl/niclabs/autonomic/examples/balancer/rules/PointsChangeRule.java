package cl.niclabs.autonomic.examples.balancer.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class PointsChangeRule extends Rule {

	private static final long serialVersionUID = 1L;

	public PointsChangeRule() {
		this.subscribeToMetric("points");
	}

	@Override
	public Alarm check(MonitorController monitor) {
		Wrapper<String> ws = monitor.getMetricValue("points");
		if (ws.isValid()) {
			String s = ws.getValue();
			return s.equals("BUFFERING") ? Alarm.OK : Alarm.VIOLATION;
		}
		System.err.println("WARNING: invalid metric value \"points\": " + ws.getMessage());
		return Alarm.VIOLATION;
	}

}
