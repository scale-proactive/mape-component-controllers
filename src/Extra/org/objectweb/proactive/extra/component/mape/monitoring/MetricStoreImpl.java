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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricEventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricStore;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extra.component.mape.monitoring.event.RemmosEvent;
import org.objectweb.proactive.extra.component.mape.monitoring.event.RemmosEventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.ValidMetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.WrongMetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordStore;


public class MetricStoreImpl extends AbstractPAComponentController implements MetricStore, RemmosEventListener, BindingController {

	private static final long serialVersionUID = 1L;

	// functional client interface name --> external monitor controller
	private Map<String, MonitorController> externalMonitors = new HashMap<String, MonitorController>();
	
	// functional server interface name --> internal monitor controller
	private Map<String, MonitorController> internalMonitors = new HashMap<String, MonitorController>();
	
	// functional client multicast interface name --> external monitor controller multicast
	private Map<String, MonitorControllerMulticast> externalMonitorsMulticast = new HashMap<String, MonitorControllerMulticast>();

	// metric name --> metric
	private Map<String, Metric<?>> metrics = new HashMap<String, Metric<?>>();
		
	private RecordStore records;
	private MetricEventListener metricEventListener;

	@Override
	public void addMetric(String name, Metric<?> metric) {
		metric.setRecordSource(records);
		metric.setMetricSource((MetricStore) this);
		metrics.put(name, metric);
	}

	@Override
	public MetricValue calculate(String name) {
		if(metrics.containsKey(name)) {
			MetricValue mv = new ValidMetricValue(metrics.get(name).calculate(), false);
			if (metricEventListener != null) {
				metricEventListener.notifyMetricUpdate(name);
			}
			return mv;
		}
		return new WrongMetricValue("Metric \"" + name + "\" not found.");
	}

	@Override
	public void disableMetric(String name) {
		Metric<?> metric = metrics.get(name);
		if(metric != null) {
			metric.disableEventSubsctiption();
		}
	}

	@Override
	public void enableMetric(String name) {
		Metric<?> metric = metrics.get(name);
		if(metric != null) {
			metric.enableEventSubscription();
		}
	}

	@Override
	public MetricValue getValue(String name) {
		Metric<?> metric = metrics.get(name);
		if(metric != null) {
			return new ValidMetricValue(metric.getValue(), false);
		}
		return new WrongMetricValue("Metric \"" + name + "\" not found.");
	}

	@Override
	public void removeMetric(String name) {
		metrics.remove(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(String name, Object v) {
		Metric metric = metrics.get(name);
		if(metric != null) {
			metric.setValue(v);
		}
	}
	
	@Override
	public List<String> getMetricList() {
		Set<String> keys = metrics.keySet();
		List<String> res = new ArrayList<String>(keys.size());
		res.addAll(keys);
		return res;
	}

	// EXTERNAL METRICS API

	private String getNextItfName(String itfPath) {
		StringTokenizer token = new StringTokenizer(itfPath.trim(), "/");
		if (!token.hasMoreTokens()) {
			return null;
		}
		return token.nextToken();
	}

	private String getNextItfPath(String itfPath) {
		StringTokenizer token = new StringTokenizer(itfPath.trim(), "/");
		if (!token.hasMoreTokens()) {
			return null;
		}
		token.nextToken();
		String nextPath = "";
		while (token.hasMoreTokens())
		nextPath += "/" + token.nextToken();
		return nextPath.equals("") ? "/" : nextPath;
	}

	private Object getRemoteMonitorController(String itfName) {
		Object remoteMon = null;
		if ((remoteMon = externalMonitors.get(itfName)) == null) {
			if ((remoteMon = internalMonitors.get(itfName)) == null) {
				remoteMon = externalMonitorsMulticast.get(itfName);
			}
		}
		return remoteMon;
	}

	@Override
	public MetricValue calculate(String name, String itfPath) {
		String nextItfName = getNextItfName(itfPath);
		if (nextItfName == null) {
			return this.calculate(name);
		}
		
		Object remoteMon = getRemoteMonitorController(nextItfName);
		
		if (remoteMon instanceof MonitorController) {
			return ((MonitorController) remoteMon).calculateMetric(name, getNextItfPath(itfPath));
		}
		else if (remoteMon instanceof MonitorControllerMulticast) {
			return new ValidMetricValue(((MonitorControllerMulticast) remoteMon).calculateMetric(name, getNextItfPath(itfPath)), true);
		}
		
		(new NoSuchInterfaceException(nextItfName)).printStackTrace();
		return new WrongMetricValue("Monitor cant reach interface \"" + nextItfName + "\".");
	}

	@Override
	public MetricValue getValue(String name, String itfPath) {
		String nextItfName = getNextItfName(itfPath);
		if (nextItfName == null) {
			// LocalRequest
			return this.getValue(name);
		}

		Object remoteMon = getRemoteMonitorController(nextItfName);
		
		if (remoteMon instanceof MonitorController) {
			return ((MonitorController) remoteMon).getMetricValue(name, getNextItfPath(itfPath));
		} else if (remoteMon instanceof MonitorControllerMulticast) {
			return new ValidMetricValue(((MonitorControllerMulticast) remoteMon).getMetricValue(name, getNextItfPath(itfPath)), true);
		}
	
		(new NoSuchInterfaceException(nextItfName)).printStackTrace();
		return new WrongMetricValue("Monitor cant reach interface \"" + nextItfName + "\".");
	}

	@Override
	public void setValue(String name, Object v, String itfPath) {
		String nextItfName = getNextItfName(itfPath);
		if (nextItfName == null) {
			// LocalRequest
			this.setValue(name, v);
			return;
		} 

		// TODO: support for remote set metric value method on monitor controller
		Object remoteMon = getRemoteMonitorController(nextItfName);
		if (remoteMon instanceof MonitorController) {
			//((MonitorController) remoteMon).setMetricValue(name, v, getNextItfPath(itfPath));
		} else if (remoteMon instanceof MonitorControllerMulticast) {
			//((MonitorControllerMulticast) remoteMon).setMetricValue(name, v, getNextItfPath(itfPath));
		} else {
			(new NoSuchInterfaceException(nextItfName)).printStackTrace();
		}
	}

	@Override
	public List<String> getMetricList(String itfPath) {
		String nextItfName = getNextItfName(itfPath);
		if (nextItfName == null) {
			// LocalRequest
			return getMetricList();
		}

		Object remoteMon = getRemoteMonitorController(nextItfName);

		if (remoteMon instanceof MonitorController) {
			return ((MonitorController) remoteMon).getMetricList(getNextItfPath(itfPath));
		} else if (remoteMon instanceof MonitorControllerMulticast) {
			//return ((MonitorControllerMulticast) remoteMon).getMetricList(getNextItfPath(itfPath));
			// TODO: support multicast
			return new ArrayList<String>();
		}
	
		(new NoSuchInterfaceException(nextItfName)).printStackTrace();
		return new ArrayList<String>();
	}

	// REMMOS EVENT
	
	@Override
	public void onEvent(RemmosEvent re) {
		// check all the metrics stored. If the metric is subscribed for the event, recalculate it.
		//System.out.println("EVENT ON " + hostComponent.getComponentParameters().getControllerDescription().getName() + ": " + re.getType());
		for(Map.Entry<String, Metric<?>> entry : metrics.entrySet()) {
			
			if(entry.getValue().isEventSubscriptionEnable() && entry.getValue().isSubscribedTo(re.getType())) {
	
				entry.getValue().calculate();
				
				if (metricEventListener != null) {
					metricEventListener.notifyMetricUpdate(entry.getKey());
				}
			}
		}
	}

	// BINDING CONTROLLER
	
	@Override
	public void bindFc(String name, Object itf) throws NoSuchInterfaceException {
		if(name.equals(RecordStore.ITF_NAME)) {
			records = (RecordStore) itf;
		} else if(name.equals(MetricEventListener.ITF_NAME)) {
			metricEventListener = (MetricEventListener) itf;
		} else if (name.endsWith("-external-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-external-" + MonitorController.ITF_NAME));
			if (itf instanceof MonitorController) {
				externalMonitors.put(realName, (MonitorController) itf);
			} else {
				externalMonitorsMulticast.put(realName, (MonitorControllerMulticast) itf);
			}
		} else if (name.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-internal-" + MonitorController.ITF_NAME));
			internalMonitors.put(realName, (MonitorController) itf);
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}

	@Override
	public String[] listFc() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(RecordStore.ITF_NAME);
		list.add(MetricEventListener.ITF_NAME);
		for (String name : externalMonitors.keySet()) list.add(name + "-external-" + MonitorController.ITF_NAME);
		for (String name : externalMonitorsMulticast.keySet()) list.add(name + "-external-" + MonitorController.ITF_NAME);
		for (String name : internalMonitors.keySet()) list.add(name + "-internal-" + MonitorController.ITF_NAME);
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Object lookupFc(String name) throws NoSuchInterfaceException {
		if(name.equals(RecordStore.ITF_NAME)) {
			return records;
		} else if(name.equals(MetricEventListener.ITF_NAME)) {
			return metricEventListener;
		} else if (name.endsWith("-external-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-external-" + MonitorController.ITF_NAME));
			if (externalMonitors.containsKey(realName)) {
				return externalMonitors.get(realName);
			}
			return externalMonitorsMulticast.get(realName);
		} else if (name.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-internal-" + MonitorController.ITF_NAME));
			return internalMonitors.get(realName);
		}	
		throw new NoSuchInterfaceException(name);
	}

	@Override
	public void unbindFc(String name) throws NoSuchInterfaceException {
		if(name.equals(RecordStore.ITF_NAME)) {
			records = null;
		} else if(name.equals(MetricEventListener.ITF_NAME)) {
			metricEventListener = null;
		} else if (name.endsWith("-external-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-external-" + MonitorController.ITF_NAME));
			if (externalMonitors.containsKey(realName)) {
				externalMonitors.remove(realName);
			} else if (externalMonitorsMulticast.containsKey(realName)) {
				externalMonitorsMulticast.remove(realName);
			} else {
				throw new NoSuchInterfaceException(name);
			}
		} else if (name.endsWith("-internal-" + MonitorController.ITF_NAME)) {
			String realName = name.substring(0, name.lastIndexOf("-internal-" + MonitorController.ITF_NAME));
			if (internalMonitors.containsKey(realName)) {
				internalMonitors.remove(realName);
			} else {
				throw new NoSuchInterfaceException(name);
			}
		} else {
			throw new NoSuchInterfaceException(name);
		}
	}
}
