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
package org.objectweb.proactive.extra.component.mape.monitoring.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.monitoring.records.AbstractRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.OutgoingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordStore;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordType;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Transformation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

/**
 * Log Storage component for the Monitoring Framework.
 * Contains a collections of AbstractRecord objects.
 * 
 * Another implementation could introduce another kind of Log.
 * 
 * @author cruz
 *
 */
public class RecordStoreImpl extends AbstractPAComponentController implements RecordStore {

	private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_MONITORING);

	/** Log for incoming requests */
    private Map<ComponentRequestID, IncomingRequestRecord> incomingRequestLog;
    
    /** Log for outgoing request */
    private Map<ComponentRequestID, OutgoingRequestRecord> outgoingRequestLog;

	
	public void init() {
		logger.debug("[Log Store] Initializing logs ...");
		// should some of these two HashMap's be synchronized?		
		incomingRequestLog = new ConcurrentHashMap<ComponentRequestID, IncomingRequestRecord>();
    	outgoingRequestLog = new ConcurrentHashMap<ComponentRequestID, OutgoingRequestRecord>();
	}

	@Override
	public BooleanWrapper exists(Object key, RecordType rt) {
		if(rt == RecordType.IncomingRequestRecord) {
			if(incomingRequestLog.containsKey(key)) {
				return new BooleanWrapper(true);
			}
		}
		if(rt == RecordType.OutgoingRequestRecord) {
			if(outgoingRequestLog.containsKey(key)) {
				return new BooleanWrapper(true);
			}
		}
		return new BooleanWrapper(false);
	}

	@Override
	public AbstractRecord fetch(Object key, RecordType rt) {
		if(rt == RecordType.IncomingRequestRecord) {
			return incomingRequestLog.get(key);	
		}
		else if(rt == RecordType.OutgoingRequestRecord) {
			return outgoingRequestLog.get(key);
		}
		else {
			logger.debug("ERROR. Fetch: Unrecognized RecordType");
		}
		return null;
	}
	
	// FIXME Should use the general fetch (from above), but that gives a ClassCastException when passing it as a parameter, won't fix it now...
	// Maybe it's because the return type (for the Future) is extended from AbstractRecord and so the result can't be cast Incoming/OutgoingRequestRecord? 
	public IncomingRequestRecord fetchIncomingRequestRecord(Object key) {
		return incomingRequestLog.get(key);
	}
	public OutgoingRequestRecord fetchOutgoingRequestRecord(Object key) {
		return outgoingRequestLog.get(key);
	}

	@Override
	public void insert(AbstractRecord record) {
		if(record.getRecordType() == RecordType.IncomingRequestRecord) {
			//logger.debug("INSERTING IN REQ LOG: ID: "+ record.getRequestID() + " -- " + ((RequestRecord)record).getCalledComponent() + "." + ((RequestRecord)record).getInterfaceName() + "." + ((RequestRecord)record).getMethodName() + " -- " + ((RequestRecord)record).getArrivalTime() + ", "+ ((RequestRecord)record).getServingStartTime() + ", "+ ((RequestRecord)record).getReplyTime());
			incomingRequestLog.put(record.getRequestID(), (IncomingRequestRecord) record);
		}
		else if(record.getRecordType() == RecordType.OutgoingRequestRecord) {
			//logger.debug("INSERTING IN CALL LOG: ID: "+ record.getRequestID() + " -- " + ((CallRecord)record).getCalledComponent() + "." + ((CallRecord)record).getInterfaceName() + "." + ((CallRecord)record).getMethodName() + " -- " + ((CallRecord)record).getSentTime() + ", "+ ((CallRecord)record).getReplyReceptionTime() );
			outgoingRequestLog.put(record.getRequestID(), (OutgoingRequestRecord) record);
		}
		else {
			logger.debug("ERROR. Insert: Unrecognized RecordType, ID:"+ record.getRequestID() + ", ");
		}
	}
	
	// the same from above... because HashMap.put() replaces old value!!
	@Override
	public void update(Object key, AbstractRecord record) {
		if(record.recordType == RecordType.IncomingRequestRecord) {
			incomingRequestLog.put(record.getRequestID(), (IncomingRequestRecord) record);
		}
		else if(record.recordType == RecordType.OutgoingRequestRecord) {
			outgoingRequestLog.put(record.getRequestID(), (OutgoingRequestRecord) record);
		}
		else {
			logger.debug("ERROR. Update: Unrecognized RecordType");
		}
	} 

	@Override
	public Map<ComponentRequestID, OutgoingRequestRecord> getOutgoingRequestRecords() {
		
		Map<ComponentRequestID, OutgoingRequestRecord> callRecords = new HashMap<ComponentRequestID, OutgoingRequestRecord>(outgoingRequestLog.size());
		// copy all entries of the log
		callRecords.putAll(outgoingRequestLog);
		return callRecords;
	}

	@Override
	public Map<ComponentRequestID, IncomingRequestRecord> getIncomingRequestRecords() {
		
		Map<ComponentRequestID, IncomingRequestRecord> requestRecords = new HashMap<ComponentRequestID, IncomingRequestRecord>(outgoingRequestLog.size());
		// copy all entries of the log
		requestRecords.putAll(incomingRequestLog);
		return requestRecords;
	}

	/**
	 * Returns a subset of all the entries in the Call Log with an specific parent ID
	 */
	@Override
	public Map<ComponentRequestID, OutgoingRequestRecord> getOutgoingRequestRecordsFromParent(
			ComponentRequestID id) {

		Map<ComponentRequestID, OutgoingRequestRecord> selectedRecords = new HashMap<ComponentRequestID, OutgoingRequestRecord>();
		OutgoingRequestRecord cr;
		
		// TODO Perform the query in a more efficient way
		for(ComponentRequestID crid: outgoingRequestLog.keySet()) {
			cr = outgoingRequestLog.get(crid);
			// put all the records that have 'id' as parent
			if(cr.getParentID().equals(id)) {
				selectedRecords.put(crid, cr);
			}
		}
		return selectedRecords;
	}
	
	/**
	 * Returns a subset of all the entries in the Request Log with the same root ID
	 */
	@Override
	public Map<ComponentRequestID, IncomingRequestRecord> getIncomingRequestRecordsFromRoot(
			ComponentRequestID rootID) {

		Map<ComponentRequestID, IncomingRequestRecord> selectedRecords = new HashMap<ComponentRequestID, IncomingRequestRecord>();
		IncomingRequestRecord rr;
		
		// TODO Perform the query in a more efficient way
		for(ComponentRequestID crid: incomingRequestLog.keySet()) {
			rr = incomingRequestLog.get(crid);
			// put all the records that have 'rootID' as root
			if(rr.getRootID().equals(rootID)) {
				selectedRecords.put(crid, rr);
			}
		}
		return selectedRecords;
	}

	@Override
	public void reset() {
		incomingRequestLog.clear();
		outgoingRequestLog.clear();
	}
	
	
	public List<ComponentRequestID> getListOfRequestIDs() {
		Set<ComponentRequestID> keyset = incomingRequestLog.keySet();
		List<ComponentRequestID> keylist = new ArrayList<ComponentRequestID>(keyset.size());
		keylist.addAll(keyset);
		//Collections.sort(keylist);
		return keylist;
	}
    
	public List<ComponentRequestID> getListOfCallIDs() {
		Set<ComponentRequestID> keyset = outgoingRequestLog.keySet();
		List<ComponentRequestID> keylist = new ArrayList<ComponentRequestID>(keyset.size());
		keylist.addAll(keyset);
		//Collections.sort(keylist);
		return keylist;
	}

	@Override
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(Condition<OutgoingRequestRecord> condition) {
		
		List<OutgoingRequestRecord> result = new ArrayList<OutgoingRequestRecord>();
		// applies condition to all IncomingRequestRecords stored
		for(OutgoingRequestRecord orr : outgoingRequestLog.values()) {
			if(condition.evaluate(orr)) {
				result.add(orr);
			}
		}
		return result;
	}

	@Override
	public List<?> getOutgoingRequestRecords(
			Condition<OutgoingRequestRecord> condition,
			Transformation<OutgoingRequestRecord, ?> transformation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IncomingRequestRecord> getIncomingRequestRecords(
			Condition<IncomingRequestRecord> condition) {
		
		List<IncomingRequestRecord> result = new ArrayList<IncomingRequestRecord>();
		// applies condition to all IncomingRequestRecords stored
		for(IncomingRequestRecord irr : incomingRequestLog.values()) {
			if(condition.evaluate(irr)) {
				result.add(irr);
			}
		}
		return result;
	}

	@Override
	public List<?> getIncomingRequestRecords(
			Condition<IncomingRequestRecord> condition,
			Transformation<IncomingRequestRecord, ?> transformation) {

		return null;
	}

}
