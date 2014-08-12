package cl.niclabs.autonomic.examples.balancer.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class TimesMetric extends Metric<String> {

	private static final long serialVersionUID = 1L;

	public TimesMetric() {
		subscribeTo(RemmosEventType.OUTGOING_REQUEST_EVENT);
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

		double n = 1000000.0;
		System.out.printf("Times: %.3f (%.3f, %.3f, %.3f)", w.getValue()/n, w1.getValue()/n, w2.getValue()/n, w3.getValue()/n);

		return "ok";
	}

	@Override
	public String getValue() {
		return "ok";
	}

	@Override
	public void setValue(String value) { }

}
