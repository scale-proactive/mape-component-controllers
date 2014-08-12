package cl.niclabs.autonomic.examples.balancer.plans;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.planning.Plan;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class UpdatePointsPlan extends Plan {

	private static final long serialVersionUID = 1L;

	@Override
	public void planActionFor(String ruleName, Alarm alarm, MonitorController monitor, ExecutorController executor) {
		
		if (alarm.equals(Alarm.VIOLATION)) {
			Wrapper<String> p = monitor.getMetricValue("points");
			if (p.isValid()) {
				executor.execute("set-value($this/child::Balancer/attribute::points, \"" + p.getValue() + "\");");
			} else {
				System.out.println("monitor.getMetricValue(\"points\") invalid ???");
			}
		}
	}

}
