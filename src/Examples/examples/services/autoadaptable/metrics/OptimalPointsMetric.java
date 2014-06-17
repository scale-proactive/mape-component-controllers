package examples.services.autoadaptable.metrics;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;

import examples.services.autoadaptable.AASCST;

public class OptimalPointsMetric extends Metric<String> {

	private static final long serialVersionUID = 1L;
	private String points = 1.0/3 + "u" + 2.0/3;

	@Override
	public String calculate() {
		MetricValue mv1, mv2, mv3;
		mv1 = this.metricStore.calculate(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SERVICE + "/" + AASCST.SOLVER_C1);
		mv2 = this.metricStore.calculate(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SERVICE + "/" + AASCST.SOLVER_C2);
		mv3 = this.metricStore.calculate(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SERVICE + "/" + AASCST.SOLVER_C3);
		
		try {
			double v1, v2, v3;

			Object obj = mv1.getValue();
			if (obj != null) {
				v1 = (double) obj;
			} else {
				return warning();
			}
			
			obj = mv2.getValue();
			if (obj != null) {
				v2 = (double) obj;
			} else {
				return warning();
			}
			
			obj = mv3.getValue();
			if (obj != null) {
				v3 = (double) obj;
			} else {
				return warning();
			}

			double t = (v1 + v2 + v3);
			double x1 = t/v1, x2 = t/v2, x3 = t/v3;
			
			t  = 1 / (x1 + x2 + x3);
			
			double p1 = t * x1;
			double p2 = t * (x1 + x2);
			
			points = p1 + "u" + p2;
			return points;

		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
		return points;
	}

	@Override
	public String getValue() {
		return points;
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}

	private String warning() {
		System.out.println("[SOLVER RT VARIATION METRICWARINING] One or more metric values are corrupted...");
		return points;
	}
}
