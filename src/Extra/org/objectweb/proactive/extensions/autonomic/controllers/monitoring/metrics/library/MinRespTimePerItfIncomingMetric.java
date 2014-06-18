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
package org.objectweb.proactive.extra.component.mape.monitoring.metrics.library;

import java.util.List;

import org.objectweb.proactive.extra.component.mape.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.records.Condition;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;

/**
 * Calculates the Average Response Time of all the requests that have been served by the component.
 * 
 * @author cruz
 *
 */

public class MinRespTimePerItfIncomingMetric extends Metric<Long> {

	private Long value;
	private String itfName;
	
	public MinRespTimePerItfIncomingMetric(String interfaceName) {
		itfName = interfaceName;
		this.subscribeTo(RemmosEventType.INCOMING_REQUEST_EVENT);
	}
	
	public Long calculate() {

		List<IncomingRequestRecord> recordList = null;
		recordList = recordStore.getIncomingRequestRecords(new Condition<IncomingRequestRecord>(){
			// condition that returns true for every record
			@Override
			public boolean evaluate(IncomingRequestRecord irr) {
				if(irr.getInterfaceName().equals(itfName)) {
					return true;
				}
				return false;
			}
		}
		);
		
		// and calculates the average
		long min = Long.MAX_VALUE;
		long respTime;
		for(IncomingRequestRecord irr : recordList) {
			if(irr.isFinished()) {
				respTime = irr.getReplyTime() - irr.getArrivalTime();
				if( respTime <= min ) {
					min = respTime;
				}
			}
		}
		value = min;
		return value;
	}
	
	@Override
	public Long getValue() {
		return this.value;
	}

	@Override
	public void setValue(Long value) {
		this.value = value;
	}

}


