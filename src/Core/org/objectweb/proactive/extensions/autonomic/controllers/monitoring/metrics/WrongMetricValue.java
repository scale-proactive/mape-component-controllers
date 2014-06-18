package org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.WrongMetricValueException;


public class WrongMetricValue extends MetricValue {

	private static final long serialVersionUID = 1L;
	private Throwable e;
	private String msg;

	public WrongMetricValue(String message) {
		msg = message;
	}
	
	public WrongMetricValue(Throwable exception) {
		e = exception;
	}
	
	public WrongMetricValue(String message, Throwable exception) {
		msg = message;
		e = exception;
	}

	public Object getValue() throws WrongMetricValueException {
		if (e != null && msg != null) {
			throw new WrongMetricValueException(msg, e);
		}
		else if (e != null) {
			throw new WrongMetricValueException(e);
		}
		else if (msg != null) {
			throw new WrongMetricValueException(msg);
		}
		throw new WrongMetricValueException("Unknown");
	}

	public boolean isValid() {
		return false;
	}

	public boolean isMulticast() {
		return false;
	}

}
