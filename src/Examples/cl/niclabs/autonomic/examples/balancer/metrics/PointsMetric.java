package cl.niclabs.autonomic.examples.balancer.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class PointsMetric extends Metric<String> {

	private static final long serialVersionUID = 1L;
	private double p1, p2;
	
	private int counter;

	public PointsMetric() {
		p1 = 0.334;
		p2 = 0.667;
		subscribeTo(RemmosEventType.OUTGOING_REQUEST_EVENT);
		
		counter = 0;
	}

	public PointsMetric(String points) {
		this();
		String[] split = points.split("u");
		if (split.length == 2) {
			double np1 = Double.parseDouble(split[0]);
			double np2 = Double.parseDouble(split[1]);
			if (np1 <= 1 && np2 <= 1 && np1 <= np2) {
				p1 = np1;
				p2 = np2;
			}
		}
	}

	@Override
	public String calculate() {
		
		counter = (counter + 1) % 1;
		if (counter != 0) return p1 + "u" + p2;

		Wrapper<Double> w1 = this.metricStore.calculate("avgInc", "/cracker/solver-1");
		Wrapper<Double> w2 = this.metricStore.calculate("avgInc", "/cracker/solver-2");
		Wrapper<Double> w3 = this.metricStore.calculate("avgInc", "/cracker/solver-3");
		
		if ( !(w1.isValid() && w2.isValid() && w3.isValid()) ) {
			System.err.println("[WARNING] PointsMetric fail when trying to get solvers avgInc values");
		}

		double c1 = p1 / w1.getValue();
		double c2 = (p2 - p1) / w2.getValue();
		double c3 = 0.0;
		try {
			c3 = (1 - p2) / w3.getValue();
		} catch (ClassCastException e) {
			System.out.println("-------------__> " + w3.getValue());
			return "FAIL";
		}
		double alpha = 1.0 / (c1 + c2 + c3);

		double np1 = alpha * c1;
		double np2 = alpha * (c1 + c2);

		double n = 1000000.0;
		System.out.printf("Old Points (%.3f, %.3f) -- ", p1, p2);
		System.out.printf("Times (%.3f, %.3f, %.3f) -- ", w1.getValue()/n, w2.getValue()/n, w3.getValue()/n);
		System.out.printf("New Points (%.3f, %.3f)\n", np1, np2);
		
		//p1 = np1;
		//p2 = np2;
		
		return p1 + "u" + p2;
	}

	@Override
	public String getValue() {
		return p1 + "u" + p2;
	}

	@Override
	public void setValue(String value) {
		String[] split = value.split("u");
		if (split.length == 2) {
			double np1 = Double.parseDouble(split[0]);
			double np2 = Double.parseDouble(split[1]);
			if (np1 <= 1 && np2 <= 1 && np1 <= np2) {
				p1 = np1;
				p2 = np2;
			}
		}
	}

}
