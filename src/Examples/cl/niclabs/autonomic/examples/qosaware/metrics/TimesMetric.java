package cl.niclabs.autonomic.examples.qosaware.metrics;

import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class TimesMetric extends Metric<String> {

	private static final long serialVersionUID = 1L;
	private long counter;
	private double p1, p2;

	public TimesMetric() {
		p1 = 0.334;
		p2 = 0.667;
		counter = 0;
		subscribeTo(RemmosEventType.OUTGOING_REQUEST_EVENT);
	}

	@Override
	public String calculate() {

		Wrapper<Double> w = this.metricStore.calculate("avgOut");
		Wrapper<Double> w1 = this.metricStore.calculate("avgInc", "/clientReq/httpReq");
		Wrapper<Double> w2 = this.metricStore.calculate("avgInc", "/clientReq/httpsReq");
		Wrapper<Double> w3 = this.metricStore.calculate("avgInc", "/clientReq/httpReq/jeeReq");
		Wrapper<Double> w4 = this.metricStore.calculate("avgInc", "/clientReq/httpsReq/jeeReq");
		//Wrapper<Double> w2 = this.metricStore.calculate("avgInc", "/springoo/solver-2");
		//Wrapper<Double> w3 = this.metricStore.calculate("avgInc", "/springoo/solver-3");
		
		if ( !(w.isValid()) && !(w1.isValid() && w2.isValid() && w3.isValid() && w4.isValid()) ) {
			System.err.println("[WARNING. Times Metric] PointsMetric fail when trying to get cracker avgOut or solvers avgInc values");
		}

		counter++;

		double n = 1000000.0;
		//System.out.printf("%d\t%.3f\t%.3f\t%.3f\t%.3f", counter, w.getValue()/n, w1.getValue()/n, w2.getValue()/n, w3.getValue()/n);
		System.out.printf("%d\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f", counter, w.getValue()/n, w1.getValue()/n, w2.getValue()/n, w3.getValue()/n, w4.getValue()/n );
		System.out.printf("\tp1=%.3f\tp2=%.3f\n", p1, p2);
		return "ok";
	}

	@Override
	public String getValue() {
		return "ok";
	}

	@Override
	public void setValue(String value) { }

}
