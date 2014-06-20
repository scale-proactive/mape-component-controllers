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
package org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetricsLibrary {

	Map<String, Class<?>> library;
	
	private static MetricsLibrary instance = null;
	
	private MetricsLibrary() {
		library = new HashMap<String,Class<?>>();
		library.put("avgInc", AvgRespTimeIncomingMetric.class);
		library.put("avgOut", AvgRespTimeOutgoingMetric.class);
		library.put("avgIncItf", AvgRespTimePerItfIncomingMetric.class);
		library.put("avgOutItf", AvgRespTimePerItfOutgoingMetric.class);
		library.put("maxInc", MaxRespTimeIncomingMetric.class);
		library.put("maxOut", MaxRespTimeOutgoingMetric.class);
		library.put("minInc", MinRespTimeIncomingMetric.class);
		library.put("minOut", MinRespTimeOutgoingMetric.class);
		library.put("maxIncItf", MaxRespTimePerItfIncomingMetric.class);
		library.put("maxOutItf", MaxRespTimePerItfOutgoingMetric.class);
		library.put("minIncItf", MinRespTimePerItfIncomingMetric.class);
		library.put("minOutItf", MinRespTimePerItfOutgoingMetric.class);
	}
	
	/** Singleton
	 * 
	 * @return
	 */
	public static MetricsLibrary getInstance() {
		if(instance == null) {
			instance = new MetricsLibrary();
		}
		return instance;
	}
	
	/** 
	 * Gets the class from the library, and returns an instance of that class
	 * 
	 * @param name
	 * @return
	 */
	public Metric<?> getMetric(String name) {
		Metric<?> metric = null;
		if(library.containsKey(name)) {
			Class<?> metricClass = library.get(name);

			try {
				metric = (Metric<?>) metricClass.getConstructor().newInstance();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return metric;
	}
	
	/**
	 * Gets the list of available metrics from the library
	 * @return
	 */
	public Set<String> getMetricList() {
		return library.keySet();
	}
	
	
}
