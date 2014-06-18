/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.autonomic.controllers.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.Condition;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.OutgoingRequestRecord;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

/**
 * Log Storage component for the Monitoring Framework.
 * Contains a collections of AbstractRecord objects.
 * 
 *         ****** Another implementation could introduce another kind of Log. ******
 * **** This is another implementation of RecordStore, do not give importance to the IDs.******
 *
 * 
 * @author mibanez
 *
 */
public class RecordStoreImpl extends AbstractPAComponentController implements RecordStore {

	private static final long serialVersionUID = 1L;

    private CircularArrayList<IncomingRequestRecord> inCircleArray = new CircularArrayList<IncomingRequestRecord>();
    private CircularArrayList<OutgoingRequestRecord> outCircleArray = new CircularArrayList<OutgoingRequestRecord>();
    private Map<ComponentRequestID, IncomingRequestRecord> inMap = new HashMap<ComponentRequestID, IncomingRequestRecord>();
    private Map<ComponentRequestID, OutgoingRequestRecord> outMap = new HashMap<ComponentRequestID, OutgoingRequestRecord>();

    private int maxSize = 256;

    public void setMaxSize(int maxSize) {
    	this.maxSize = maxSize;
    }

	private void insert(IncomingRequestRecord record) {
		if (inCircleArray.size() >= maxSize) {
			IncomingRequestRecord oldIrr = inCircleArray.remove(0);
			inMap.remove(oldIrr.getRequestID());
		}
		inCircleArray.add((IncomingRequestRecord) record);
		inMap.put(record.getRequestID(), (IncomingRequestRecord) record);
	}

	private void insert(OutgoingRequestRecord record) {
		if (outCircleArray.size() >= maxSize) {
			OutgoingRequestRecord oldOrr = outCircleArray.remove(0);
			outMap.remove(oldOrr.getRequestID());
		}
		outCircleArray.add((OutgoingRequestRecord) record);
		outMap.put(record.getRequestID(), (OutgoingRequestRecord) record);
	}

	public void update(IncomingRequestRecord record) {
		synchronized (inCircleArray) {
			IncomingRequestRecord oldRecord = inMap.get(record.getRequestID());
			if (oldRecord != null) {
				oldRecord.setFinished(record.isFinished());
				oldRecord.setArrivalTime(record.getArrivalTime());
				oldRecord.setReplyTime(record.getReplyTime());
				oldRecord.setServingStartTime(record.getServingStartTime());
			} else {
				insert(record);
			}
		}
	}

	public void update(OutgoingRequestRecord record) {
		synchronized (outCircleArray) {
			OutgoingRequestRecord oldRecord = outMap.get(record.getRequestID());
			if (oldRecord != null) {
				oldRecord.setFinished(record.isFinished());
				oldRecord.setSentTime(record.getSentTime());
				oldRecord.setReplyReceptionTime(record.getReplyReceptionTime());
			} else {
				insert(record);
			}
		}
	}

	public Wrapper<IncomingRequestRecord> getIncomingRequestRecord(ComponentRequestID id) {
		return new Wrapper<IncomingRequestRecord>(inMap.get(id));
	}

	public Wrapper<OutgoingRequestRecord> getOutgoingRequestRecord(ComponentRequestID id) {
		return new Wrapper<OutgoingRequestRecord>(outMap.get(id));
	}

	@Override
	public void reset() {
		inCircleArray.clear();
		outCircleArray.clear();
	}

	// ........................................................................................................

	@Override
	public List<IncomingRequestRecord> getIncomingRequestRecords() {
		List<IncomingRequestRecord> list = new ArrayList<IncomingRequestRecord>();
		list.addAll(inMap.values());
		return list;
	}

	@Override
	public List<IncomingRequestRecord> getIncomingRequestRecords(int amount) {
		
		List<IncomingRequestRecord> list = new ArrayList<IncomingRequestRecord>();
		synchronized (inCircleArray) {

			int stopPoint = inCircleArray.size() - amount;
			if (stopPoint < 0)
				stopPoint = 0;
	
			for (int i = inCircleArray.size() - 1; i >= stopPoint; i--) {
				list.add(inCircleArray.get(i));
			}
		}
		
		return list;
	}

	@Override
	public List<IncomingRequestRecord> getIncomingRequestRecords(int amount, Condition<IncomingRequestRecord> condition) {

		List<IncomingRequestRecord> list = new ArrayList<IncomingRequestRecord>();
		synchronized (inCircleArray) {

			int stopPoint = inCircleArray.size() - amount;
	
			for (int i = inCircleArray.size() - 1; i >= stopPoint && i >= 0; i--) {
				IncomingRequestRecord irr = inCircleArray.get(i);
				if (condition.evaluate(irr)) {
					list.add(irr);
				} else {
					stopPoint--;
				}
			}
		}
		
		return list;
	}

	@Override
	public List<IncomingRequestRecord> getIncomingRequestRecords(Condition<IncomingRequestRecord> condition) {
		
		List<IncomingRequestRecord> result = new ArrayList<IncomingRequestRecord>();
		synchronized (inCircleArray) {
			for (IncomingRequestRecord irr : inCircleArray) {
				if(condition.evaluate(irr))
					result.add(irr);
			}
		}

		return result;
	}


	@Override
	public List<OutgoingRequestRecord> getOutgoingRequestRecords() {
		List<OutgoingRequestRecord> list = new ArrayList<OutgoingRequestRecord>();
		list.addAll(outMap.values());
		return list;
	}

	@Override
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(int amount) {
		
		List<OutgoingRequestRecord> list = new ArrayList<OutgoingRequestRecord>();
		synchronized (outCircleArray) {

			int stopPoint = outCircleArray.size() - amount;
			if (stopPoint < 0)
				stopPoint = 0;
	
			for (int i = outCircleArray.size() - 1; i >= stopPoint; i--) {
				list.add(outCircleArray.get(i));
			}
		}
		
		return list;
	}

	@Override
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(Condition<OutgoingRequestRecord> condition) {
		
		List<OutgoingRequestRecord> result = new ArrayList<OutgoingRequestRecord>();

		synchronized (outCircleArray) {
			for (OutgoingRequestRecord orr : outCircleArray) {
				if(condition.evaluate(orr))
					result.add(orr);
			}
		}

		return result;
	}

	@Override
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(int amount,
			Condition<OutgoingRequestRecord> condition) {
		
		List<OutgoingRequestRecord> list = new ArrayList<OutgoingRequestRecord>();
		synchronized (outCircleArray) {

			int stopPoint = outCircleArray.size() - amount;
	
			for (int i = outCircleArray.size() - 1; i >= stopPoint && i >= 0; i--) {
				OutgoingRequestRecord irr = outCircleArray.get(i);
				if (condition.evaluate(irr)) {
					list.add(irr);
				} else {
					stopPoint--;
				}
			}
		}
		
		return list;
	}

}
