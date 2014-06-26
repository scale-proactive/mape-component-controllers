package tests.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;

public class FooMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	double value = 0.0;

	@Override
	public Double calculate() {
		return ++value;
	}

	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public void setValue(Double value) {
		this.value = value;
	}

}
