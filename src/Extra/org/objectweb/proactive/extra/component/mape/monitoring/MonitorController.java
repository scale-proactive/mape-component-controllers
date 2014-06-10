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
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extra.component.mape.monitoring.RequestPath;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.Metric;
import org.objectweb.proactive.extra.component.mape.monitoring.metrics.MetricValue;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extra.component.mape.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extra.component.mape.monitoring.records.OutgoingRequestRecord;


public interface MonitorController {

	public static final String MONITOR_CONTROLLER = "monitor-controller";
	public static final String ITF_NAME = "monitor-service-nf";

	void startGCMMonitoring();
	void stopGCMMonitoring();
	void resetGCMMonitoring();
	Boolean isGCMMonitoringStarted();
	Object getGCMStatistics(String itfName, String methodName) throws ProActiveRuntimeException;
	//Object getGCMStatistics(String itfName, String methodName, Class<?>[] parameterTypes) throws ProActiveRuntimeException;
	Map<String, Object> getAllGCMStatistics();

    //-------------------------------------------------------------------------
    // Extensions for the Monitoring Framework
    //
    
    /**
     * Get the list of all requests that have been entered this component
     * 
     */
    List<ComponentRequestID> getListOfIncomingRequestIDs();
    List<ComponentRequestID> getListOfOutgoingRequestIDs();
    
    /** 
     * Get the path followed by an specific request
     * 
     * @param id
     * @return
     */
    RequestPath getPathForID(ComponentRequestID id);
    RequestPath getPathForID(ComponentRequestID id, ComponentRequestID rootID, Set<String> visited);
    
    /**
     * Same from above, but with statistical information attached
     * 
     * @param id
     * @return
     */
    RequestPath getPathStatisticsForId(ComponentRequestID id);
    
    /**
     * Get the list of entries in the Incoming Request Log
     * @return
     */
    Map<ComponentRequestID, IncomingRequestRecord> getIncomingRequestLog();
    
    /**
     * Get the list of entries in the Outgoing Request Log
     * @return
     */
    Map<ComponentRequestID, OutgoingRequestRecord> getOutgoingRequestLog();
    
    List<String> getNotificationsReceived(); 
    
    String getMonitoredComponentName();

    // ---------------------------------------------------------------------------
    
    /**
     * Return the name of all the currently added metrics.
     * @return List of the names of the currently added metrics.
     */
    public List<String> getMetricList();

    /**
     * Return the name of all the currently added metrics.
     * @param itfPath	path to the component that is hosting of the metric. The path is build using the interfaces
     * who connect this component with the remote component hosting the metric.<br> Examples:<br>
     * itfPath = "/interface-name-1/interface-to-desired-component"<br>
     * itfPath = "/"
     * @return List of the names of the currently added metrics.
     */
    public List<String> getMetricList(String itfPath);

    /**
     * Add a metric on this monitor.
     * @param name		the name of the metric
     * @param metric	the metric
     */
    public void addMetric(String name, Metric<?> metric);

  
    /**
     * Executes the calculate() method for the desired metric.
     * @param name	the name of the metric
     * @return MetricValue wrapper containing the output of the calculate() method.
     */
    public MetricValue calculateMetric(String name);

    /**
     * Executes the calculate() method for the desired metric.
     * @param name		name of the metric
     * @param itfPath	path to the metric's owner component. See more details at {@link #getMetricList(String)}
     * @return MetricValue wrapper containing the output of the calculate() method.
     */
    public MetricValue calculateMetric(String name, String itfPath);
 

    /**
     * Gets the metric value of the named metric
     * @param name	the name of the metric
     * @return a MetricValue wrapper containing the value of the metric
     */
    public MetricValue getMetricValue(String name);
    
    /**
     * Gets the current value of a metric.
     * @param name 		name of the metric
     * @param itfPath	path to the metric's owner component. See more details at {@link #getMetricList(String)}
     * @return a MetricValue wrapper containing the value of the metric
     */
    public MetricValue getMetricValue(String name, String itfPath);


    /**
     * Set a new value for this metric
     * @param name		name of the metric
     * @param value		the new value of the metric
     * @return MetricValue wrapper containing the value of the metric
     */
    void setMetricValue(String name, Object value);

    /**
     * Set a new value for this metric
     * @param name		name of the metric
     * @param value		the new value of the metric
     * @param itfPath	path to the metric's owner component. See more details at {@link #getMetricList(String)}
     * @return MetricValue wrapper containing the value of the metric
     */
    public void setMetricValue(String name, Object value, String itfPath);

}
