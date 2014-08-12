package cl.niclabs.autonomic.examples.balancer.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class PointsMetric extends Metric<String> {

	private static final long serialVersionUID = 1L;
	private String state; // POINTS or "BUFFERING"
	private double p1, p2;
	private int buffer;
	private long counter;

	public PointsMetric() {
		state = "OK";
		p1 = 0.334;
		p2 = 0.667;
		counter = 0;
		buffer = 1;
		subscribeTo(RemmosEventType.OUTGOING_REQUEST_EVENT);
	}

	public PointsMetric(int buffer) {
		this();
		this.buffer = buffer;
	}

	@Override
	public String calculate() {

		Wrapper<Double> w = this.metricStore.calculate("avgOut");
		Wrapper<Double> w1 = this.metricStore.calculate("avgInc", "/cracker/solver-1");
		Wrapper<Double> w2 = this.metricStore.calculate("avgInc", "/cracker/solver-2");
		Wrapper<Double> w3 = this.metricStore.calculate("avgInc", "/cracker/solver-3");
		
		if ( !(w.isValid()) && !(w1.isValid() && w2.isValid() && w3.isValid()) ) {
			System.err.println("[WARNING] PointsMetric fail when trying to get cracker avgOut or solvers avgInc values");
		}

		counter++;

		if ((counter % buffer) == 0) {
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
	
			p1 = alpha * c1;
			p2 = alpha * (c1 + c2);
		}

		double n = 1000000.0;
		System.out.printf("%d\t%.3f\t%.3f\t%.3f\t%.3f", counter, w.getValue()/n, w1.getValue()/n, w2.getValue()/n, w3.getValue()/n);
		System.out.printf("\t%.3f\t%.3f\n", p1, p2);

		state = p1 + "u" + p2;
		return state;
	}

	@Override
	public String getValue() {
		return state;
	}

	@Override
	public void setValue(String value) {
		state = value;
	}

}
