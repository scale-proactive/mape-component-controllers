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
package org.objectweb.proactive.extensions.autonomic.controllers.remmos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PABindingControllerImpl;
import org.objectweb.proactive.core.component.control.PAContentController;
import org.objectweb.proactive.core.component.control.PAContentControllerImpl;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.control.PAMulticastController;
import org.objectweb.proactive.core.component.control.PAMulticastControllerImpl;
import org.objectweb.proactive.core.component.control.PASuperController;
import org.objectweb.proactive.core.component.control.PASuperControllerImpl;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.autonomic.controllers.ACConstants;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AlarmListener;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.EventControl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.EventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MetricEventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MetricStore;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MetricStoreImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorControllerImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.RecordStore;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.RecordStoreImpl;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.event.RemmosEventListener;
import org.objectweb.proactive.extensions.autonomic.controllers.planning.PlannerController;
import org.objectweb.proactive.extensions.autonomic.controllers.planning.PlannerControllerImpl;


/**
 * This is an utility class used to instantiate "monitorable" and "manageable" components.
 * 
 * @author cruz
 *
 */
public class Remmos {

	// Logger
	private static final Logger logger = ProActiveLogger.getLogger(Loggers.REMMOS);

	// Monitor-related Components
	private static final String EVENT_LISTENER_COMP = "event-listener-NF";
	private static final String RECORD_STORE_COMP = "record-store-NF";
	private static final String MONITOR_SERVICE_COMP = "monitor-service-NF";
	private static final String METRICS_STORE_COMP = "metrics-store-NF";
	
	private static final String ANALYSIS_CONTROLLER_COMP = "analysis-controller-NF";
	private static final String PLANNER_CONTROLLER_COMP = "PlannerController";
	private static final String EXECUTION_CONTROLLER_COMP = "execution-controller-NF";
	
	// SLA Management-related Components
	// public static final String SLA_SERVICE_COMP = "sla-service-NF";
	// public static final String SLO_STORE_COMP = "slo-store-NF";

	// Reconfiguration-related Components
	// public static final String RECONFIGURATION_SERVICE_COMP = "reconfiguration-component-NF";

	private static final String COMPONENT_CONTROLLER_CONFIG = 
		"/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config.xml";

	private static PAGCMTypeFactory patf;
	private static PAGenericFactory pagf;


	public Remmos() throws InstantiationException {
		checkFactories();
	}

	public Remmos(PAGCMTypeFactory pagcmTypeFactory, PAGenericFactory pagcmGenericFactory) throws InstantiationException {
		patf = pagcmTypeFactory;
		pagf = pagcmGenericFactory;
		checkFactories();
	}

	private static void checkFactories() throws InstantiationException {
		if (patf == null || pagf == null) {
			try {
				Component boot = Utils.getBootstrapComponent();
				patf = (PAGCMTypeFactory) Utils.getPAGCMTypeFactory(boot);
				pagf = (PAGenericFactory) Utils.getPAGenericFactory(boot);
			} catch (Exception e) {
				throw new InstantiationException(e.getMessage());
			}
		}
	}

	/**
	 * Similar to {@link PAGCMTypeFactory#createFcType(InterfaceType[])}, but adds the remmos
	 * NF interface types.
	 * @param fInterfaceTypes
	 * @param hierarchy The hierarchy of the component ({@link Constants#COMPOSITE} or {@link Constants#PRIMITIVE})
	 * @return
	 * @throws InstantiationException
	 */
	public ComponentType createFcType(PAGCMInterfaceType[] fInterfaceTypes, String hierarchy)
			throws InstantiationException {

		PAGCMInterfaceType[] remmosNFItfTypes = createMonitorableNFType(patf, fInterfaceTypes, hierarchy);
		return patf.createFcType(fInterfaceTypes, remmosNFItfTypes);
	}

	/**
	 * Similar to {@link PAGCMTypeFactory#createFcType(InterfaceType[], InterfaceType[])}, but adds the remmos
	 * NF interface types.
	 * @param fInterfaceTypes
	 * @param nfInterfaceTypes
	 * @param hierarchy	The hierarchy of the component ({@link Constants#COMPOSITE} or {@link Constants#PRIMITIVE})
	 * @return
	 * @throws InstantiationException
	 */
	public ComponentType createFcType(PAGCMInterfaceType[] fInterfaceTypes, PAGCMInterfaceType[] nfInterfaceTypes,
			String hierarchy) throws InstantiationException {

		PAGCMInterfaceType[] remmosNFItfTypes = createMonitorableNFType(patf, fInterfaceTypes, hierarchy);
		
		// Merge with other nfInterfaceTypes
		HashMap<String, PAGCMInterfaceType> map = new HashMap<String, PAGCMInterfaceType>();
		for (PAGCMInterfaceType nfItfType : remmosNFItfTypes) {
			map.put(nfItfType.getFcItfName(), nfItfType);
		}
		for (PAGCMInterfaceType nfItfType : nfInterfaceTypes) {
			if (map.containsKey(nfItfType.getFcItfName())) {
				throw new InstantiationException("The NF interface \"" + nfItfType.getFcItfName() + "\" is already in use.");
			}
			map.put(nfItfType.getFcItfName(), nfItfType);
		}
		PAGCMInterfaceType[] finalNFTypes = map.values().toArray(new PAGCMInterfaceType[map.size()]);
		
		return patf.createFcType(fInterfaceTypes, finalNFTypes);
	}


	public Component newFcInstance(Type type, ControllerDescription controllerDesc, ContentDescription contentDesc,
            Node node) throws InstantiationException {

		Component comp = pagf.newFcInstance(type, controllerDesc, contentDesc, node);
		addObjectControllers((PAComponent) comp);
		return comp;
	}

	/**
	 * Creates the NF interfaces that will be used for the Monitoring and Management framework (implemented as components).
	 * 
	 * @param pagcmTf
	 * @param fItfType
	 * @return
	 */ 
	public static PAGCMInterfaceType[] createMonitorableNFType(PAGCMTypeFactory pagcmTf, PAGCMInterfaceType[] fItfType, String hierarchy) {

		ArrayList<PAGCMInterfaceType> typeList = new ArrayList<PAGCMInterfaceType>();
		
		// Normally, the NF interfaces mentioned here should be those that are going to be implemented by NF components,
		// and the rest of the NF interfaces (that are going to be implemented by object controller) should be in a ControllerDesc file.
		// But the PAComponentImpl ignores the NFType if there is a ControllerDesc file specified :(,
		// so I better put all the NF interfaces here.
		// That means that I need another method to add the object controllers for the (not yet created) controllers.
		try {
			// Object controller-managed server interfaces.
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.CONTENT_CONTROLLER, PAContentController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.BINDING_CONTROLLER, PABindingController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.LIFECYCLE_CONTROLLER, PAGCMLifeCycleController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.SUPER_CONTROLLER, PASuperController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.NAME_CONTROLLER, NameController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.MULTICAST_CONTROLLER, PAMulticastController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			
			// server Monitoring interface
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(ACConstants.MONITOR_CONTROLLER, MonitorController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(AnalyzerController.ANALYSIS_CONTROLLER, AnalyzerController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(PlannerController.PLANNER_CONTROLLER, PlannerController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(ExecutorController.EXECUTOR_CONTROLLER, ExecutorController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));

			String itfName;
		
			// external client Monitoring interfaces
			// add one client Monitoring interface for each client F interface
			// Support client-singleton, and client-multicast interfaces
			for(PAGCMInterfaceType itfType : fItfType) {
				if (!itfType.isFcClientItf()) continue;
				itfName = itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX;
				if ((itfType.isGCMSingletonItf() && !itfType.isGCMCollectiveItf()) || itfType.isGCMGathercastItf()) {
					// add a client-singleton interface
					typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
				} else if(itfType.isGCMMulticastItf()) {
					// add a multicast client interface
					typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(itfName, MonitorControllerMulticast.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.MULTICAST_CARDINALITY));
				}
			}
			
			// composites have also internal client and server bindings
			if(Constants.COMPOSITE.equals(hierarchy)) {
				// one client internal Monitoring interface for each server binding
				// collective and multicast/gathercast interfaces not supported (yet)
				for(PAGCMInterfaceType itfType : fItfType) {
					// only server-singleton supported ... others ignored
					if(!itfType.isFcClientItf() && itfType.isGCMSingletonItf() && !itfType.isGCMCollectiveItf()) {
						itfName = itfType.getFcItfName() + ACConstants.INTERNAL_CLIENT_SUFFIX;
						typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY, PAGCMTypeFactory.INTERNAL));
					}
				}
				// one server internal Monitoring interface in each composite
				itfName = ACConstants.INTERNAL_SERVER_NFITF;
				typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY, PAGCMTypeFactory.INTERNAL));
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		return (PAGCMInterfaceType[]) typeList.toArray(new PAGCMInterfaceType[typeList.size()]);
	}
	
	
	/**
	 * Adds the controller objects to the NF interfaces that are not part of the M&M Framework.
	 * Normally, PAComponentImpl.addMandatoryControllers should have added already the mandatory MEMBRANE, LIFECYCLE and NAME controllers.
	 * Interfaces like BINDING and CONTENT, which are not supposed to be in all components, should have been removed from the component NFType in the appropriate cases.
	 * 
	 * @param component
	 */
	public static void addObjectControllers(PAComponent component) {

		PAMembraneController memb = null;
		try {
			memb = Utils.getPAMembraneController(component);
		} catch (NoSuchInterfaceException e) {
			// Non-existent interfaces have been ignored at component creation time.
		}
		// add the remaining object controllers
		try {
			 // this call is just to catch the exception. If the exception is generated in the next line, for some reason I can't catch it here.
			component.getFcInterface(Constants.CONTENT_CONTROLLER);
			memb.setControllerObject(Constants.CONTENT_CONTROLLER, PAContentControllerImpl.class.getName());
		} catch (NoSuchInterfaceException e) {
			// Non-existent interfaces have been ignored at component creation time.
		}
		try {
			component.getFcInterface(Constants.BINDING_CONTROLLER);
			memb.setControllerObject(Constants.BINDING_CONTROLLER, PABindingControllerImpl.class.getName());
		} catch (NoSuchInterfaceException e) {
			// Non-existent interfaces have been ignored at component creation time.
		}
		try {
			component.getFcInterface(Constants.SUPER_CONTROLLER);
			memb.setControllerObject(Constants.SUPER_CONTROLLER, PASuperControllerImpl.class.getName());
		} catch (NoSuchInterfaceException e) {
			// Non-existent interfaces have been ignored at component creation time.
		}
		try {
			component.getFcInterface(Constants.MULTICAST_CONTROLLER);
			memb.setControllerObject(Constants.MULTICAST_CONTROLLER, PAMulticastControllerImpl.class.getName());
		} catch (NoSuchInterfaceException e) {
			// Non-existent interfaces have been ignored at component creation time.
		}
		// LIFECYCLE is mandatory and should have been added at component creation time
		// NAME      is mandatory and should have been added at component creation time
	}

	
	/**
	 * Builds the monitoring components and put them in the membrane.
	 * The functional assembly, in the case of composites, must be done before, otherwise the internal assemblies will be incomplete.
	 * 
	 * After the execution of this method, the component (composite or primitive) will have all the Monitor-related components
	 * created and bound to the corresponding (internal and external) interfaces on the membrane.
	 * 
	 *  The bindings from the external client and internal client monitoring interfaces are not created here.
	 *  They must be added later with the "enableMonitoring" method.
	 * 
	 * @param component
	 * @throws Exception 
	 */
	public static void addMonitoring(Component component) throws Exception {
		checkFactories();
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		States oldStates = stopMembraneAndLifeCycle(membrane, lifeCycle);

		// add components to the membrane
		Node node = getDeploymentNode(component);
		Component eventListener = createBasicEventListener(patf, pagf, EventListener.class.getName(), node);
		Component recordStore = createBasicRecordStore(patf, pagf, RecordStoreImpl.class.getName(), node);
		Component monitorService = createMonitorService(patf, pagf, MonitorControllerImpl.class.getName(), component, node);
		Component metricsStore = createMetricsStore(patf, pagf, MetricStoreImpl.class.getName(), component, node);
	
		membrane.nfAddFcSubComponent(eventListener);
		membrane.nfAddFcSubComponent(recordStore);
		membrane.nfAddFcSubComponent(monitorService);
		membrane.nfAddFcSubComponent(metricsStore);

		// bindings between NF components
		membrane.nfBindFc(MONITOR_SERVICE_COMP+"."+EventControl.ITF_NAME, EVENT_LISTENER_COMP+"."+EventControl.ITF_NAME);
		membrane.nfBindFc(MONITOR_SERVICE_COMP+"."+RecordStore.ITF_NAME, RECORD_STORE_COMP+"."+RecordStore.ITF_NAME);
		membrane.nfBindFc(MONITOR_SERVICE_COMP+"."+MetricStore.ITF_NAME, METRICS_STORE_COMP+"."+MetricStore.ITF_NAME);
		membrane.nfBindFc(EVENT_LISTENER_COMP+"."+RecordStore.ITF_NAME, RECORD_STORE_COMP+"."+RecordStore.ITF_NAME);
		membrane.nfBindFc(EVENT_LISTENER_COMP+"."+RemmosEventListener.ITF_NAME, METRICS_STORE_COMP+"."+RemmosEventListener.ITF_NAME);
		membrane.nfBindFc(METRICS_STORE_COMP+"."+RecordStore.ITF_NAME, RECORD_STORE_COMP+"."+RecordStore.ITF_NAME);
		
		// binding between the NF Monitoring Interface of the host component, and the Monitor Component
		membrane.nfBindFc(ACConstants.MONITOR_CONTROLLER, MONITOR_SERVICE_COMP+"."+MonitorController.ITF_NAME);		

		boolean isComposite = ((PAComponent) component).getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE);

		if (isComposite) {
			// and the binding from the internal server monitor interface, back to the NF Monitor Component
			String clientItfName = ACConstants.INTERNAL_SERVER_NFITF;
			String serverItfName = MonitorController.ITF_NAME;
			membrane.nfBindFc(clientItfName, MONITOR_SERVICE_COMP + "." + serverItfName);
		}

		startMembraneAndLifeCycle(oldStates, membrane, lifeCycle);
	}

	public static void addAnalysis(Component component) throws Exception {
		checkFactories();
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		States oldStates = stopMembraneAndLifeCycle(membrane, lifeCycle);

		// Adding analysis controller
		Component analyzer = createAnalyzerController(patf, pagf, getDeploymentNode(component));
		membrane.nfAddFcSubComponent(analyzer);
		
		// Bind with membrane
		membrane.nfBindFc(AnalyzerController.ANALYSIS_CONTROLLER,
				ANALYSIS_CONTROLLER_COMP + "." + AnalyzerController.ITF_NAME);

		// Assumes MonitorController already added.
		membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + MonitorController.ITF_NAME,
				MONITOR_SERVICE_COMP + "." +  MonitorController.ITF_NAME);
		membrane.nfBindFc(METRICS_STORE_COMP+"."+MetricEventListener.ITF_NAME,
				ANALYSIS_CONTROLLER_COMP+"."+MetricEventListener.ITF_NAME);


		// Bind with PlannerController if it exist. NOTE: This ugly method is needed since the
		//  "NoSuchComponentException" is thrown only on the remote thread.
		for (Component comp : membrane.nfGetFcSubComponents()) {
			if (GCM.getNameController(comp).getFcName().equals(PLANNER_CONTROLLER_COMP)) {
				membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + AlarmListener.ITF_NAME,
						PLANNER_CONTROLLER_COMP + "." + AlarmListener.ITF_NAME);
				
				break;
			}
		}

		startMembraneAndLifeCycle(oldStates, membrane, lifeCycle);

	}

	/**
	 * Adds PlannerController to the component membrane
	 * @param component
	 * @throws Exception
	 */
	public static void addPlannerController(Component component) throws Exception {		
		checkFactories();
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		States oldStates = stopMembraneAndLifeCycle(membrane, lifeCycle);

		// Add PlannerController
		Component plannerController = createPlannerController(patf, pagf, getDeploymentNode(component));
		membrane.nfAddFcSubComponent(plannerController);

		// Bind with membrane
		membrane.nfBindFc(PlannerController.PLANNER_CONTROLLER,
				PLANNER_CONTROLLER_COMP + "." + PlannerController.ITF_NAME);

		// Bind with monitor.
		membrane.nfBindFc(PLANNER_CONTROLLER_COMP + "." + MonitorController.ITF_NAME,
				MONITOR_SERVICE_COMP + "." + MonitorController.ITF_NAME);

		// Bind with ExecutionController and AnalyzerController only if they exist.
		// NOTE: This ugly method is needed since the "NoSuchComponentException" is thrown only on the remote thread.
		int i = 0;
		boolean analyzer = true, executor = true;
		Component[] compControllers = membrane.nfGetFcSubComponents();
		while ( (analyzer || executor) && i < compControllers.length ) {
	
			if (analyzer && GCM.getNameController(compControllers[i]).getFcName().equals(ANALYSIS_CONTROLLER_COMP)) {
				membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + AlarmListener.ITF_NAME,
						PLANNER_CONTROLLER_COMP + "." + AlarmListener.ITF_NAME);
				analyzer = false;
			}

			if (executor && GCM.getNameController(compControllers[i]).getFcName().equals(EXECUTION_CONTROLLER_COMP)) {
				membrane.nfBindFc(PLANNER_CONTROLLER_COMP + "." + ExecutorController.ITF_NAME,
						EXECUTION_CONTROLLER_COMP + "." + ExecutorController.ITF_NAME);
				executor = false;
			}

			i += 1;
		}

		startMembraneAndLifeCycle(oldStates, membrane, lifeCycle);
	}

	/**
	 * Adds ExecutorController to the component membrane
	 * @param component
	 * @throws Exception
	 */
	public static void addExecutorController(Component component) throws Exception {		
		checkFactories();
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		States oldStates = stopMembraneAndLifeCycle(membrane, lifeCycle);

		// Add ExecutorController
		Component executorController = createExecutorController(patf, pagf, getDeploymentNode(component));
		membrane.nfAddFcSubComponent(executorController);
	
		// Bind with membrane
		membrane.nfBindFc(ExecutorController.EXECUTOR_CONTROLLER,
				EXECUTION_CONTROLLER_COMP + "." + ExecutorController.ITF_NAME);

		// Bind with PlannerController if it exist. NOTE: This ugly method is needed since the
		// "NoSuchComponentException" is thrown only on the remote thread.
		for (Component comp : membrane.nfGetFcSubComponents()) {
	
			if (GCM.getNameController(comp).getFcName().equals(PLANNER_CONTROLLER_COMP)) {

				membrane.nfBindFc(PLANNER_CONTROLLER_COMP + "." + ExecutorController.ITF_NAME,
						EXECUTION_CONTROLLER_COMP + "." + ExecutorController.ITF_NAME);
				break;
			}
		}
		
		startMembraneAndLifeCycle(oldStates, membrane, lifeCycle);
	}

	/**
	 * Creates the NF Event Listener component.
	 * @param patf
	 * @param pagf
	 * @return
	 */
	private static Component createBasicEventListener(PAGCMTypeFactory patf, PAGenericFactory pagf, String eventListenerClass, Node node) {
		try {
			InterfaceType[] eventListenerItfType = new InterfaceType[] {
					patf.createGCMItfType(EventControl.ITF_NAME, EventControl.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(RecordStore.ITF_NAME, RecordStore.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(RemmosEventListener.ITF_NAME, RemmosEventListener.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY)
			};
			return pagf.newNfFcInstance(patf.createFcType(eventListenerItfType),
					new ControllerDescription(EVENT_LISTENER_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG),
					new ContentDescription(eventListenerClass), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates the NF Record Store component
	 * @param patf
	 * @param pagf
	 * @param logStoreClass
	 * @return
	 */
	private static Component createBasicRecordStore(PAGCMTypeFactory patf, PAGenericFactory pagf, String recordStoreClass, Node node) {
		try {
			InterfaceType[] recordStoreItfType = new InterfaceType[] {
					patf.createGCMItfType(RecordStore.ITF_NAME, RecordStore.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY)
			};
			return pagf.newNfFcInstance(patf.createFcType(recordStoreItfType), 
					new ControllerDescription(RECORD_STORE_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG), 
					new ContentDescription(recordStoreClass), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates the NF Metrics Store component
	 * @param patf
	 * @param pagf
	 * @param metricsStoreClass
	 * @param node
	 * @return
	 */
	private static Component createMetricsStore(PAGCMTypeFactory patf, PAGenericFactory pagf, String metricsStoreClass, Component component, Node node) {
		try {
			ArrayList<InterfaceType> itfTypeList = new ArrayList<InterfaceType>();
			itfTypeList.add(patf.createGCMItfType(MetricStore.ITF_NAME, MetricStore.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			itfTypeList.add(patf.createGCMItfType(RemmosEventListener.ITF_NAME, RemmosEventListener.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			itfTypeList.add(patf.createGCMItfType(RecordStore.ITF_NAME, RecordStore.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			itfTypeList.add(patf.createGCMItfType(MetricEventListener.ITF_NAME, MetricEventListener.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));

			String itfName;
			
			// external client Monitoring interfaces
			// add one client Monitoring interface for each client binding (maybe optional or mandatory)
			// collective and multicast/gathercast interfaces not supported (yet)
			for(InterfaceType itfType : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
				if(!itfType.isFcClientItf()) continue;
				itfName = itfType.getFcItfName() + "-external-" +  MonitorController.ITF_NAME;
				if((((PAGCMInterfaceType)itfType).isGCMSingletonItf() && !((PAGCMInterfaceType)itfType).isGCMCollectiveItf()) || ((PAGCMInterfaceType)itfType).isGCMGathercastItf()) {
					// only client-singleton supported
					itfTypeList.add(patf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
				}
				else if(((PAGCMInterfaceType)itfType).isGCMMulticastItf() ) {
					logger.debug("   There is a MULTICAST client itf! The Monitor Component should have the MULTICAST client interface: "+ itfName);
					itfTypeList.add(patf.createGCMItfType(itfName, MonitorControllerMulticast.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
				}
			}
			
			// composites also require client interfaces for internal bindings
			if(Constants.COMPOSITE.equals(((PAComponent) component).getComponentParameters().getHierarchicalType())) {
				// one client internal Monitoring interface for each server binding
				// collective and multicast/gathercast interfaces not supported (yet)
				for(InterfaceType itfType : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
					if(!itfType.isFcClientItf() && ((PAGCMInterfaceType)itfType).isGCMSingletonItf() && !((PAGCMInterfaceType)itfType).isGCMCollectiveItf()) {
						// only server-singleton supported ... others ignored
						itfName = itfType.getFcItfName() + "-internal-" + MonitorController.ITF_NAME;
						itfTypeList.add(patf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));					
					}
				}
			}
		
			return pagf.newNfFcInstance(patf.createFcType(itfTypeList.toArray(new InterfaceType[itfTypeList.size()])), 
					new ControllerDescription(METRICS_STORE_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG), 
					new ContentDescription(metricsStoreClass), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Creates the NF Monitor Service component
	 * @param patf
	 * @param pagf
	 * @param monitorServiceClass
	 * @param component
	 * @return
	 */
	private static Component createMonitorService(PAGCMTypeFactory patf, PAGenericFactory pagf, String monitorServiceClass, Component component, Node node) {
		try {
			ArrayList<InterfaceType> itfTypeList = new ArrayList<InterfaceType>();
			itfTypeList.add(patf.createGCMItfType(EventControl.ITF_NAME, EventControl.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			itfTypeList.add(patf.createGCMItfType(RecordStore.ITF_NAME, RecordStore.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			itfTypeList.add(patf.createGCMItfType(MetricStore.ITF_NAME, MetricStore.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));			
			itfTypeList.add(patf.createGCMItfType(MonitorController.ITF_NAME, MonitorController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY));

			String itfName;
			
			// external client Monitoring interfaces
			// add one client Monitoring interface for each client binding (maybe optional or mandatory)
			// collective and multicast/gathercast interfaces not supported (yet)
			for(InterfaceType itfType : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
				if(!itfType.isFcClientItf()) continue;
				itfName = itfType.getFcItfName() + "-external-" +  MonitorController.ITF_NAME;
				if((((PAGCMInterfaceType)itfType).isGCMSingletonItf() && !((PAGCMInterfaceType)itfType).isGCMCollectiveItf()) || ((PAGCMInterfaceType)itfType).isGCMGathercastItf()) {
					// only client-singleton supported
					itfTypeList.add(patf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
				}
				else if(((PAGCMInterfaceType)itfType).isGCMMulticastItf() ) {
					logger.debug("   There is a MULTICAST client itf! The Monitor Component should have the MULTICAST client interface: "+ itfName);
					itfTypeList.add(patf.createGCMItfType(itfName, MonitorControllerMulticast.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
				}
			}
			
			// composites also require client interfaces for internal bindings
			if(Constants.COMPOSITE.equals(((PAComponent) component).getComponentParameters().getHierarchicalType())) {
				// one client internal Monitoring interface for each server binding
				// collective and multicast/gathercast interfaces not supported (yet)
				for(InterfaceType itfType : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
					if(!itfType.isFcClientItf() && ((PAGCMInterfaceType)itfType).isGCMSingletonItf() && !((PAGCMInterfaceType)itfType).isGCMCollectiveItf()) {
						// only server-singleton supported ... others ignored
						itfName = itfType.getFcItfName() + "-internal-" + MonitorController.ITF_NAME;
						itfTypeList.add(patf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));					
					}
				}
			}

			return pagf.newNfFcInstance(patf.createFcType(itfTypeList.toArray(new InterfaceType[itfTypeList.size()])),
					new ControllerDescription(MONITOR_SERVICE_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG),
					new ContentDescription(monitorServiceClass), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	private static Component createAnalyzerController(PAGCMTypeFactory patf, PAGenericFactory pagf, Node node) {
		try {
			InterfaceType[] itfTypes = new InterfaceType[] {
					patf.createGCMItfType(AnalyzerController.ITF_NAME, AnalyzerController.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MetricEventListener.ITF_NAME, MetricEventListener.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MonitorController.ITF_NAME,	MonitorController.class.getName(), PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(AlarmListener.ITF_NAME, AlarmListener.class.getName(), PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			};

			return pagf.newNfFcInstance(patf.createFcType(itfTypes), 
					new ControllerDescription(ANALYSIS_CONTROLLER_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG), 
					new ContentDescription(AnalyzerControllerImpl.class.getName()), node);

		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Component createPlannerController(PAGCMTypeFactory patf, PAGenericFactory pagf, Node node) {
		try {
			InterfaceType[] itfTypes = new InterfaceType[] {
					patf.createGCMItfType(PlannerController.ITF_NAME, PlannerController.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(AlarmListener.ITF_NAME, AlarmListener.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MonitorController.ITF_NAME,	MonitorController.class.getName(), PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(ExecutorController.ITF_NAME, ExecutorController.class.getName(), PAGCMTypeFactory.CLIENT, PAGCMTypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			};

			return pagf.newNfFcInstance(patf.createFcType(itfTypes), 
					new ControllerDescription(PLANNER_CONTROLLER_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG), 
					new ContentDescription(PlannerControllerImpl.class.getName()), node);

		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Component createExecutorController(PAGCMTypeFactory patf, PAGenericFactory pagf, Node node) {
		try {
			InterfaceType[] itfTypes = new InterfaceType[] {
					patf.createGCMItfType(ExecutorController.ITF_NAME, ExecutorController.class.getName(), PAGCMTypeFactory.SERVER, PAGCMTypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			};
			return pagf.newNfFcInstance(patf.createFcType(itfTypes), 
					new ControllerDescription(EXECUTION_CONTROLLER_COMP, Constants.PRIMITIVE, COMPONENT_CONTROLLER_CONFIG), 
					new ContentDescription(ExecutorControllerImpl.class.getName()), node);

		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * Starts monitoring in this component and all its connections.
	 * Bindings are created if necessary.
	 * 
	 * FIXME: This method is recursive, and performs a DFS search in the graph of bindings.
	 *        It does not consider cyclic paths. For that it would need a parameter of "visited" components (like MonitorControlImpl, when recovering the paths)
	 *        
	 * WARN: The method can repeat bindings, in the sense that it can create them twice. This is not inconsistent, but it can be improved by keeping a list
	 *       of created bindings.
	 * 
	 * @param component
	 */
	public static void enableMonitoring(Component component) {
		
		// if the component is not an instance of PAComponent, it will fail
		if(!(component instanceof PAComponent)) {
			return;
		}
	
		PAComponent pacomponent = (PAComponent) component;
		boolean isComposite = Constants.COMPOSITE.equals(pacomponent.getComponentParameters().getHierarchicalType());
		
		// Get essential Controllers
		PASuperController sc = null;
		PABindingController bc = null;
		PAMembraneController membrane = null;
		try {
			sc = Utils.getPASuperController(pacomponent);
			bc = Utils.getPABindingController(pacomponent);
			membrane = Utils.getPAMembraneController(pacomponent);
		} catch (NoSuchInterfaceException e) {
			return;
		}

		Component parent = null;
		Component parents[] = sc.getFcSuperComponents();
		if(parents.length > 0) {
			// we should get only one parent here, as GCM does not support shared components
			parent = ((PAComponent)parents[0]);
		}
		
		for(InterfaceType itf : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
			
			PAGCMInterfaceType itfType = (PAGCMInterfaceType) itf;
			boolean isSingleton = itfType.isGCMSingletonItf() && ! itfType.isGCMCollectiveItf();

			if ( !isComposite && !itfType.isFcClientItf() ) {
				continue; // primitive server, nothing to do with them.
			}

			if(isComposite && isSingleton && !itfType.isFcClientItf()) {
				
				if ( isAlreadyBound(itfType.getFcItfName() + ACConstants.INTERNAL_CLIENT_SUFFIX, membrane) ) {
					continue; // break loops
				}
		
				Component destComp = getItfOwnerComponentOrNull(itfType.getFcItfName(), bc);
				MonitorController internalMonitor = getMonitorControllerOrNull(destComp);
				if (internalMonitor == null) {
					continue; // assumes no monitors on purpose
				}
	
				String clientItfName = itfType.getFcItfName() + "-internal-" + MonitorController.ITF_NAME;
				String serverItfName = itfType.getFcItfName() + ACConstants.INTERNAL_CLIENT_SUFFIX;
				try {
					membrane.stopMembrane();
					membrane.nfBindFc(METRICS_STORE_COMP + "." + clientItfName, serverItfName);
					membrane.nfBindFc(MONITOR_SERVICE_COMP+"."+clientItfName, serverItfName);
					membrane.nfBindFc(serverItfName, internalMonitor);
					membrane.startMembrane();
				} catch (Exception e) {
					e.printStackTrace();
				}

				enableMonitoring(destComp);
			}

			// Bind client interfaces
			if (!itfType.isFcClientItf()) continue;
			
			if (isSingleton || itfType.isGCMGathercastItf()) {
				
				if (isAlreadyBound(itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX, membrane)) {
					continue; // break loops
				}
				
				// ignore not PAComponentRepresentative (WSComponent for example)
				Component destComp = getItfOwnerComponentOrNull(itfType.getFcItfName(), bc);
				if ( !(destComp instanceof PAComponentRepresentative) ) continue;
				
				boolean isNotParent = !destComp.equals(parent);
			
				MonitorController externalMonitor = isNotParent ?
						getMonitorControllerOrNull(destComp) : getInternalMonitorControllerOrNull(destComp);
				if (externalMonitor == null) {
					continue; // assumes no monitors on purpose
				}
				
				String clientItfName = itfType.getFcItfName() + "-external-" + MonitorController.ITF_NAME;
				String serverItfName = itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX;
				
				try {
					membrane.stopMembrane();
					membrane.nfBindFc(METRICS_STORE_COMP + "." + clientItfName, serverItfName);
					membrane.nfBindFc(MONITOR_SERVICE_COMP + "." + clientItfName, serverItfName);
					membrane.nfBindFc(itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX, externalMonitor);
					membrane.startMembrane();
				} catch(Exception e) {
					e.printStackTrace();
				}
			
				if (isNotParent) {
					enableMonitoring(destComp);
				}
			}
			
			if (itfType.isGCMMulticastItf()) {
				try {
					PAMulticastController pamc = Utils.getPAMulticastController(pacomponent);
					
					// Remove old NF destinations
					String nfItfName = itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX;
					try {
						Object[] nfItfs = pamc.lookupGCMMulticast(nfItfName);
						for (Object nfItf : nfItfs) {
							pamc.unbindGCMMulticast(nfItfName, nfItf);
						}
					} catch (IllegalBindingException | IllegalLifeCycleException e) {
						e.printStackTrace();
					}

					// Get destination components
					ArrayList<Component> destinations = new ArrayList<Component>();
					for (Object destItf : pamc.lookupGCMMulticast(itfType.getFcItfName())) {
						destinations.add(((PAInterface) destItf).getFcItfOwner());
					}

					for(Component destComp : destinations) {
					
						//  ignore not PAComponentRepresentative (WSComponent for example)
						if (!(destComp instanceof PAComponentRepresentative)) continue;

						boolean isNotParent = !destComp.equals(parent);
						MonitorController externalMonitor = isNotParent ?
								getMonitorControllerOrNull(destComp) : getInternalMonitorControllerOrNull(destComp);
								
						if (externalMonitor == null) {
							continue; // assumes no monitors on purpose
						}
			
						String clientItfName = itfType.getFcItfName() + "-external-" + MonitorController.ITF_NAME;
						String serverItfName = itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX;
						
						try {
							membrane.stopMembrane();
							membrane.nfBindFc(METRICS_STORE_COMP + "." + clientItfName, serverItfName);
							membrane.nfBindFc(MONITOR_SERVICE_COMP + "." + clientItfName, serverItfName);
							membrane.nfBindFc(itfType.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX, externalMonitor);
							membrane.startMembrane();
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						if (isNotParent) {
							enableMonitoring(destComp);
						}
					}
				} catch (NoSuchInterfaceException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	public static void unbind(Object clientItf) throws IllegalLifeCycleException, NoSuchInterfaceException, IllegalBindingException, NoSuchComponentException {
		if (clientItf instanceof PAInterface) {
			PAInterface itf = (PAInterface) clientItf;
			Component owner = itf.getFcItfOwner();
			
			PAGCMLifeCycleController lcc = Utils.getPAGCMLifeCycleController(owner);
			PAMembraneController mc = Utils.getPAMembraneController(owner);
			States oldStates = Remmos.stopMembraneAndLifeCycle(mc, lcc);
			
			String clientItfName = itf.getFcItfName() + "-external-" + MonitorController.ITF_NAME;
			String serverItfName = itf.getFcItfName() + ACConstants.EXTERNAL_CLIENT_SUFFIX;
			mc.nfUnbindFc(MONITOR_SERVICE_COMP + "." + clientItfName);
			mc.nfUnbindFc(METRICS_STORE_COMP + "." + clientItfName);
			
			//mc.nfUnbindFc(serverItfName);
			
			Remmos.startMembraneAndLifeCycle(oldStates, mc, lcc);
		}
	}

	// COMPONENT CONTROLLERS GETTERS

	public static MonitorController getMonitorController(Component component) throws NoSuchInterfaceException {
		return (MonitorController) component.getFcInterface(ACConstants.MONITOR_CONTROLLER);
	}
	
	public static AnalyzerController getAnalyzerController(Component component) throws NoSuchInterfaceException {
		return (AnalyzerController) component.getFcInterface(AnalyzerController.ANALYSIS_CONTROLLER);
	}

	public static PlannerController getPlannerController(Component component) throws NoSuchInterfaceException {
		return (PlannerController) component.getFcInterface(PlannerController.PLANNER_CONTROLLER);
	}

	public static ExecutorController getExecutorController(Component component) throws NoSuchInterfaceException {
		return (ExecutorController) component.getFcInterface(ExecutorController.EXECUTOR_CONTROLLER);
	}

	// UTILS

	/** Returns the node in which this component was deployed */
	private static Node getDeploymentNode(Component component) throws NodeException {
		UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) component).getProxy();
		return NodeFactory.getNode(ubProxy.getBody().getNodeURL());
	}

	private static class States implements Serializable {
		private static final long serialVersionUID = 1L;
		private String membrane, lifeCycle;
		States(String membraneState, String lifeCycleState) {
			membrane = membraneState;
			lifeCycle = lifeCycleState;
		}
		String getMembraneState() { return membrane; }
		String getLifeCycleState() { return lifeCycle; }
	}

	/** Stops the Membran and LifeCycle Controllers */
	private static States stopMembraneAndLifeCycle(PAMembraneController membrane, PAGCMLifeCycleController lifeCycle)
			throws IllegalLifeCycleException, NoSuchInterfaceException {
		// check that membrane is started (needed to check lifeCycle state)
		String membraneState = membrane.getMembraneState();
		if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
			membrane.startMembrane(); // 
		}
		// stop lifeCycle
		String lifeCycleState = lifeCycle.getFcState();
		if (lifeCycleState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.stopFc();
		}
		// stop membrane
		if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.stopMembrane();
		}
		return new States(membraneState, lifeCycleState);
	}

	private static void startMembraneAndLifeCycle(States oldStates, PAMembraneController membrane,
			PAGCMLifeCycleController lifeCycle) throws IllegalLifeCycleException {
		if(oldStates.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.startMembrane();
		}
		if(oldStates.getLifeCycleState().equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.startFc();
		}
	}

	private static boolean isAlreadyBound(String interfaceName, PAMembraneController membrane) {
		try {
			if (membrane.nfLookupFc(interfaceName) != null) {
				return true;
			}
		} catch(Exception nsce) { }
		return false;
	}

	private static MonitorController getMonitorControllerOrNull(Component comp) {
		try {
			return getMonitorController(comp);
		} catch(Exception e) {
			return null;
		}
	}
	
	private static MonitorController getInternalMonitorControllerOrNull(Component comp) {
		try {
			return (MonitorController) comp.getFcInterface(ACConstants.INTERNAL_SERVER_NFITF);
		} catch(Exception e) {
			return null;
		}
	}
	
	private static Component getItfOwnerComponentOrNull(String interfaceName, BindingController bindingController) {
		try {
			Object serverItf = bindingController.lookupFc(interfaceName);
			return serverItf == null ? null : ((PAInterface) serverItf).getFcItfOwner();
		} catch(NoSuchInterfaceException nsie) {
			return null;
		} 
	}

}
