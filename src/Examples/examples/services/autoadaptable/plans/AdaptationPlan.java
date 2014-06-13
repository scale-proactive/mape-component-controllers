package examples.services.autoadaptable.plans;

import org.objectweb.proactive.extra.component.mape.analysis.Alarm;
import org.objectweb.proactive.extra.component.mape.execution.ExecutorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValueException;
import org.objectweb.proactive.extra.component.mape.planning.Plan;

import examples.services.autoadaptable.AASCST;

public class AdaptationPlan extends Plan {

	private static final long serialVersionUID = 1L;

	@Override
	public void planActionFor(String ruleName, Alarm alarm, MonitorController monitor, ExecutorController executor) {
		
		if ( !ruleName.equals(AASCST.VARIATION_RULE) || alarm != Alarm.VIOLATION )
			return;

		MetricValue mv1, mv2, mv3;
		mv1 = monitor.calculateMetric(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SOLVER + "/" + AASCST.SOLVER_C1);
		mv2 = monitor.calculateMetric(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SOLVER + "/" + AASCST.SOLVER_C2);
		mv3 = monitor.calculateMetric(AASCST.RESPONSE_TIME_METRIC, "/" + AASCST.SOLVER + "/" + AASCST.SOLVER_C3);

		if ( !mv1.isValid() || !mv2.isValid() || !mv3.isValid() ) {
			System.out.println("[ADAPTION PLAN WARINING] One or more metric values are corrupted...");
			return;
		}
		
		try {
			double v1, v2, v3;

			Object obj = mv1.getValue();
			if (obj != null) {
				v1 = (double) obj;
			} else {
				System.out.println("[ADAPTION PLAN WARNING] the metric value from solver 1 is null...");
				return;
			}
			
			obj = mv2.getValue();
			if (obj != null) {
				v2 = (double) obj;
			} else {
				System.out.println("[ADAPTION PLAN WARINING] the metric value from solver 2 is null...");
				return;
			}
			
			obj = mv3.getValue();
			if (obj != null) {
				v3 = (double) obj;
			} else {
				System.out.println("[ADAPTION PLAN WARINING] the metric value from solver 3 is null...");
				return;
			}
		
			double x = 1 / (v1 + v2 + v3);
			
			double p1 = x * v1;
			double p2 = x * (v1 + v2);
			
			String points = p1 + "u" + p2;
		
			executor.execute("set-value($this/child::" + AASCST.MANAGER_COMP_NAME
					+ "/attributes::points, \"" + points + "\");");

		} catch (WrongMetricValueException e) {
			e.printStackTrace();
		}
	}

}
