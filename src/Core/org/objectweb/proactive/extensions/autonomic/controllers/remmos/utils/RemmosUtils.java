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
package org.objectweb.proactive.extensions.autonomic.controllers.remmos.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAComponentType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.PathItem;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.RequestPath;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.OutgoingRequestRecord;

public class RemmosUtils {


	/** 
	 * Describe the F and NF interfaces of a PAComponent 
	 * @param comp
	 */
	public static void describeComponent(Component comp) {

		if(!(comp instanceof PAComponent)) {
			System.out.println("Component is not an instance of PAComponent");
			return;
		}
		PAComponent pacomp = (PAComponent) comp;
		System.out.println("Component: " + pacomp.getComponentParameters().getName());
		InterfaceType[] itfTypes = pacomp.getComponentParameters().getInterfaceTypes();
		itfTypes = pacomp.getComponentParameters().getComponentType().getFcInterfaceTypes();
		for(int i=0; i<itfTypes.length; i++) {
			System.out.println("  Interface: " + (((PAGCMInterfaceType)itfTypes[i]).isFcClientItf() ? " client ":" server ")
					+ (((PAGCMInterfaceType)itfTypes[i]).isFcOptionalItf() ? " optional  ":" mandatory ")
					+ (((PAGCMInterfaceType)itfTypes[i]).isInternal() ? " internal  ":" external  ")
					+ itfTypes[i].getFcItfName() );
		}

		System.out.println();
		//InterfaceType[] itfNFTypes = pacomp.getComponentParameters().getComponentNFType().getFcInterfaceTypes();
		InterfaceType[] itfNFTypes = ((PAComponentType) pacomp.getComponentParameters().getComponentType()).getNfFcInterfaceTypes();
		for(int i=0; i<itfNFTypes.length; i++) {
			System.out.println("  Interface (NF): " + (((PAGCMInterfaceType)itfNFTypes[i]).isFcClientItf() ? " client ":" server ")
					+ (((PAGCMInterfaceType)itfNFTypes[i]).isFcOptionalItf() ? " optional  ":" mandatory ")
					+ (((PAGCMInterfaceType)itfNFTypes[i]).isInternal() ? " internal  ":" external  ")
					+ itfNFTypes[i].getFcItfName() );
		}
		System.out.println();

		/*
		Object[] intfs = pacomp.getFcInterfaces();
		for(int i=0; i<intfs.length; i++) {
			System.out.println(i+":"+ ((Interface)intfs[i]).getFcItfName() );
		}*/

	}
	
	//-----------------------------------------------------------------------------------------------------------------------
	// only for testing
	public static void displayLogs(Component comp) {
		String hostComponentName = null;
		
		try {
			hostComponentName = Fractal.getNameController(comp).getFcName();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		Map<ComponentRequestID, IncomingRequestRecord> requestLog = null;
		Map<ComponentRequestID, OutgoingRequestRecord> callLog = null;
		
		/*
		try {
			requestLog = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getIncomingRequestLog();
			callLog = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getOutgoingRequestLog();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}*/
		
		System.out.println("===================== Component ["+ hostComponentName +"] =====================");
		System.out.println("===================== Incoming Log ===============================");
		displayRequestLog(requestLog);
		System.out.println("======================Outgoing Log ================================");
		displayCallLog(callLog);
		System.out.println("==================================================================");
		System.out.println();
	}

    public static void displayRequestLog(Map<ComponentRequestID, IncomingRequestRecord> requestLog) {
    	if(requestLog == null)
    		return;    	
    	Iterator<ComponentRequestID> i = requestLog.keySet().iterator();
    	ComponentRequestID crID;
    	IncomingRequestRecord rs;
    	while(i.hasNext()) {
    		crID = i.next();
    		rs = requestLog.get(crID);
    		System.out.println("ID: "+ crID + " Sender: "+ rs.getCallerComponent() +
    				" Call: "+ rs.getCalledComponent() + "." + rs.getInterfaceName()+"."+rs.getMethodName() +
    				" Arr: " + rs.getArrivalTime() + " Serv: " + rs.getServingStartTime() + " Repl: " + rs.getReplyTime() +
    				" WQ: " + (rs.getServingStartTime()-rs.getArrivalTime()) + 
    				" SRV: " + (rs.getReplyTime()-rs.getServingStartTime()) +
    				" TOT: "+ (rs.getReplyTime() - rs.getArrivalTime()));	
    	}
    }

	public static void displayCallLog(Map<ComponentRequestID, OutgoingRequestRecord> callLog) {
		if(callLog == null)
			return;
    	Iterator<ComponentRequestID> i = callLog.keySet().iterator();
    	ComponentRequestID crID;
    	OutgoingRequestRecord cs;
    	long wbnTime = 0;
    	Long start, stop;
    	Map<Long,Long> wbnStart;
    	Map<Long,Long> wbnStop;
    	while(i.hasNext()) {
    		crID = i.next();
    		cs = callLog.get(crID);
    		
    		//calculate WbN time
    		wbnStart = cs.getWbnStartTime();
    		wbnStop = cs.getWbnStopTime();
    		
    		for(Long id: wbnStart.keySet()) {
    			if(wbnStop.containsKey(id)) {
    				start = wbnStart.get(id);
    				stop = wbnStop.get(id);
    				if(stop.longValue() > cs.getReplyReceptionTime()) {
    					stop = cs.getReplyReceptionTime();
    				}
    				wbnTime += (stop - start);
    			}
    		}
    		
    		System.out.println("Parent: "+ cs.getParentID() + " ID: "+ crID + 
    				" Call: "+ cs.getCalledComponent() + "." + cs.getInterfaceName()+"."+cs.getMethodName()+ 
    				" SentTime: " + cs.getSentTime() + 
    				" RealReplyReceivedTime: " + cs.getReplyReceptionTime() +
    				" WbN: " + wbnTime + 
    				" SRV: " + (cs.getReplyReceptionTime() - cs.getSentTime()));
    	}
    }

	public static void displayNotifs(Component comp) {
		String hostComponentName = null;
		try {
			hostComponentName = Fractal.getNameController(comp).getFcName();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		List<String> notifs = null;
		
		try {
			notifs = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getNotificationsReceived();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		
		System.out.println("===================== Component ["+ hostComponentName +"] =====================");
		System.out.println("===================== Notifications ================================");
		for(String s : notifs) {
			System.out.println(s);
		}
		System.out.println("==================================================================");
		System.out.println();
		
	}
	
	public static void displayReqs(Component comp) {
		String hostComponentName = null;
		try {
			hostComponentName = Fractal.getNameController(comp).getFcName();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		List<ComponentRequestID> requests = null;
		/*
		try {
			//requests = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getListOfIncomingRequestIDs();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}*/
		System.out.println("===================== Component ["+ hostComponentName +"] =====================");
		System.out.println("===================== Requests Received ============================");
		for(ComponentRequestID r: requests) {
			System.out.println(r);
		}
		System.out.println("==================================================================");
		System.out.println();
		
	}
	
	public static void displayCalls(Component comp) {
		String hostComponentName = null;
		try {
			hostComponentName = Fractal.getNameController(comp).getFcName();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		List<ComponentRequestID> calls = null;
		/*
		try {
			//calls = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getListOfOutgoingRequestIDs();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		*/
		System.out.println("===================== Component ["+ hostComponentName +"] =====================");
		System.out.println("===================== Calls Sent =================================");
		for(ComponentRequestID r: calls) {
			System.out.println(r);
		}
		System.out.println("==================================================================");
		System.out.println();
		
	}
	
	public static void displayPath(RequestPath rp) {
		
		PathItem pi = rp.getPath();
		System.out.println("Request Path from request "+ pi.getID());
		displayPath(pi,0);
		
		/*List<PathItem> paths = rp.getPath();
		System.out.println("Request Path ("+ paths.size()+")");
		for(PathItem path:paths) {
			System.out.println("*" + path.toString());
		}*/
		/*System.out.println("--- Subtrees");
		for(PathItem subtree : rp.getHeads()) {
			System.out.println("--- Request Path from subtree "+ subtree.getID());	
			displayPath(subtree,1);
		}
		System.out.println("--- Incomplete entries");
		for(PathItem incomplete : rp.getIncompletes()) {
			System.out.println("    "+ incomplete.toString());
		}*/
	}
	
	private static void displayPath(PathItem pi, int level) {
		
		// print this
		System.out.print("* ");
		for(int i=0;i<level;i++) {
			System.out.print("   ");
		}
		System.out.println(pi.toString());
		
		// print children
		// TODO order the children before
		List<PathItem> children = new ArrayList<PathItem>();
		children.addAll(pi.getChildren().values());
		Collections.sort(children);		
		for(PathItem child : children) {
			displayPath(child,level+1);
		}
		
	}
	
	public static void displayMetrics(Component comp) throws NoSuchInterfaceException {
		String hostComponentName = null;
		try {
			hostComponentName = Fractal.getNameController(comp).getFcName();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		HashSet<String> metricSet = null;
		try {
			metricSet = ((MonitorController)comp.getFcInterface(Constants.MONITOR_CONTROLLER)).getMetricList().getValue();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		System.out.println("Metrics in component ["+ hostComponentName +"]");
		System.out.print("   ");
		for(String s : metricSet) {
			System.out.print(s+" ");
		}
		System.out.println();
	}
	
	public static String toSeg(long nseg) {
		return ""+((double)nseg)/1000000000.0;
	}
}
