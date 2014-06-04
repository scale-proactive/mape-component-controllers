package examples.md5cracker.metrics;

import java.util.List;


import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;

import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.SolverMulticast;


/**
 * Designed to be used on Cracker composite component
 * @author mnip91
 *
 */
public class GlobalSPMMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NAME = "total-spm-metric";

	private double totalSPM;
	private String itfPath;

	public GlobalSPMMetric() {
		totalSPM = 0;
		itfPath = "/" + Cracker.ITF_NAME + "/" + SolverMulticast.ITF_NAME;
	}	

	@Override
	public Double calculate() {
		try {
			MetricValue tmv = this.metricStore.calculate(LocalSPMMetric.DEFAULT_NAME, itfPath);
			if (tmv.isMulticast()) {
				
				totalSPM = 0;
				for (MetricValue mv : (List<MetricValue>) tmv.getValue()) {
					totalSPM += ((Double) mv.getValue()).doubleValue();
				}
				return totalSPM;
			} else {
				return -2.0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return  -1.0;
		}
	}

	@Override
	public Double getValue() {
		return this.totalSPM;
	}

	@Override
	public void setValue(Double value) {
		this.totalSPM = value;
	}

}
