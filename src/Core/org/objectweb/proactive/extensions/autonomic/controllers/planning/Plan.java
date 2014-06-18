package org.objectweb.proactive.extensions.autonomic.controllers.planning;

import java.io.Serializable;

import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;

public abstract class Plan implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract void planActionFor(String ruleName, Alarm alarm, MonitorController monitor,
			ExecutorController executor);

}
