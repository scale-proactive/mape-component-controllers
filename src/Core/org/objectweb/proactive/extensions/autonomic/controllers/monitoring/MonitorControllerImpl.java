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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

/**
 * Monitor Controller component for the Monitoring Framework
 * 
 * This NF Component controls the behaviour of the monitoring related activity.
 * of a Component.
 * 
 * @author cruz
 * 
 */
public class MonitorControllerImpl extends AbstractPAComponentController implements
		MonitorController, BindingController {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_MONITORING);

	private EventControl eventControl = null;
	private RecordStore recordStore = null;
	private MetricStore metricsStore = null;

	// interfaces for monitors of internal and external components
	private Map<String, MonitorController> externalMonitors = new HashMap<String, MonitorController>();
	private Map<String, MonitorController> internalMonitors = new HashMap<String, MonitorController>();
	private Map<String, MonitorControllerMulticast> externalMonitorsMulticast = new HashMap<String, MonitorControllerMulticast>();

	private String hostComponentName;
	private String basicItfs[] = {
		EventControl.ITF_NAME,
		RecordStore.ITF_NAME,
		MetricStore.ITF_NAME
	};

	/** Monitoring status */
	private boolean started = false;

	
	@Override
	public void startGCMMonitoring() {
		
		if (started) {
			return;
		}
		
		hostComponentName = hostComponent.getComponentParameters().getControllerDescription().getName();
		logger.debug("[Monitor Control] My Host component is " + hostComponentName + "[ID: " + hostComponent.getID() + "]");
		// configure the event listener

		String runtimeURL = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
		logger.debug("[Monitor Control] RuntimeURL = " + runtimeURL);
		this.eventControl.setBodyToMonitor(hostComponent.getID(), runtimeURL, hostComponentName);

		// start the other components of the framework
		this.eventControl.start();

		for (MonitorController in : internalMonitors.values()) {
			in.startGCMMonitoring();
		}
		for (MonitorControllerMulticast em : externalMonitorsMulticast.values()) {
			em.startGCMMonitoring();
		}
		for (String key : externalMonitors.keySet()) {
			if (!key.startsWith("parent")) {
				externalMonitors.get(key).startGCMMonitoring();
			}
		}

		started = true;
	}

	@Override
	public void stopGCMMonitoring() {
		started = false;
	}

	@Override
	public void resetGCMMonitoring() {
		this.recordStore.reset();
		this.eventControl.reset();
	}

	@Override
	public Wrapper<Boolean> isGCMMonitoringStarted() {
		return new ValidWrapper<Boolean>(started);
	}

	// TODO
	public RequestPath getPathStatisticsForId(ComponentRequestID id) {
		return null;
	}

	public List<String> getNotificationsReceived() {
		return eventControl.getNotifications();
	}

	@Override
	public String getMonitoredComponentName() {
		return hostComponentName;
	}

	// CONFIG
	
	public void setRecordStoreCapacity(int maxCapacity) {
		recordStore.setMaxSize(maxCapacity);
	}
	
	// METRICS 

	/** {@inheritDoc} */
	@Override
	public Wrapper<String> getMetricState(String metricName) {
		return metricsStore.getMetricState(metricName);
	}

	/** {@inheritDoc} */
	@Override
    public Wrapper<Boolean> enableMetric(String metricName) {
		return metricsStore.enableMetric(metricName);
	}

    /** {@inheritDoc} */
	@Override
    public Wrapper<Boolean> disableMetric(String metricName) {
		return metricsStore.disableMetric(metricName);
	}

	@Override
	public Wrapper<Boolean> addMetric(String name, Metric<?> metric) {
		return metricsStore.addMetric(name, metric);
	}

	@Override
	public Wrapper<Boolean> removeMetric(String metricName) {
		return metricsStore.removeMetric(metricName);
	}

	@Override
	public Wrapper<HashSet<String>> getMetricList() {
		return metricsStore.getMetricList();
	}

	@Override
	public Wrapper<HashSet<String>> getMetricList(String itfPath) {
		return metricsStore.getMetricList(itfPath);
	}

	@Override
	public <T extends Serializable> Wrapper<T> calculateMetric(String name) {
		return metricsStore.calculate(name);
	}

	@Override
	public <T extends Serializable> Wrapper<T> calculateMetric(String name, String itfPath) {
		return metricsStore.calculate(name, itfPath);
	}

	@Override
	public <T extends Serializable> Wrapper<T> getMetricValue(String name) {
		return metricsStore.getValue(name);
	}

	@Override
	public <T extends Serializable> Wrapper<T> getMetricValue(String name, String itfPath) {
		return metricsStore.getValue(name, itfPath);
	}

	@Override
	public void setMetricValue(String name, Object value) {
		metricsStore.setValue(name, value);
	}

	@Override
	public void setMetricValue(String name, Object value, String itfPath) {
		metricsStore.setValue(name, value, itfPath);
	}

	// BindingController interface

	@Override
	public void bindFc(String cItf, Object sItf)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {

		if (cItf.equals(EventControl.ITF_NAME)) {
			eventControl = (EventControl) sItf;
		} else if (cItf.equals(RecordStore.ITF_NAME)) {
			recordStore = (RecordStore) sItf;
		} else if (cItf.equals(MetricStore.ITF_NAME)) {
			metricsStore = (MetricStore) sItf;
		} else if (cItf.endsWith("-external-" + MonitorController.ITF_NAME)) {
			// it refers to the monitoring interface of an external component (bound
			// from an external client interface)
			if (sItf instanceof MonitorController) {
				// WARN: does not check if the corresponding external client
				// interface exists in the host component
				// The server interface maybe a Multicast. In that case, it must be
				// cast appropriately.!!!
				externalMonitors.put(cItf, (MonitorController) sItf);
			} else if (sItf instanceof MonitorControllerMulticast) {
				// System.out.println("   bindFc. Binding ["+cItf+"] to Multicast interface");
				externalMonitorsMulticast.put(cItf,
						(MonitorControllerMulticast) sItf);
			}
		} else if (cItf.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			// it refers to the monitoring interface of an internal component
			// (external server interface bound to an internal server interface)
		
			// WARN: does not check if the corresponding internal server
			// interface exists in the host component
			internalMonitors.put(cItf, (MonitorController) sItf);
		} else {
			throw new NoSuchInterfaceException("Interface [" + cItf
					+ "] not found ... Type received: " + sItf.getClass().getName());
		}
	}

	@Override
	public String[] listFc() {
		int nExternalMonitors = externalMonitors.size();
		int nInternalMonitors = internalMonitors.size();
		int nExternalMonitorsMulticast = externalMonitorsMulticast.size();
		int nBasicItfs = basicItfs.length;

		ArrayList<String> itfsList = new ArrayList<String>(nExternalMonitors
				+ nInternalMonitors + nExternalMonitorsMulticast + nBasicItfs);
		for (int i = 0; i < nBasicItfs; i++) {
			itfsList.add(basicItfs[i]);
		}
		itfsList.addAll(externalMonitors.keySet());
		itfsList.addAll(internalMonitors.keySet());

		return itfsList.toArray(new String[itfsList.size()]);
	}

	@Override
	public Object lookupFc(String cItf) throws NoSuchInterfaceException {
		if (cItf.equals(EventControl.ITF_NAME)) {
			return eventControl;
		}
		if (cItf.equals(RecordStore.ITF_NAME)) {
			return recordStore;
		}
		if (cItf.equals(MetricStore.ITF_NAME)) {
			return metricsStore;
		}
		if (cItf.endsWith("-external-" + MonitorController.ITF_NAME)) {
			// System.out.println("   Looking up ... "+ cItf);
			// the interface maybe a singleton or a multicast
			if (externalMonitors.containsKey(cItf)) {
				return externalMonitors.get(cItf);
			}
			return externalMonitorsMulticast.get(cItf);
		}
		if (cItf.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			return internalMonitors.get(cItf);
		}
		throw new NoSuchInterfaceException("Interface " + cItf
				+ " non existent");
	}

	@Override
	public void unbindFc(String cItf) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (cItf.equals(EventControl.ITF_NAME)) {
			eventControl = null;
		} else if (cItf.equals(RecordStore.ITF_NAME)) {
			recordStore = null;
		} else if (cItf.equals(MetricStore.ITF_NAME)) {
			metricsStore = null;
		} else if (cItf.endsWith("-external-" + MonitorController.ITF_NAME)) {
			if (externalMonitors.containsKey(cItf)) {
				externalMonitors.put(cItf, null);
			} else if (externalMonitorsMulticast.containsKey(cItf)) {
				externalMonitorsMulticast.put(cItf, null);
			}
		} else if (cItf.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			internalMonitors.put(cItf, null);
		} else {
			throw new NoSuchInterfaceException("Interface " + cItf + " non existent");
		}
	}

}
