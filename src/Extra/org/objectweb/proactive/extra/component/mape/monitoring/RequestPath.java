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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.extra.component.mape.monitoring.PathItem;
import org.objectweb.proactive.extra.component.mape.monitoring.records.ComponentRequestID;

/**
 * This class represent the path that a request follows through a series of components
 * 
 * @author cruz
 *
 */
public class RequestPath implements Serializable {

	/** The component request that this path is built for */
	private ComponentRequestID requestID;

	/** The pathItem representing the head of the tree for this request */
	private PathItem path;
	
	/** List of subtrees that have not been merged in the final tree */
	private ArrayList<PathItem> heads;
	
	/** List of pathItem that were added for already visited components, and that must be merged with the "heads" */
	private ArrayList<PathItem> incompletes;
	
	private HashSet<String> visited;
	
	/* If you delete me, I'll deadlock your work in obscure circumstances */
	public RequestPath() {
		
	}
	
	public RequestPath(ComponentRequestID requestID) {
		heads = new ArrayList<PathItem>();
		incompletes = new ArrayList<PathItem>();
		visited = new HashSet<String>();
		this.requestID = requestID;
	}
	
	public void init() {
		heads = new ArrayList<PathItem>();
		incompletes = new ArrayList<PathItem>();
		visited = new HashSet<String>();
	}
	
	
	public void addHead(PathItem newItem) {
		heads.add(newItem);
	}
	
	public void addIncomplete(PathItem newItem) {
		incompletes.add(newItem);
	}
	
	public PathItem getPath() {
		return path;
	}
	
	public int getSize() {
		return heads.size() + incompletes.size();
	}
	
	public void addVisited(String componentName) {
		visited.add(componentName);
	}
	
	public void addSetVisited(Set<String> moreVisited) {
		visited.addAll(moreVisited);
	}
	
	public boolean isVisited(String componentName) {
		return visited.contains(componentName);
	}
	
	public Set<String> getVisited() {
		return visited;
	}
	
	public List<PathItem> getHeads() {
		return heads;
	}
	
	public List<PathItem> getIncompletes() {
		return incompletes;
	}
	
	public void setPath(PathItem pi) {
		path = pi;
	}
	
	public ComponentRequestID getID() {
		return requestID;
	}
	
	public PathItem getHead(ComponentRequestID id) {
		PathItem resp = null;
		for(PathItem pi : heads) {
			if(pi.getID().equals(id)) {
				resp = pi;
			}
		}
		return resp;
	}
	
	public void removeHead(ComponentRequestID id) {
		PathItem remove = getHead(id);
		if(remove != null) {
			heads.remove(remove);
		}
	}
	
/*	public RequestPath sort() {
		System.out.println("DELETE ME!!!!!!!");
		RequestPathNew result = new RequestPathNew();
		RequestPathNew temp = new RequestPathNew();
		int size = heads.size();
		// the RequestPath has been constructed in a way that the last element inserted is the first of the path
		PathItem first = heads.get(size-1);
		result.add(first);
		ComponentRequestID current = heads.get(size-1).getCurrentID();
		
		while(result.getSize() < size) {
			// get all the requests received with the current ID (should be only one)
			for(PathItem pi: heads) {
				if(pi.getCurrentID() == pi.getParentID() && pi.getCurrentID() == current) {
					result.add(pi);
				}
			}
			int n=0;
			// put all the child requests of this one
			for(PathItem pi:heads) {
				if(pi.getCurrentID() != pi.getParentID() && pi.getCurrentID() == current) {
					temp.add(pi);
					n++;
				}
			}
			// sort the last n calls by startTime (they're done by the same component, so it should make sense)
			Collections.sort(temp.heads);
			// and add them to the list
			result.getPath().addAll(temp.getPath());
		}
		
		return null;
	}*/
}
