package examples.md5cracker.metrics;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;

public class NumberOfWorkers extends Metric<Integer> {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "number-of-workers-metric";

	private int value;

	public NumberOfWorkers(int initValue) {
		value = initValue;
	}
	@Override
	public Integer calculate() {
		return value;
	}
	@Override
	public Integer getValue() {
		return value;
	}
	@Override
	public void setValue(Integer value) {
		this.value = value;
	}

}
