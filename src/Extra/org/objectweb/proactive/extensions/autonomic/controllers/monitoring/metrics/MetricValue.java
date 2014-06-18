package org.objectweb.proactive.extra.component.mape.monitoring.metrics;

import java.io.Serializable;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;


public abstract class MetricValue implements Serializable {

	private static final long serialVersionUID = 1L;

	abstract public Object getValue() throws WrongMetricValueException;

	abstract public boolean isValid();
	abstract public boolean isMulticast();

}
