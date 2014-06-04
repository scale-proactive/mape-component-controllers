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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.extra.component.mape.sla.MetricsListener;
import org.objectweb.proactive.extra.component.mape.sla.SLANotifier;
import org.objectweb.proactive.extra.component.mape.sla.SLORule;
import org.objectweb.proactive.extra.component.mape.sla.SLOStore;

public class SLOStoreImpl extends AbstractPAComponentController implements
		SLOStore, MetricsListener, BindingController {

	private MonitorController monitor;
	private SLANotifier slaNotifier;
	
	private Map<String, SLORule<?>> rules;
	
	String[] itfList = { MonitorController.ITF_NAME, SLANotifier.ITF_NAME };
	
	@Override
	public void addSLO(String name, SLORule<?> rule) {
		rules.put(name, rule);
		// create the metric, add it to the monitor, and subscribe to their updates
		String metricName = rule.getMetricName();
		Metric<?> metric = rule.getMetric();
		monitor.addMetric(metricName, metric);

	}

	@Override
	public void disableSLO(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableSLO(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		rules = new HashMap<String, SLORule<?>>();
	}

	@Override
	public void removeSLO(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMetric(Set<String> updatedMetrics) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindFc(String itfName, Object itf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if(itfName.equals(MonitorController.ITF_NAME)) {
			monitor = (MonitorController) itf;
			return;
		}
		if(itfName.equals(SLANotifier.ITF_NAME)) {
			slaNotifier = (SLANotifier) itf;
			return;
		}
		throw new NoSuchInterfaceException("Interface "+ itfName +" not found!");
		
	}

	@Override
	public String[] listFc() {
		return itfList;
	}

	@Override
	public Object lookupFc(String itfName) throws NoSuchInterfaceException {
		if(itfName.equals(MonitorController.ITF_NAME)) {
			return monitor;
		}
		if(itfName.equals(SLANotifier.ITF_NAME)) {
			return slaNotifier;
		}
		throw new NoSuchInterfaceException("Interface "+ itfName +" not found!");
	}

	@Override
	public void unbindFc(String itfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if(itfName.equals(MonitorController.ITF_NAME)) {
			monitor = null;
		}
		if(itfName.equals(SLANotifier.ITF_NAME)) {
			slaNotifier = null;
		}
		throw new NoSuchInterfaceException("Interface "+ itfName +" not found!");
	}
}
