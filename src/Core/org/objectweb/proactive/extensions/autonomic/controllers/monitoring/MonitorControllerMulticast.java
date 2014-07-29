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
import java.util.HashSet;
import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

/**
 * Multicast version of MonitorControl interface.
 * The interface just aggregates the results obtained from the existent call in the MonitorControl interface.
 * 
 * @author cruz
 *
 */

@ClassDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
public interface MonitorControllerMulticast {

	void startGCMMonitoring();
	void stopGCMMonitoring();
	void resetGCMMonitoring();
	List<Wrapper<Boolean>> isGCMMonitoringStarted();

    List<List<String>> getNotificationsReceived(); 
    List<String> getMonitoredComponentName();

    // ------------------------------------------------------------------------------

    public void setRecordStoreCapacity(int maxCapacity);
    
    // ------------------------------------------------------------------------------

    public List<Wrapper<String>> getMetricState(String metricName);
    public List<Wrapper<Boolean>> enableMetric(String metricName);
    public List<Wrapper<Boolean>> disableMetric(String metricName);

    public List<Wrapper<HashSet<String>>> getMetricList();
    public List<Wrapper<HashSet<String>>> getMetricList(String itfPath);

    public List<Wrapper<Boolean>> addMetric(String name, Metric<?> metric);
	public List<Wrapper<Boolean>> removeMetric(String metricName);

    public <T extends Serializable> List<Wrapper<T>> calculateMetric(String name);

    public <T extends Serializable> List<Wrapper<T>> calculateMetric(String name, String itfPath);
 
    public <T extends Serializable> List<Wrapper<T>> getMetricValue(String name);
    
    public <T extends Serializable> List<Wrapper<T>> getMetricValue(String name, String itfPath);

    public void setMetricValue(String name, Object value);

    public void setMetricValue(String name, Object value, String itfPath);
}
