package org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics;

import java.io.Serializable;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.WrongMetricValueException;


public abstract class MetricValue implements Serializable {

	private static final long serialVersionUID = 1L;

	abstract public Object getValue() throws WrongMetricValueException;

	abstract public boolean isValid();
	abstract public boolean isMulticast();

}
