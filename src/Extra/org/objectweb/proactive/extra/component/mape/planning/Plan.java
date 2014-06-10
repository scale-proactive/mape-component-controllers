package org.objectweb.proactive.extra.component.mape.planning;

import java.io.Serializable;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;

public abstract class Plan implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract void planActionFor(String ruleName, Alarm alarm, MonitorController monitor,
			ExecutorController executor);

}
