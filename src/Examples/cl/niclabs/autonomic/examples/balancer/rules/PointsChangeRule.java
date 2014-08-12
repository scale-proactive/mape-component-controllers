package cl.niclabs.autonomic.examples.balancer.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class PointsChangeRule extends Rule {

	private static final long serialVersionUID = 1L;

	private double p1, p2;

	public PointsChangeRule() {
		p1 = 0.334;
		p2 = 0.667;
		this.subscribeToMetric("points");
	}

	@Override
	public Alarm check(MonitorController monitor) {
		Wrapper<String> ws = monitor.getMetricValue("points");
		if (ws.isValid()) {
			String[] split = ws.getValue().split("u");
			if (split.length == 2) {
				double np1 = Double.parseDouble(split[0]);
				double np2 = Double.parseDouble(split[1]);
				if (np1 != p1 || np2 != p2) {
					p1 = np1;
					p2 = np2;
					return Alarm.VIOLATION;
				}
				return Alarm.OK;
			}
			System.err.println("WARNING: invalid metric value \"points\": " + ws.getValue());
			return Alarm.FAIL;
		}
		System.err.println("WARNING: invalid metric value \"points\": " + ws.getMessage());
		return Alarm.FAIL;
	}

}
