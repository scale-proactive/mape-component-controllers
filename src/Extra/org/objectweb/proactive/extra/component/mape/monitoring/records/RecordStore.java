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

import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.component.mape.monitoring.records.AbstractRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.OutgoingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordType;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Transformation;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

/**
 * Interface for storing monitoring records in the Log Store
 * 
 * Very basic management of log entries.
 * 
 * @author cruz
 *
 */
public interface RecordStore {

	public final static String ITF_NAME = "record-store-nf";

	// init the logs store
	void init();
	
	// inserts new record in the store
	void insert(AbstractRecord record);
	
	// fetches an existing record in the store
	AbstractRecord fetch(Object key, RecordType rt);
	IncomingRequestRecord fetchIncomingRequestRecord(Object key);
	OutgoingRequestRecord fetchOutgoingRequestRecord(Object key);
	
	// queries the existence of a record in the store
	BooleanWrapper exists(Object key, RecordType rt);

	// updates an existing record
	void update(Object key, AbstractRecord record);
	
	// test: obtain logs
	Map<ComponentRequestID, IncomingRequestRecord> getIncomingRequestRecords();
	Map<ComponentRequestID, OutgoingRequestRecord> getOutgoingRequestRecords();
	
	// obtain subset of entries
	Map<ComponentRequestID, OutgoingRequestRecord> getOutgoingRequestRecordsFromParent(ComponentRequestID id);
	Map<ComponentRequestID, IncomingRequestRecord> getIncomingRequestRecordsFromRoot(ComponentRequestID rootID);
	
	// clean the logs
	void reset();
	
	
	public List<ComponentRequestID> getListOfRequestIDs();
	public List<ComponentRequestID> getListOfCallIDs();
	
	// select an specific set of records
	public List<IncomingRequestRecord> getIncomingRequestRecords(Condition<IncomingRequestRecord> condition);
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(Condition<OutgoingRequestRecord> condition);
	
	public List<?> getIncomingRequestRecords(Condition<IncomingRequestRecord> condition, Transformation<IncomingRequestRecord,?> transformation);
	public List<?> getOutgoingRequestRecords(Condition<OutgoingRequestRecord> condition, Transformation<OutgoingRequestRecord,?> transformation);
	
}
