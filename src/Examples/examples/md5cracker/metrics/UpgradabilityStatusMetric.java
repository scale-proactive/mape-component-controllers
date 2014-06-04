package examples.md5cracker.metrics;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;

public class UpgradabilityStatusMetric extends Metric<Boolean> {
	private static final long serialVersionUID = 1L;
	private boolean upgradable;
	
	public static final String DEFAULT_NAME = "upgradability-metric";
	
	public UpgradabilityStatusMetric(boolean initValue) {
		upgradable = initValue;
	}

	@Override
	public Boolean calculate() {
		return upgradable;
	}

	@Override
	public Boolean getValue() {
		return upgradable;
	}

	@Override
	public void setValue(Boolean value) {
		upgradable = value;
	}

}
