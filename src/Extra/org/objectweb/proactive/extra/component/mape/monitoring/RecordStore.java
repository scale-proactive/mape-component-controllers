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
package org.objectweb.proactive.extra.component.mape.monitoring;

import java.util.List;

import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.OutgoingRequestRecord;
import org.objectweb.proactive.extra.component.mape.utils.Wrapper;

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

	public void setMaxSize(int maxSize);

	public void update(IncomingRequestRecord record);
	public void update(OutgoingRequestRecord record);

	public Wrapper<IncomingRequestRecord> getIncomingRequestRecord(ComponentRequestID id);
	public Wrapper<OutgoingRequestRecord> getOutgoingRequestRecord(ComponentRequestID id);

	// clean the logs
	public void reset();
	
	// select an specific set of records
	
	public List<IncomingRequestRecord> getIncomingRequestRecords();
	public List<IncomingRequestRecord> getIncomingRequestRecords(int amount);
	public List<IncomingRequestRecord> getIncomingRequestRecords(int amount, Condition<IncomingRequestRecord> condition);
	public List<IncomingRequestRecord> getIncomingRequestRecords(Condition<IncomingRequestRecord> condition);

	public List<OutgoingRequestRecord> getOutgoingRequestRecords();
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(int amount);
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(int amount, Condition<OutgoingRequestRecord> condition);
	public List<OutgoingRequestRecord> getOutgoingRequestRecords(Condition<OutgoingRequestRecord> condition);
	


}
