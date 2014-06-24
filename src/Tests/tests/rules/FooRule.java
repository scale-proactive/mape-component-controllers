package tests.rules;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;

public class FooRule extends Rule {

	private static final long serialVersionUID = 1L;

	@Override
	public Alarm check(MonitorController monitor) {
		return Alarm.WARNING;
	}

}
