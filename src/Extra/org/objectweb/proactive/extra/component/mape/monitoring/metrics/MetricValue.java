package org.objectweb.proactive.extra.component.mape.monitoring.metrics;

import java.io.Serializable;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;


public interface MetricValue extends Serializable {

	public Object getValue() throws WrongMetricValueException;

	public boolean isValid();
	public boolean isMulticast();

}
