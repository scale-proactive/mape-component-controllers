package records;

import java.util.List;

import org.objectweb.proactive.extra.component.mape.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;

public class TestMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	private Double value;


	Condition<IncomingRequestRecord> cond;

	public TestMetric() {
		this.subscribeTo(RemmosEventType.INCOMING_REQUEST_EVENT);
		cond = new Condition<IncomingRequestRecord>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean evaluate(IncomingRequestRecord irr) {
				return irr.isFinished();
			}
		};
	}

	public Double calculate() {

		List<IncomingRequestRecord> recordList = null;
		recordList = recordStore.getIncomingRequestRecords(10, cond);
		// and calculates the average
		double sum = 0.0;
		double nRecords = recordList.size();
		for(IncomingRequestRecord irr : recordList) {
			if(irr.isFinished()) {
				sum += (double)(irr.getReplyTime() - irr.getArrivalTime());
			}
		}
		value = nRecords > 0 ? sum/nRecords: 0;
		return value;
	}

	@Override
	public Double getValue() {
		return this.value;
	}

	@Override
	public void setValue(Double value) {
		this.value = value;
	}

}
