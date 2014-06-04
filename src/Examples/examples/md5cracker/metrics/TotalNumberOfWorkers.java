package examples.md5cracker.metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;

import examples.md5cracker.cracker.Cracker;
import examples.md5cracker.cracker.SolverMulticast;


public class TotalNumberOfWorkers extends Metric<Integer> {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_NAME = "total-number-of-workers";

	String itfPath = "/" + Cracker.ITF_NAME + "/" + SolverMulticast.ITF_NAME;
	

	@Override
	public Integer calculate() {
		try {
			MetricValue tmv = this.metricStore.getValue(NumberOfWorkers.DEFAULT_NAME, itfPath);
			if (tmv.isMulticast()) {

				int total = 0;
				//System.out.println("--------------------------------------");
				for (MetricValue mv : (List<MetricValue>) tmv.getValue()) {
					//System.out.println("---> " + ((Double) mv.getValue()).doubleValue());
					total += (Integer) mv.getValue();
				}
				return total;
			} else {
				return -2;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return  -1;
		}
	}
	@Override
	public Integer getValue() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setValue(Integer value) {
		// TODO Auto-generated method stub
		
	}
	
	

}
