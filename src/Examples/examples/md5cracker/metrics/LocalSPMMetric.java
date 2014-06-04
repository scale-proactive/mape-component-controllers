package examples.md5cracker.metrics;

import java.util.List;
import java.util.PriorityQueue;


import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.OutgoingRequestRecord;

import examples.md5cracker.cracker.solver.ResultRepository;



public class LocalSPMMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NAME = "solver-spm-metric";

	private double spm;
	private final int BUFF_SIZE = 10;
	
	public LocalSPMMetric() {
	}

	@Override
	public Double calculate() {
		Condition<OutgoingRequestRecord> resultRepoCond = new Condition<OutgoingRequestRecord>(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean evaluate(OutgoingRequestRecord orr) {
				return orr.getInterfaceName().equals(ResultRepository.ITF_NAME);
			}
		};
		List<OutgoingRequestRecord> recordList = recordStore.getOutgoingRequestRecords(resultRepoCond);
		if (recordList == null || recordList.size() <= 1) {
			return (spm = 0);
		}

		// The records are sorted by id, and I need them sorted by sent time, so I use MyPriorityQueue
		MyLimitedPriorityQueue queue = new MyLimitedPriorityQueue(BUFF_SIZE + 1);
		for (OutgoingRequestRecord rec : recordList) {
			queue.add(rec.getSentTime());
		}
		
		double avg = 0;
		long lastTime = queue.remove();
		int data_size = queue.size();
		while ( !queue.isEmpty() ) {
			long time = queue.remove();      
			avg += time - lastTime;
			lastTime = time;
		}
		avg = avg/data_size;
		spm = 60/(avg/1000000);
		return spm;
	}

	@Override
	public Double getValue() {
		return spm;
	}

	@Override
	public void setValue(Double value) {
		this.spm = value;
	}

	// We need the higher times. We add them to the priority queue, if the queue is full,
	// remove the times on the top, this means, the lower times.
	private class MyLimitedPriorityQueue extends PriorityQueue<Long> {
		private static final long serialVersionUID = 1L;
		private int MAX_SIZE;
		
		public MyLimitedPriorityQueue(int maxSize) {
			super(maxSize + 1);
			MAX_SIZE = maxSize;
		}

		@Override
		public boolean add(Long time) {
			boolean v = super.add(time);
			while (this.size() > MAX_SIZE) {
				super.remove(); // remove the lower time
			}
			return v;
		}
	}

}
