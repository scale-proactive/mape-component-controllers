package examples.services.autoadaptable.metrics;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;


public class ResponseTimeMetric extends Metric<Double> {

	private static final long serialVersionUID = 1L;

	private long lastOldest = -1;

	private double avg;
	private final int BUFF_SIZE = 10;

	private Condition<IncomingRequestRecord> condition;

	public ResponseTimeMetric(String serverItfName) {
		final String itfName = serverItfName;
		condition = new Condition<IncomingRequestRecord>() {
			private static final long serialVersionUID = 1L;
			public boolean evaluate(IncomingRequestRecord object) {
				return object.getInterfaceName().equals(itfName);
			}
		};

		//this.subscribeTo(RemmosEventType.INCOMING_REQUEST_EVENT);
	}

	@Override
	public Double calculate() {
		
		List<IncomingRequestRecord> recordList = recordStore.getIncomingRequestRecords(condition);
		// The records are sorted by id, and I need them sorted by sent time, so I use MyPriorityQueue
		TimesFilter filter = new TimesFilter(BUFF_SIZE);
		for (IncomingRequestRecord rec : recordList) {
			if (rec.isFinished()) {
				if (rec.getArrivalTime() >= lastOldest) {
					filter.add(rec.getArrivalTime(), rec.getReplyTime() - rec.getArrivalTime());
				}
			}
		}
		lastOldest = filter.getOldest();
	
		if (filter.getSize() == 0) {
			return (avg = 0);
		}
	
		avg = 1.0 * filter.getSum() / filter.getSize();
		return avg;
	}

	@Override
	public Double getValue() {
		return avg;
	}

	@Override
	public void setValue(Double value) {
		this.avg = value;
	}


	private class TimesFilter implements Serializable {

		private static final long serialVersionUID = 1L;
	
		private boolean full;
		private Pair[] pairs;

		
		public TimesFilter(int maxSize) {
			pairs = new Pair[maxSize];
			full = false;
		}

		public void add(long init, long value) {
			if (!full) {
				for (int i = 0; i < pairs.length; i++) {
					if (pairs[i] == null) {
						pairs[i] = new Pair(init, value);
						return;
					}
				}
				full = true;
			}

			// get the oldest record
			long oldest = Long.MAX_VALUE;
			int index = -1;
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i].x < oldest) {
					oldest = pairs[i].x;
					index = i;
				}
			}
			
			if (index >= 0 && oldest < init) {
				pairs[index] = new Pair(init, value);
			}
		}

		public long getOldest() {
			long oldest = Long.MAX_VALUE;
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i] != null && pairs[i].x < oldest)
					oldest = pairs[i].x;
			}
			return oldest == Long.MAX_VALUE ? -1 : oldest;
		}

		public long getSum() {
			long sum = 0;
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i] != null) {
					sum += pairs[i].y;
				}
			}
			return sum;
		}
		
		public long getSize() {
			long size = 0;
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i] != null) {
					size += 1;
				}
			}
			return size;
		}

		private class Pair implements Serializable {
			private static final long serialVersionUID = 1L;
			long x, y;
			Pair(long x, long y) { this.x = x; this.y = y; }
		}

	}

}
