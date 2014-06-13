package examples.services.autoadaptable.rules;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.analysis.Rule;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;

import examples.services.autoadaptable.AASCST;

public class VariationRule extends Rule {

	private static final long serialVersionUID = 1L;
	
	private double lastValue = -1;
	private double thresholdRatio = Double.MAX_VALUE;

	private double p1 = 1.0/3;
	private double p2 = 2.0/3;

	public VariationRule(double thresholdRatio) {
		this.thresholdRatio = thresholdRatio;
		this.subscribeToMetric(AASCST.OPTIMAL_POINTS_METRIC);
	}

	@Override
	public Alarm check(MonitorController monitor) {
		
		MetricValue ow = monitor.getMetricValue(AASCST.OPTIMAL_POINTS_METRIC);
		
		if (!ow.isValid()) {
			return Alarm.FAIL;
		}

		try {
			Object value = ow.getValue();

			if (value == null) {
				return Alarm.WARNING;
			}
			String[] split = ((String) value).split("u");
			if (split.length != 2) {
				System.out.println ("FAIL ALARMA--------------------------------------------------------");
				return Alarm.FAIL;
			}
			
			double np1 = Double.parseDouble(split[0]);
			double np2 = Double.parseDouble(split[1]);
			
			boolean change = false;
			if (Math.abs(1 - (np1/p1)) > thresholdRatio || Math.abs(1 - (np2/p2)) > thresholdRatio) {
				System.out.println ("---------------------------------------------------- NEW VALUES !! ALARM SENT----");
				p1 = np1;
				p2 = np2;
			}
			
			if (change) {
				return Alarm.VIOLATION;
			}
			
			return Alarm.OK;

		} catch (WrongMetricValueException e) {
			e.printStackTrace();
			return Alarm.FAIL;
		}
	}

}
