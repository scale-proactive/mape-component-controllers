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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.component.mape.monitoring.PathItem;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;

/**
 * This class represents an element in the path of a request.
 * It should, at least, include:
 * 		- Component Name
 * 		- Interface Name
 * 		- Method Name
 * Optionally, it can include statistics for each component 
 * 
 * @author cruz
 *
 */
public class PathItem implements Serializable, Comparable<PathItem> {

	String componentName;  // the component called (this component)
	String interfaceName;  // the interface called (a server interface of this component)
	String methodName;     // the method called

	ComponentRequestID current;  // id of the received request
	
	long sendTime;      // when the caller sent the request to this component
	long replyRecvTime; // when the caller received the response from this component
	long recvTime;      // when this component received the request
	long replySentTime; // when this component sent the reply
	
	List<ComponentRequestID> childrenID;            // the ID of all the children of this request
	Map<ComponentRequestID, PathItem> children; // the list of children of this request
	
	public PathItem(ComponentRequestID current, String c, String i, String m) {
		this.current = current;
		this.componentName = c;
		this.interfaceName = i;
		this.methodName = m;
		this.childrenID = new ArrayList<ComponentRequestID>();
		this.children = new HashMap<ComponentRequestID, PathItem>();
	}

	public String toString() {
		return "("+current+") "+componentName+"."+interfaceName+"."+methodName+": \t"+"client: "+ (replyRecvTime-sendTime) + "\t"+"server: "+ (replySentTime-recvTime);
	}
	
	public ComponentRequestID getID() {
		return current;
	}
	
	
	public void setSendTime(long t) {
		sendTime = t;
	}
	
	public void setReplyRecvTime(long t) {
		replyRecvTime = t;
	}

	public void setRecvTime(long t) {
		recvTime = t;
	}
	
	public void setReplySentTime(long t) {
		replySentTime = t;
	}
	
	public long getSendTime() {
		return sendTime;
	}
	public long getRecvTime() {
		return recvTime;
	}
	
	public long getReplySentTime() {
		return replySentTime;
	}
	
	public void addChildID(ComponentRequestID childID) {
		childrenID.add(childID);
	}
	
	public void addChild(ComponentRequestID childID, PathItem child) {
		children.put(childID, child);
	}
	
	public List<ComponentRequestID> getChildrenID() {
		return childrenID;
	}
	
	public Map<ComponentRequestID, PathItem> getChildren() {
		return children;
	}
	
	public void setChildren(Map<ComponentRequestID, PathItem> map) {
		children = map;
	}
	
	public void setChildrenID(List<ComponentRequestID> list) {
		childrenID = list;
	}

	/**
	 * Partial order.
	 * It compares two pathItem supposed to come from the same Component, and determines which one "happened before".
	 * If pathItem from different components are compared the result is not guaranteed.
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(PathItem o) {
		return (int)(this.sendTime - o.getSendTime());
		//return (int)(this.recvTime - o.getRecvTime());
	}
	
}
