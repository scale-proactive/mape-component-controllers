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

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.library.MetricsLibrary;
import org.objectweb.proactive.extra.component.mape.remmos.Remmos;
import org.objectweb.proactive.extra.component.mape.sla.SLAService;
import org.objectweb.proactive.extra.component.mape.sla.SLORule;
import org.objectweb.proactive.extra.component.mape.sla.SLOStore;

public class SLAServiceImpl extends AbstractPAComponentController implements SLAService, BindingController {

	SLOStore sloStore;
	
	String[] itfList = { SLOStore.ITF_NAME };
	
	public SLAServiceImpl() {
		super();
	}
	
	@Override
	public void addSLO(String name, Object rule) {

		SLORule<?> sloRule = null;
		
		if(rule instanceof String[]) {
			String [] args = (String[]) rule;
			int nArgs = args.length;
			if(nArgs < 3) {
				System.out.println("Must provide at least 3 arguments: <metricType> <condition> <threshold>");
				return;
			}
			String sloName = name;
			String metricType = args[0];
			String conditionName = args[nArgs-2];
			String threshold = args[nArgs-1];
			String[] metricArgs;
			if(nArgs == 3) {
				metricArgs = null;
			}
			else {
				metricArgs = new String[nArgs-3];
				for(int i=0; i<nArgs-3; i++) {
					metricArgs[i] = args[i+1];
				}
			}
			// TODO It should check if the combination metricType + args is already monitored in the MetricStore, or not.
			Metric<?> metric = MetricsLibrary.getInstance().getMetric(metricType);
			//Class<?> metricReturnType = metric.getClass().getSuperclass().getTypeParameters()[0].getClass();
			System.out.println("sloName         : "+ sloName);
			System.out.println("metricType      : "+ metricType);
			//System.out.println("metricReturnType: "+ metricReturnType.getName());
			System.out.println("conditionName   : "+ conditionName);
			System.out.println("threshold       : "+ threshold);
			
			
			// parse the string, create the rule and store it
			//sloStore.addSLO("newRule", sloRule);
			return;
		}
		System.out.println("Don't know how to handle rules contained in "+ rule.getClass().getName());
	}

	@Override
	public void disableSLO(String name) {
		sloStore.disableSLO(name);		
	}

	@Override
	public void enableSLO(String name) {
		sloStore.enableSLO(name);		
	}

	@Override
	public void removeSLO(String name) {
		sloStore.removeSLO(name);
	}

	@Override
	public void bindFc(String itfName, Object itf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if(itfName.equals(SLOStore.ITF_NAME)) {
			sloStore = (SLOStore) itf;
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
		if(itfName.equals(SLOStore.ITF_NAME)) {
			return sloStore;
		}
		throw new NoSuchInterfaceException("Interface "+ itfName +" not found!");
	}

	@Override
	public void unbindFc(String itfName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if(itfName.equals(SLOStore.ITF_NAME)) {
			sloStore = null;
		}
		throw new NoSuchInterfaceException("Interface "+ itfName +" not found!");
	}
	
}
