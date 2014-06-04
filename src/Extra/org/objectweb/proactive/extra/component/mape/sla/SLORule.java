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
package org.objectweb.proactive.extra.component.mape.sla;

import java.io.Serializable;

import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.sla.AlarmLevel;
import org.objectweb.proactive.extra.component.mape.sla.Condition;

/**
 * This class represents a very simple rule.
 * metricValue <condition> threshold value.
 * 
 * If the condition fails, an alarm is sent.
 *  * Optionally, it contains a preventive value that triggers a preventive alarm.
 * 
 * 
 * @author cruz
 *
 */
public class SLORule<T> implements Serializable {

	/** The name of the monitored metric */
	private String metricName;
	
	T metricValue;

	Metric<T> metric;
	
	Condition<T> condition;
	
	T threshold;

	T preventiveThreshold;
	
	private MonitorController monitor; 
	
	boolean enabled = true;
	
	/** 
	 * Checks the condition and determines if an alarm must be raised
	 * 
	 * @return
	 */
	public AlarmLevel check() {
		
		metricValue = (T) monitor.calculateMetric(metricName);
		
		if(condition.evaluate(metricValue, threshold)) {
			return AlarmLevel.OK;
		}
		return null;
	}
	
	public boolean isEnabled() {
		return enabled; 
	}
	
	public void setEnabled(boolean e) {
		enabled = e;
	}
	
	public String getMetricName() {
		return metricName;
	}
	
	public void setMonitor(MonitorController ref) {
		monitor = ref;
	}
	
	public Metric<?> getMetric() {
		return metric;
	}
	
	
}
