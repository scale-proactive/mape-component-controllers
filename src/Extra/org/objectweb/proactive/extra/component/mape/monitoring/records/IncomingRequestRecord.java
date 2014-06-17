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

import java.io.Serializable;

import org.objectweb.proactive.extra.component.mape.monitoring.records.AbstractRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordType;


/**
 * Stores the data and timestamps related to an incoming request (a call from another component).
 * 
 * @author cruz
 *
 */
public class IncomingRequestRecord extends AbstractRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	private String callerComponent;
	private String calledComponent;
	private String interfaceName;
	private String methodName;

	private long arrivalTime;
	private long servingStartTime;
	private long replyTime;
	
	private ComponentRequestID rootID;
	
	private boolean finished;
	
	public IncomingRequestRecord() { }
	
	public IncomingRequestRecord(ComponentRequestID requestID, String callerComponent, String calledComponent, String interfaceName, String methodName, long arrivalTime, ComponentRequestID rootID) {
		super(requestID);
		this.callerComponent = callerComponent;
		this.calledComponent = calledComponent;
		this.interfaceName = interfaceName;
		this.methodName = methodName;
		this.arrivalTime = arrivalTime;
		this.finished = false;
		this.rootID = rootID;
	}
	
	public long getServingStartTime() {
		return servingStartTime;
	}

	public void setServingStartTime(long servingStartTime) {
		this.servingStartTime = servingStartTime;
	}

	public long getReplyTime() {
		return replyTime;
	}

	public void setReplyTime(long replyTime) {
		this.replyTime = replyTime;
	}

	public String getCallerComponent() {
		return callerComponent;
	}
	
	public String getCalledComponent() {
		return calledComponent;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public boolean isFinished() {
		if(finished) return true;
		finished = (arrivalTime != 0 && servingStartTime != 0 && replyTime != 0);
		return finished;
	}
	
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public ComponentRequestID getRootID() {
		return rootID;
	}

	@Override
	public RecordType getRecordType() {
		return RecordType.IncomingRequest;
	}
	
}
