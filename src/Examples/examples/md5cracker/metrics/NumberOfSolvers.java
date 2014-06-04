package examples.md5cracker.metrics;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;

/**
 * Same as NumberOfWorkers, but for Solvers
 * @author mnip91
 *
 */
public class NumberOfSolvers extends Metric<Integer> {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "number-of-solvers-metric";

	private int num;
	
	public NumberOfSolvers(int initialNumberOfWorkers) {
		this.num = initialNumberOfWorkers;
	}

	@Override
	public Integer calculate() {
		return num;
	}

	@Override
	public Integer getValue() {
		return num;
	}

	@Override
	public void setValue(Integer value) {
		num = value;
	}

}
