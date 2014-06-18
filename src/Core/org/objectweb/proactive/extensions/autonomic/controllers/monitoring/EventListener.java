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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.tags.tag.CMTag;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.FutureNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.EventControl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEvent;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventType;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.ComponentRequestID;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.IncomingRequestRecord;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.OutgoingRequestRecord;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.records.RecordType;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

/**
 * Event Listener component for the Monitoring Framework
 * 
 * This NF Component subscribes to all the interesting events that are required by the monitoring activity.
 * Each time an event is received, a record is created an sent to the Log Store.
 * The specific behaviour regarding reading/updating/writing a record is dependent on the middleware.
 * This component works as a sensor of events related to the F component it belongs to.
 * 
 * This version is adapted for the GCM/ProActive case, and considers asynchronism of requests and futures.
 * 
 * @author cruz
 *
 */
public class EventListener extends AbstractPAComponentController implements NotificationListener, BindingController, EventControl {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_MONITORING);

	private RecordStore recordStore;
	private RemmosEventListener metricsStore;
	private String[] itfs = { RecordStore.ITF_NAME, RemmosEventListener.ITF_NAME };

	/** Connection to a ProActive BodyWrapperMBean 
	 *  In real terms, this connection should be to a (still inexistent) ComponentWrapperMBean. */
	private JMXNotificationManager jmxNotificationManager = null;

	// JMX data for locating the MBean
	private UniqueID monitoredBodyID;
	private String runtimeURL;
	
	/** Name of the component monitored by this listener */
	private String monitoredComponentName;
	
	/** This is just for debugging. I want to know if this guy received all the notifications I sent */
	private List<String> notificationStore = null;
	
	private boolean started = false;
	
	public EventListener() {
		super();
		
	}
	/*
	public void init() {
		logger.debug("[EventListener] Init ...");
		jmxNotificationManager = JMXNotificationManager.getInstance();
	}*/

	public void start() {
		if(!started) {
			logger.debug("[EventListener] Starting Monitoring for component ["+ monitoredComponentName + "]");
			if(notificationStore == null) {
				notificationStore = new ArrayList<String>();
			}
			try {
            	// subscribes this class as a listener to JMX notifications from the BodyWrapperMBean
            	// from the AO implementing this Component
                if(jmxNotificationManager == null) {
                	jmxNotificationManager = JMXNotificationManager.getInstance();
                }
				jmxNotificationManager.subscribe(FactoryName.createActiveObjectName(monitoredBodyID), this, FactoryName.getCompleteUrl(runtimeURL));
            } catch (IOException e) {
                throw new ProActiveRuntimeException("JMX subscription for the MonitorController has failed", e);
            }
            started = true;
            logger.debug("[EventListener] Monitoring Started for component ["+ monitoredComponentName + "] "+ " bodyID: "+ monitoredBodyID + " @ "+ runtimeURL);
		}
	}
	
	
	public void setBodyToMonitor(UniqueID objectID, String runtimeURL, String componentName) {
		this.monitoredBodyID = objectID;
		this.runtimeURL = runtimeURL;
		this.monitoredComponentName = componentName;
	}

	// MAIN WORK of the collector. Highly dependent on the middleware.
	@Override
	public void handleNotification(Notification notification, Object handback) {
		String type = notification.getType();
		// Handling for REQUEST RECEIVED
		if (type.equals(NotificationType.requestReceived)) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            logger.debug(
            		" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][requestRecv] " + //"From:" + data.getSource() +
            		//" To:"+ data.getDestination() +
            		" Method:" + data.getMethodName() +
            		" SeqNumber: " + data.getSequenceNumber() +
            		" NotifSeqNbr: " + notification.getSequenceNumber() +
            		" Tags: " + data.getTags());
            notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][requestRecv] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
            processRequestReceived(notification);
        }
		// Handling for SERVING STARTED
		else if (type.equals(NotificationType.servingStarted)) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            processServingStarted(notification);
        } 
		// Handling for REPLY SENT
        else if (type.equals(NotificationType.replySent)) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            logger.debug(
            		" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][replySent  ] " + //From:" + data.getSource() +
            		//" To:"+ data.getDestination() +
            		" Method:" + data.getMethodName() +
            		" SeqNumber: " + data.getSequenceNumber() +
            		" NotifSeqNbr: " + notification.getSequenceNumber() +
            		" Tags: " + data.getTags());
            //processReplySent(notification);
        }
		// TODO Handling for VOID REQUEST SERVED
        else if (type.equals(NotificationType.voidRequestServed)) {
            // RequestNotificationData data = (RequestNotificationData) notification.getUserData();
//            logger.debug("["+componentName+"][voidReqServ] From:" + data.getSource() +
//            		" To:"+ data.getDestination() +
//            		" Method:" + data.getMethodName() +
//            		" SeqNumber: " + data.getSequenceNumber() +
//            		" Timestamp: " + notification.getTimeStamp() +
//            		" NotifSeqNbr: " + notification.getSequenceNumber() +
//            		" Tags: " + data.getTags());
            
        }
		// Handling for REQUEST SENT
        else if (type.equals(NotificationType.requestSent)) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            logger.debug(
            		" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][requestSent] " + //"From:" + data.getSource() +
//            		" To:"+ data.getDestination() +
            		" Method:" + data.getMethodName() +
            		" SeqNumber: " + data.getSequenceNumber() +
            		" NotifSeqNbr: " + notification.getSequenceNumber() +
            		" Tags: " + data.getTags());
            notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][requestSent] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
            processRequestSent(notification);
        } 
		// TODO Handling for REPLY RECEIVED
		// For the moment I'm more interested in the Real Reply Received notification
        else if (type.equals(NotificationType.replyReceived)) {
            RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            logger.debug(
            		" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][replyRecv  ] " + //From:" + data.getSource() +
//            		" To:"+ data.getDestination() +
            		" Method:" + data.getMethodName() +
            		" SeqNumber: " + data.getSequenceNumber() +
            		" NotifSeqNbr: " + notification.getSequenceNumber() +
            		" Tags: " + data.getTags());
        }
		// TODO Handling for RECEIVED FUTURE RESULT
		// This only happen when the thread is doing a WbN, and receives the Future update.
		// If there is no WbN, then this does not happen
		// see ... FutureProxy.waitfor()
        else if(type.equals(NotificationType.receivedFutureResult)) {
        	FutureNotificationData data = (FutureNotificationData) notification.getUserData();
        	logger.debug(
        			" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][futureUpdt ] " + //From:" + data.getSource() +
//            		" To:"+ data.getDestination() +
//            		" Method:" + data.getMethodName() +
//            		" SeqNumber: " + data.getSequenceNumber() +
//            		" NotifSeqNbr: " + notification.getSequenceNumber() +
//            		" Tags: " + data.getTags()
            		" Creator" + data.getCreatorID() +
            		" Waiter" + data.getBodyID()
            		);
        }
		// Handling for REAL REPLY RECEIVED
		// This happens when the reply received does not have any more Futures (all the reply data is available)
        else if(type.equals(NotificationType.realReplyReceived)) {
        	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
            logger.debug(
            		" Timestamp: " + notification.getTimeStamp() +
            		"["+monitoredComponentName+"][RRreplyRecv] " + //From:" + data.getSource() +
//            		" To:"+ data.getDestination() +
            		" Method:" + data.getMethodName() +
            		" SeqNumber: " + data.getSequenceNumber() +
            		" NotifSeqNbr: " + notification.getSequenceNumber() +
            		" Tags: " + data.getTags());
            notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][RRreplyRecv] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
            processRealReplyReceived(notification);
        }
		// TODO
		// This notification is "new" ... because the existent "waitByNecessity" notification has a "FutureNotificationData" attached,
		// which doesn't include all the information I need to find the appropriate entry in the logs.
        else if(type.equals(NotificationType.requestWbN)) {
        	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
        	logger.debug(
        			" Timestamp: " + notification.getTimeStamp() +
        			"["+monitoredComponentName+"][WaitByNeces] " + //From:" + data.getSource() +
        			//                		" To:"+ data.getDestination() +
        			" Method:" + data.getMethodName() +
        			" SeqNumber: " + data.getSequenceNumber() +
        			" NotifSeqNbr: " + notification.getSequenceNumber() +
        			" Tags: " + data.getTags());
        	notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][WaitByNeces] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
            processWaitByNecessity(notification);
        	
        }
		// TODO
		// This notification is "new" ... 
        else if(type.equals(NotificationType.replyAC)) {
        	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
        	logger.debug(
        			" Timestamp: " + notification.getTimeStamp() +
        			"["+monitoredComponentName+"][replySentAC] " + //From:" + data.getSource() +
        			//                		" To:"+ data.getDestination() +
        			" Method:" + data.getMethodName() +
        			" SeqNumber: " + data.getSequenceNumber() +
        			" NotifSeqNbr: " + notification.getSequenceNumber() +
        			" Tags: " + data.getTags());
        	//notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][replySentAC] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
        	//processWaitByNecessity(notification);
        }
		// TODO
		// This notification is "new" ... 
        else if(type.equals(NotificationType.realReplySent)) {
        	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
        	logger.debug(
        			" Timestamp: " + notification.getTimeStamp() +
        			"["+monitoredComponentName+"][RRreplySent] " + //From:" + data.getSource() +
        			//                		" To:"+ data.getDestination() +
        			" Method:" + data.getMethodName() +
        			" SeqNumber: " + data.getSequenceNumber() +
        			" NotifSeqNbr: " + notification.getSequenceNumber() +
        			" Tags: " + data.getTags());
        	notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][RRreplySent] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
        	processRealReplySent(notification);
        	
        }
		// TODO
		// This notification is "new" ... 
        else if(type.equals(NotificationType.requestFutureUpdate)) {
        	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
        	logger.debug(
        			" Timestamp: " + notification.getTimeStamp() +
        			"["+monitoredComponentName+"][FutureUpdat] " + //From:" + data.getSource() +
        			//                		" To:"+ data.getDestination() +
        			" Method:" + data.getMethodName() +
        			" SeqNumber: " + data.getSequenceNumber() +
        			" NotifSeqNbr: " + notification.getSequenceNumber() +
        			" Tags: " + data.getTags());
        	notificationStore.add(notification.getTimeStamp()+" ["+monitoredComponentName+"][FutureUpdat] Seq["+data.getSequenceNumber()+"] Tags "+data.getTags());
        	processFutureUpdate(notification);
        	
        }
        else if (type.equals(NotificationType.setOfNotifications)) {
            @SuppressWarnings("unchecked")
            ConcurrentLinkedQueue<Notification> notificationsList = (ConcurrentLinkedQueue<Notification>) notification
                    .getUserData();
            for (Iterator<Notification> iterator = notificationsList.iterator(); iterator.hasNext();) {
                handleNotification(iterator.next(), handback);
            }
        }
		else {
			logger.debug(" Other: ["+ type +"]");
		}
		
	}
	
	/**
     * Process the requestReceived notification.
     * Stores the request data in the requestLog.
     * TODO It's possible that some other notifications (servingStarted, replySent,...) be received before
     *      this one. In that case, when this entry is added to the requestLog, the other ones must be considered
     * @param notification
     */
    private void processRequestReceived(Notification notification) {
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	String cmTag = extractCMTag(data);
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String sourceName = cmTagFields[2];
    	String destName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	if(interfaceName.equals("-")) {
    		return;
    	}
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	
    	boolean newRecord = false;
    	
    	// checks if the request data has already been entered in the map
    	IncomingRequestRecord rs = recordStore.getIncomingRequestRecord(current).get();
    	if (rs != null) {
    		rs.setArrivalTime(notification.getTimeStamp());
    	} else {
    		rs = new IncomingRequestRecord(current, sourceName, destName, interfaceName, methodName, notification.getTimeStamp(), root);
    	}
    	
    	recordStore.update(rs);
   
    	if (newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if (rs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
	/**
     * Process the servingStarted notification.
     * Updates the request data in the requestLog.
     * TODO It's possible that some other notifications (for example, replySent) be received before
     *      this one. In that case, when this entry is added to the requestLog, the other ones must be considered
     *      Note that, normally, requestReceived SHOULD have been received before this one (but it may also happen that this one arrives before)
     * @param notification
     */
    private void processServingStarted(Notification notification) {
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	String cmTag = extractCMTag(data);
    	// the request may not have a CMTag, when the request was invoked directly on the component
    	// (not from another component).
    	// In that case no "requestReceived" notification was generated either
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String sourceName = cmTagFields[2];
    	String destName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	IncomingRequestRecord rs = recordStore.getIncomingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (rs == null) {
    		newRecord = true;
    		// The data should be added (without the arrival time), and be updated later,
        	// when the corresponding requestReceived notification be processed
    		rs = new IncomingRequestRecord(current, sourceName, destName, interfaceName, methodName, 0, root);
    		rs.setServingStartTime(notification.getTimeStamp());
    	} else {
    		rs.setServingStartTime(notification.getTimeStamp());
    	}

    	
    	recordStore.update(rs);
    	if (newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if (rs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Process the replySent notification.
     * This notification is generated when a reply is sent to the caller component as an answer to a non-void request 
     * @param notification
     */
    private void processReplySent(Notification notification) {
    	// modifies the request in the requestLog, to add the time at which the reply was sent
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	String cmTag = extractCMTag(data);
    	// the request may not have a CMTag, when the request was invoked directly on the component
    	// (not from another component).
    	// In that case no "requestReceived" notification was generated either
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String sourceName = cmTagFields[2];
    	String destName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	IncomingRequestRecord rs = recordStore.getIncomingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (rs == null) {
    		newRecord = true;
    		rs = new IncomingRequestRecord(current, sourceName, destName, interfaceName, methodName, 0, root);
    		rs.setReplyTime(notification.getTimeStamp());
    	} else {
    		rs.setReplyTime(notification.getTimeStamp());
    	}

    	recordStore.update(rs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(rs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	
    }
    
    /**
     * Process the requestSent notification.
     * Stores the new call in the callLog.
     * TODO It's possible that some other notifications (replyReceived, realReplyReceived...) be received before
     *      this one. In that case, when this entry is added to the requestLog, the other ones must be considered
     * @param notification
     */
    private void processRequestSent(Notification notification) {
    	// adds the request to callLog
    	// this should be the first notification regarding this request, anyway the order is not guaranteed,
    	// so care must be taken when processing the corresponding "replyReceived"
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	// TODO: Move this processing to the CMTag part
    	String cmTag = extractCMTag(data);
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String destComponentName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	if(interfaceName.equals("-")) {
    		return;
    	}
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	OutgoingRequestRecord cs = recordStore.getOutgoingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (cs == null) {
    		newRecord = true;
    		cs = new OutgoingRequestRecord(current, parent, destComponentName, interfaceName, methodName,
    				notification.getTimeStamp(), false, root);
    	} else {
    		cs.setSentTime(notification.getTimeStamp());
    	}
    	
    	recordStore.update(cs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(cs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Process the realReplyReceived notification.
     * Updates the call record stored in the callLog.
     * This notification is sent when the definitive answer of a reply (i.e. a reply that does not involve futures) is received,
     * allowing to determine the time that the request took to be effectively served.
     * TODO It's possible that some other notifications (replyReceived, realReplyReceived...) be received before
     *      this one. In that case, when this entry is added to the requestLog, the other ones must be considered
     * @param notification
     */
    private void processRealReplyReceived(Notification notification) {
    	// modifies the request in the callLog, to add the time at which the final reply was received
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	// TODO: Move this processing to the CMTag part
    	String cmTag = extractCMTag(data);
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String destComponentName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	if(interfaceName.equals("-")) {
    		return;
    	}
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	OutgoingRequestRecord cs = recordStore.getOutgoingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (cs == null) {
    		newRecord = true;
    		cs = new OutgoingRequestRecord(current, parent, destComponentName, interfaceName, methodName, 0, false, root);
    		cs.setReplyReceptionTime(notification.getTimeStamp());
    	} else {
    		cs.setReplyReceptionTime(notification.getTimeStamp());
    	}
  
    	recordStore.update(cs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(cs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Process the waitByNecessity notification.
     * Sets the time when this request (?) became blocked doing wait by necessity, so it possible to compute the WaitByNecessity time
     * Think: there maybe a possibly mistake (conceptually), if the Body is unblocked and then blocks in another WbN (I'm counting only from the first one)
     * @param notification
     */
    private void processWaitByNecessity(Notification notification) {
    	// modifies the request in the callLog, to add the time at which the WbN happened
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	// TODO: Move this processing to the CMTag part
    	String cmTag = extractCMTag(data);
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String destComponentName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	if(interfaceName.equals("-")) {
    		return;
    	}
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	OutgoingRequestRecord cs = recordStore.getOutgoingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (cs == null) {
    		newRecord = true;
    		cs = new OutgoingRequestRecord(current, parent, destComponentName, interfaceName, methodName, 0, false, root);
    		cs.addWbnStartTime(data.getSequenceNumber(), notification.getTimeStamp());
    	} else {
    		cs.addWbnStartTime(data.getSequenceNumber(), notification.getTimeStamp());
    	}
    	
    	recordStore.update(cs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(cs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Process the realReplySent notification.
     * This notification is generated when the final reply is sent to the caller component as an answer to a non-void request.
     * It can happen immediately from BodyImpl.ActiveLocalBodyStrategy.serveInternal(Request),
     *    or well from an AC, in FuturePool.ACService.doAutomaticContinuation() 
     * @param notification
     */
    private void processRealReplySent(Notification notification) {
    	// modifies the request in the requestLog, to add the time at which the reply was sent
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	String cmTag = extractCMTag(data);
    	// the request may not have a CMTag, when the request was invoked directly on the component
    	// (not from another component).
    	// In that case no "requestReceived" notification was generated either
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String sourceName = cmTagFields[2];
    	String destName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	
    	IncomingRequestRecord rs = recordStore.getIncomingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (rs == null) {
    		newRecord = true;
    		rs = new IncomingRequestRecord(current, sourceName, destName, interfaceName, methodName, 0, root);
    		rs.setReplyTime(notification.getTimeStamp());
    	} else {
    		rs.setReplyTime(notification.getTimeStamp());
    	}

    	recordStore.update(rs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(rs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.INCOMING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Process the requestFutureUpdate notification.
     * This notification is generated when an update is done on a Future that is been waited for.
     * It indicate that the WbN on this Future has finished.
     * If no one was waiting for this Future, this notification is never sent (for example,
     * it could have an AC that will be done, but it is not waiting)
     * @param notification
     */
    private void processFutureUpdate(Notification notification) {
    	// modifies the request in the callLog, to add the time at which the WbN happened
    	RequestNotificationData data = (RequestNotificationData) notification.getUserData();
    	// TODO: Move this processing to the CMTag part
    	String cmTag = extractCMTag(data);
    	if(cmTag == null) {
    		return;
    	}
    	String[] cmTagFields = cmTag.split("::");
    	ComponentRequestID parent = new ComponentRequestID(Long.parseLong(cmTagFields[0]));
    	ComponentRequestID current = new ComponentRequestID(Long.parseLong(cmTagFields[1]));
    	String destComponentName = cmTagFields[3];
    	String interfaceName = cmTagFields[4];
    	String methodName = cmTagFields[5];
    	ComponentRequestID root = new ComponentRequestID(Long.parseLong(cmTagFields[6]));
    	if(interfaceName.equals("-")) {
    		return;
    	}
    	
    	OutgoingRequestRecord cs = recordStore.getOutgoingRequestRecord(current).get();
    	boolean newRecord = false;
    	
    	if (cs == null) {
    		newRecord = true;
    		cs = new OutgoingRequestRecord(current, parent, destComponentName, interfaceName, methodName, 0, false, root);
    		cs.addWbnStopTime(data.getSequenceNumber(), notification.getTimeStamp());
    	} else {
    		cs.addWbnStopTime(data.getSequenceNumber(), notification.getTimeStamp());
    	}
    	
    	recordStore.update(cs);
    	if(newRecord) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.NEW_OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    	if(cs.isFinished()) {
    		notifyMetrics(new RemmosEvent(RemmosEventType.OUTGOING_REQUEST_EVENT, notification.getTimeStamp()));
    	}
    }
    
    /**
     * Extracts the CMTag from the complete Tag string (which can include several tags)
     * @param data
     * @return
     */
    private String extractCMTag(RequestNotificationData data) {
    	String tagString = data.getTags();
    	// The ? is a "reluctant" quantifier, to make the .* to match the smallest possible string.
    	Pattern pattern = Pattern.compile("\\[TAG\\](.*?)\\[DATA\\](.*?)\\[END\\]");
    	Pattern inner = Pattern.compile("\\[(.*?)\\]");
    	Matcher match = pattern.matcher(tagString);
    	String currentTag;
    	String currentFields[];
    	while(match.find()) {
    		currentTag = match.group();
    		currentFields = inner.split(currentTag);
    		if(currentFields[1].equals(CMTag.IDENTIFIER)) {
    			// This is the CMTag. Process it
    			return currentFields[2];
    		}
    	}
    	
    	return null;
    }
    
    private void notifyMetrics(RemmosEvent re) {
    	metricsStore.onEvent(re);
    }
    
    public List<String> getNotifications() {
    	return notificationStore;
    }
    
    public void reset() {
    	notificationStore.clear();
    }

	@Override
	public void bindFc(String cItf, Object sItf) throws NoSuchInterfaceException {
		if(cItf.equals(RecordStore.ITF_NAME)) {
			recordStore = (RecordStore) sItf;
			return;
		}
		if(cItf.equals(RemmosEventListener.ITF_NAME)) {
			metricsStore = (RemmosEventListener) sItf;
			return;
		}
		throw new NoSuchInterfaceException("Interface "+ cItf +" non existent");		
	}

	@Override
	public String[] listFc() {
		return itfs;
	}

	@Override
	public Object lookupFc(String cItf) throws NoSuchInterfaceException {
		if(cItf.equals(RecordStore.ITF_NAME)) {
			return recordStore;
		}
		if(cItf.equals(RemmosEventListener.ITF_NAME)) {
			return metricsStore;
		}
		throw new NoSuchInterfaceException("Interface "+ cItf +" non existent");
	}

	@Override
	public void unbindFc(String cItf) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if(cItf.equals(RecordStore.ITF_NAME)) {
			recordStore = null;
		}
		if(cItf.equals(RemmosEventListener.ITF_NAME)) {
			metricsStore = null;
		}
		throw new NoSuchInterfaceException("Interface "+ cItf +" non existent");
	}

}
