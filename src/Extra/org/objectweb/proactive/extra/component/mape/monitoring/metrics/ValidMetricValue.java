package org.objectweb.proactive.extra.component.mape.monitoring.metrics;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;


public class ValidMetricValue implements MetricValue {

	private static final long serialVersionUID = 1L;
	
	private Object result;
	private boolean isMulticast;
	
	
	public ValidMetricValue(Object result, boolean isMulticast) {
		this.result = result;
		this.isMulticast = isMulticast;
	}

	@Override
	public Object getValue() throws WrongMetricValueException {
		return result;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isMulticast() {
		return isMulticast;
	}

}
