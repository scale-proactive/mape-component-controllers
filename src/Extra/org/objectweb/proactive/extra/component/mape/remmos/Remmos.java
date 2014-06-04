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
package org.objectweb.proactive.extra.component.mape.remmos;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
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
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.component.mape.analysis.AnalysisController;
import org.objectweb.proactive.extra.component.mape.analysis.AnalysisControllerImpl;
import org.objectweb.proactive.extra.component.mape.reconfiguration.ExecutionController;
import org.objectweb.proactive.extra.component.mape.reconfiguration.ExecutionControllerImpl;
import org.objectweb.proactive.extra.component.mape.monitoring.EventControl;
import org.objectweb.proactive.extra.component.mape.monitoring.EventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricEventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricStore;
import org.objectweb.proactive.extra.component.mape.monitoring.MetricStoreImpl;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorController;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorControllerImpl;
import org.objectweb.proactive.extra.component.mape.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extra.component.mape.monitoring.event.RemmosEventListener;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordStore;
import org.objectweb.proactive.extra.component.mape.monitoring.records.RecordStoreImpl;


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
	private static final String EXECUTION_CONTROLLER_COMP = "execution-controller-NF";
	
	// SLA Management-related Components
	// public static final String SLA_SERVICE_COMP = "sla-service-NF";
	// public static final String SLO_STORE_COMP = "slo-store-NF";

	// Reconfiguration-related Components
	// public static final String RECONFIGURATION_SERVICE_COMP = "reconfiguration-component-NF";

	private static final String CONFIG_FILE_PATH = "/org/objectweb/proactive/core/component/componentcontroller/"
			+ "config/default-component-controller-config-basic.xml";

	private static final String INTERNAL_MON_ITF = "internal-server-" + Constants.MONITOR_CONTROLLER;

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
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.MONITOR_CONTROLLER, MonitorController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			
			// SLA management interface
			// typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.SLA_CONTROLLER, SLAService.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));		
			// Analysis interface
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.ANALYSIS_CONTROLLER, AnalysisController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));

			// reconfiguration interface
			//typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.RECONFIGURATION_CONTROLLER, PAReconfigurationController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));
			// Execution interface
			typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(Constants.EXECUTION_CONTROLLER, ExecutionController.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY));

			String itfName;
		
			// external client Monitoring interfaces
			// add one client Monitoring interface for each client F interface
			// Support client-singleton, and client-multicast interfaces
			for(PAGCMInterfaceType itfType : fItfType) {
				if (!itfType.isFcClientItf()) continue;
				itfName = itfType.getFcItfName() + "-external-" + Constants.MONITOR_CONTROLLER;
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
						itfName = itfType.getFcItfName() + "-internal-"+Constants.MONITOR_CONTROLLER;
						typeList.add((PAGCMInterfaceType) pagcmTf.createGCMItfType(itfName, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY, PAGCMTypeFactory.INTERNAL));
					}
				}
				// one server internal Monitoring interface in each composite
				itfName = "internal-server-" + Constants.MONITOR_CONTROLLER;
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

		logger.debug("Currently on runtime: "+ ProActiveRuntimeImpl.getProActiveRuntime().getURL() );
		PAComponent pac = (PAComponent) component;
		PAComponentRepresentative pacr = (PAComponentRepresentative) component;
		logger.debug("Adding monitoring components for component ["+ pac.getComponentParameters().getName()+"], with ID ["+ pac.getID() +"]");
		String bodyUrl = ((UniversalBodyProxy) pacr.getProxy()).getBody().getNodeURL();
		//logger.debug("   Which is in node ["+ bodyUrl + "]");
		Node parentNode = NodeFactory.getNode(bodyUrl);
		//ProActiveRuntime part = parentNode.getProActiveRuntime();
		//logger.debug("   and in runtime ["+ part.getURL() + "]");

		// creates the components used for monitoring
		logger.debug("Creating NF monitoring components");
		Component eventListener = createBasicEventListener(patf, pagf, EventListener.class.getName(), parentNode);
		Component recordStore = createBasicRecordStore(patf, pagf, RecordStoreImpl.class.getName(), parentNode);
		Component monitorService = createMonitorService(patf, pagf, MonitorControllerImpl.class.getName(), component, parentNode);
		Component metricsStore = createMetricsStore(patf, pagf, MetricStoreImpl.class.getName(), component, parentNode);
	
		// Get essential controllers
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		PABindingController bindingController = Utils.getPABindingController(component);
	
		String membraneState = membrane.getMembraneState();
		if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
			// I need the membrane started in order to consult the state of LifeCycleController
			membrane.startMembrane();
		}

		String componentState = lifeCycle.getFcState();
		if (componentState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.stopFc();
		}

		if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.stopMembrane();
		}

		// add components to the membrane
		logger.debug("Inserting components in the membrane");
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
		membrane.nfBindFc(Constants.MONITOR_CONTROLLER, MONITOR_SERVICE_COMP+"."+MonitorController.ITF_NAME);		

		
		boolean isComposite = ((PAComponent) component).getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE);

		for(InterfaceType itfType : ((PAComponent) component).getComponentParameters().getInterfaceTypes()) {
			
			if ( !isComposite && !itfType.isFcClientItf() ) continue; // save some time, nothing to do with them.
			
			boolean isSingleton = ((PAGCMInterfaceType) itfType).isGCMSingletonItf() && !((PAGCMInterfaceType)itfType).isGCMCollectiveItf();
			if(isComposite && isSingleton && !itfType.isFcClientItf()) {
				String clientItfName = itfType.getFcItfName() + "-internal-" + MonitorController.ITF_NAME;
				String serverItfName = itfType.getFcItfName() + "-internal-" + Constants.MONITOR_CONTROLLER;
				membrane.nfBindFc(METRICS_STORE_COMP + "." + clientItfName, serverItfName);
				membrane.nfBindFc(MONITOR_SERVICE_COMP+"."+clientItfName, serverItfName);
			}
			
			// Bind client interfaces
			if(!itfType.isFcClientItf()) continue;
			String clientItfName = itfType.getFcItfName() + "-external-" + MonitorController.ITF_NAME;
			String serverItfName = itfType.getFcItfName() + "-external-" + Constants.MONITOR_CONTROLLER;
			membrane.nfBindFc(METRICS_STORE_COMP + "." + clientItfName, serverItfName);
			membrane.nfBindFc(MONITOR_SERVICE_COMP + "." + clientItfName, serverItfName);
		}

		if (isComposite) {
			// and the binding from the internal server monitor interface, back to the NF Monitor Component
			String clientItfName = "internal-server-" + Constants.MONITOR_CONTROLLER;
			String serverItfName = MonitorController.ITF_NAME;
			membrane.nfBindFc(clientItfName, MONITOR_SERVICE_COMP + "." + serverItfName);
		}

		// restore membrane and component lifecycle after having made changes
		if(membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.startMembrane();
		}
		if(componentState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.startFc();
		}
	}


	/**
	 * Builds the SLA monitoring components and put them in the membrane.
	 * The Monitoring components must have been added before, otherwise this method will fail.
	 * 
	 * After the execution of this method, the component (composite or primitive) will have all the SLA Monitor-related components
	 * created and bound to the the Monitor Service components.
	 * 
	 * @param component
	 * @throws Exception
	 */
	/*
	public static void addSLAMonitoring(Component component) throws Exception {
		// bootstrapping component and factories
		Component boot = Fractal.getBootstrapComponent();
		PAGCMTypeFactory patf = null;
		PAGenericFactory pagf = null;
		patf = (PAGCMTypeFactory) Fractal.getTypeFactory(boot);
		pagf = (PAGenericFactory) Fractal.getGenericFactory(boot);

		PAComponent pac = (PAComponent) component;
		PAComponentRepresentative pacr = (PAComponentRepresentative) component;
		logger.debug("Adding SLA Monitoring components for component ["+ pac.getComponentParameters().getName()+"], with ID ["+ pac.getID() +"]");
		UniversalBodyProxy ubp = (UniversalBodyProxy) pacr.getProxy();
		UniversalBody ub = ubp.getBody();
		String bodyUrl = ub.getNodeURL();
		//logger.debug("   Which is in node ["+ bodyUrl + "]");
		Node parentNode = NodeFactory.getNode(bodyUrl);
		//ProActiveRuntime part = parentNode.getProActiveRuntime();
		//logger.debug("   and in runtime ["+ part.getURL() + "]");
		
		// creates the components used for monitoring
		Component slaService = createBasicSLAService(patf, pagf, SLAServiceImpl.class.getName(), parentNode);
		Component sloStore = createBasicSLOStore(patf, pagf, SLOStoreImpl.class.getName(), parentNode);
		
		// performs the NF assembly
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		// stop the membrane and component lifecycle before making changes
		String membraneOldState = membrane.getMembraneState();
		String componentOldState = lifeCycle.getFcState();
		lifeCycle.stopFc();
		membrane.stopMembrane();
		
		
		// add components to the membrane
		membrane.nfAddFcSubComponent(slaService);
		membrane.nfAddFcSubComponent(sloStore);
		// bindings between NF components
		membrane.nfBindFc(SLA_SERVICE_COMP+"."+SLO_STORE_ITF, SLO_STORE_COMP+"."+SLO_STORE_ITF);
		membrane.nfBindFc(SLO_STORE_COMP+"."+MONITOR_SERVICE_ITF, MONITOR_SERVICE_COMP+"."+MONITOR_SERVICE_ITF);
		// binding between the NF SLA Interface of the host component, and the SLA Component
		membrane.nfBindFc(Constants.SLA_CONTROLLER, SLA_SERVICE_COMP+"."+SLA_SERVICE_ITF);
		
		// restore membrane and component lifecycle after having made changes
		if(membraneOldState.equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.startMembrane();
		}
		if(componentOldState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.startFc();
		}
		
		logger.debug("   Done for component ["+pac.getComponentParameters().getName()+"] !");
	}
	*/
	public static void addAnalysis(Component component) throws Exception {
		
		checkFactories();

		UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) component).getProxy();
		Node parentNode = NodeFactory.getNode(ubProxy.getBody().getNodeURL());
		
		Component analysis = createAnalysisController(patf, pagf, AnalysisControllerImpl.class.getName(), parentNode);
		
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		
		String membraneState = membrane.getMembraneState();
		if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
			// I need the membrane started in order to consult the state of LifeCycleController
			membrane.startMembrane();
		}

		String componentState = lifeCycle.getFcState();
		if (componentState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.stopFc();
		}

		if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.stopMembrane();
		}
	
		// Adding analysis controller
		membrane.nfAddFcSubComponent(analysis);
		membrane.nfBindFc(Constants.ANALYSIS_CONTROLLER, ANALYSIS_CONTROLLER_COMP+"."+AnalysisController.ITF_NAME);
		
		// Assumes MonitorController already added.
		membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + MonitorController.ITF_NAME, MONITOR_SERVICE_COMP + "." +  MonitorController.ITF_NAME);
		membrane.nfBindFc(METRICS_STORE_COMP+"."+MetricEventListener.ITF_NAME, ANALYSIS_CONTROLLER_COMP+"."+MetricEventListener.ITF_NAME);

		// Bind with execution controller if it exist. NOTE: This ugly method is needed since the
		//  "NoSuchComponentException" is thrown only on the remote thread.
		for (Component comp : membrane.nfGetFcSubComponents()) {
			if (GCM.getNameController(comp).getFcName().equals(EXECUTION_CONTROLLER_COMP)) {
				membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + ExecutionController.ITF_NAME,
						EXECUTION_CONTROLLER_COMP + "." + ExecutionController.ITF_NAME);
				
				break;
			}
		}

		if(membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) membrane.startMembrane();
		if(componentState.equals(PAGCMLifeCycleController.STARTED)) lifeCycle.startFc();

	}

	/**
	 * Add the Reconfiguration component and put it in the membrane.
	 * TODO: add the binding from the SLA component, if it exists.
	 * 
	 * After the execution of this method, the component (composite or primitive) will have a Reconfiguration component embedding a PAGCMScript engine
	 * 
	 * @param component
	 * @throws Exception
	 */
	/*
	public static void addReconfiguration(Component component) throws Exception {

		// bootstrapping component and factories
		Component boot = Fractal.getBootstrapComponent();
		PAGCMTypeFactory patf = null;
		PAGenericFactory pagf = null;
		patf = (PAGCMTypeFactory) Fractal.getTypeFactory(boot);
		pagf = (PAGenericFactory) Fractal.getGenericFactory(boot);
		
		logger.debug("Currently on runtime: "+ ProActiveRuntimeImpl.getProActiveRuntime().getURL() );
		PAComponent pac = (PAComponent) component;
		PAComponentRepresentative pacr = (PAComponentRepresentative) component;
		logger.debug("Adding reconfiguration component for component ["+ pac.getComponentParameters().getName()+"], with ID ["+ pac.getID() +"]");
		UniversalBodyProxy ubp = (UniversalBodyProxy) pacr.getProxy();
		UniversalBody ub = ubp.getBody();
		String bodyUrl = ub.getNodeURL();
		//logger.debug("   Which is in node ["+ bodyUrl + "]");
		Node parentNode = NodeFactory.getNode(bodyUrl);
		ProActiveRuntime part = parentNode.getProActiveRuntime();
		//logger.debug("   and in runtime ["+ part.getURL() + "]");
		
		// creates the components used for reconfiguration
		Component reconfiguration = createReconfigurationComponent(patf, pagf, ReconfigurationImpl.class.getName(), parentNode);

		// performs the NF assembly
		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		// stop the membrane and component lifecycle before making changes
		String membraneOldState = membrane.getMembraneState();
		String componentOldState = lifeCycle.getFcState();
		lifeCycle.stopFc();
		membrane.stopMembrane();
		
		
		// add components to the membrane
		membrane.nfAddFcSubComponent(reconfiguration);
		// binding between the NF Reconfiguration interface of the host component, and the Reconfiguration Component
		membrane.nfBindFc(Constants.RECONFIGURATION_CONTROLLER, RECONFIGURATION_SERVICE_COMP+"."+ACTIONS_ITF);
		
		// restore membrane and component lifecycle after having made changes
		if(membraneOldState.equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.startMembrane();
		}
		if(componentOldState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.startFc();
		}
		
		logger.debug("   Done for component ["+pac.getComponentParameters().getName()+"] !");
	}
	*/
	public static void addExecution(Component component) throws Exception {
		
		checkFactories();

		UniversalBodyProxy ubProxy = (UniversalBodyProxy) ((PAComponentRepresentative) component).getProxy();
		Node parentNode = NodeFactory.getNode(ubProxy.getBody().getNodeURL());
		
		Component execution = createExecutionController(patf, pagf, ExecutionControllerImpl.class.getName(), parentNode);

		PAMembraneController membrane = Utils.getPAMembraneController(component);
		PAGCMLifeCycleController lifeCycle = Utils.getPAGCMLifeCycleController(component);
		
		String membraneState = membrane.getMembraneState();
		if (membraneState.equals(PAMembraneController.MEMBRANE_STOPPED)) {
			// I need the membrane started in order to consult the state of LifeCycleController
			membrane.startMembrane();
		}

		String componentState = lifeCycle.getFcState();
		if (componentState.equals(PAGCMLifeCycleController.STARTED)) {
			lifeCycle.stopFc();
		}

		if (membrane.getMembraneState().equals(PAMembraneController.MEMBRANE_STARTED)) {
			membrane.stopMembrane();
		}

		// Adding execution controller
		membrane.nfAddFcSubComponent(execution);
		membrane.nfBindFc(Constants.EXECUTION_CONTROLLER, EXECUTION_CONTROLLER_COMP+"."+ExecutionController.ITF_NAME);

		// Bind with analysis controller if it exist. NOTE: This ugly method is needed since the
		//  "NoSuchComponentException" is thrown only on the remote thread.
		for (Component comp : membrane.nfGetFcSubComponents()) {
			if (GCM.getNameController(comp).getFcName().equals(ANALYSIS_CONTROLLER_COMP)) {
				membrane.nfBindFc(ANALYSIS_CONTROLLER_COMP + "." + ExecutionController.ITF_NAME,
						EXECUTION_CONTROLLER_COMP + "." + ExecutionController.ITF_NAME);
				
				break;
			}
		}

		if(membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) membrane.startMembrane();
		if(componentState.equals(PAGCMLifeCycleController.STARTED)) lifeCycle.startFc();

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
					new ControllerDescription(EVENT_LISTENER_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH),
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
					new ControllerDescription(RECORD_STORE_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH), 
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
					new ControllerDescription(METRICS_STORE_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH), 
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
					new ControllerDescription(MONITOR_SERVICE_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH),
					new ContentDescription(monitorServiceClass), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * TODO: replace analysis controller with sla
	 * 
	private static Component createBasicSLAService(PAGCMTypeFactory patf, PAGenericFactory pagf, String slaServiceClass, Node node) {

		Component slaService = null;
		InterfaceType[] slaServiceItfType = null;
		ComponentType slaServiceType = null;
		
		try {
			slaServiceItfType = new InterfaceType[] {
					patf.createGCMItfType(SLO_STORE_ITF, SLOStore.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(SLA_SERVICE_ITF, SLAService.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY)
			};
			slaServiceType = patf.createFcType(slaServiceItfType);
			slaService = pagf.newNfFcInstance(slaServiceType, 
					new ControllerDescription(SLA_SERVICE_COMP, Constants.PRIMITIVE, "/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config-basic.xml"), 
					new ContentDescription(slaServiceClass),
					node
			);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return slaService;
	}
	
	private static Component createBasicSLOStore(PAGCMTypeFactory patf, PAGenericFactory pagf, String sloStoreClass, Node node) {
		
		Component sloStore = null;
		InterfaceType[] sloStoreItfType = null;
		ComponentType sloStoreType = null;
		
		try {
			sloStoreItfType = new InterfaceType[] {
					patf.createGCMItfType(SLO_STORE_ITF, SLOStore.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(METRICS_NOTIF_ITF, MetricsListener.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MONITOR_SERVICE_ITF, MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(SLA_ALARM_ITF, SLANotifier.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY)
			};
			sloStoreType = patf.createFcType(sloStoreItfType);
			sloStore = pagf.newNfFcInstance(sloStoreType, 
					new ControllerDescription(SLO_STORE_COMP, Constants.PRIMITIVE, "/org/objectweb/proactive/core/component/componentcontroller/config/default-component-controller-config-basic.xml"), 
					new ContentDescription(sloStoreClass),
					node
			);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return sloStore;
	}
	*/
	
	private static Component createExecutionController(PAGCMTypeFactory patf, PAGenericFactory pagf, String clazz, Node node) {
		try {
			InterfaceType[] itfTypes = new InterfaceType[] {
					patf.createGCMItfType(ExecutionController.ITF_NAME, ExecutionController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			};
			return pagf.newNfFcInstance(patf.createFcType(itfTypes), 
					new ControllerDescription(EXECUTION_CONTROLLER_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH), 
					new ContentDescription(clazz), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Component createAnalysisController(PAGCMTypeFactory patf, PAGenericFactory pagf, String clazz, Node node) {
		try {
			InterfaceType[] itfTypes = new InterfaceType[] {
					patf.createGCMItfType(AnalysisController.ITF_NAME, AnalysisController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MetricEventListener.ITF_NAME, MetricEventListener.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(MonitorController.ITF_NAME,	MonitorController.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY, PAGCMTypeFactory.SINGLETON_CARDINALITY),
					patf.createGCMItfType(ExecutionController.ITF_NAME, ExecutionController.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL, PAGCMTypeFactory.SINGLETON_CARDINALITY),
			};
			return pagf.newNfFcInstance(patf.createFcType(itfTypes), 
					new ControllerDescription(ANALYSIS_CONTROLLER_COMP, Constants.PRIMITIVE, CONFIG_FILE_PATH), 
					new ContentDescription(clazz), node);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
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
		
		for(InterfaceType itf : pacomponent.getComponentParameters().getComponentType().getFcInterfaceTypes()) {
			
			PAGCMInterfaceType itfType = (PAGCMInterfaceType) itf;
			boolean isSingleton = itfType.isGCMSingletonItf() && ! itfType.isGCMCollectiveItf();

			// Composite server interfaces, only singleton supported
			if(isComposite && isSingleton && !itfType.isFcClientItf()) {

				if (isAlreadyBound(itfType.getFcItfName() + "-internal-" + Constants.MONITOR_CONTROLLER, membrane)) {
					continue; // break loops
				}
			
				Component destComp = getItfOwnerComponentOrNull(itfType.getFcItfName(), bc);
				MonitorController internalMonitor = getMonitorControllerOrNull(destComp);
				
				if (internalMonitor == null) {
					continue; // assumes no monitors on purpose
				}
				
				try {
					membrane.stopMembrane();
					membrane.nfBindFc(itfType.getFcItfName() + "-internal-" + Constants.MONITOR_CONTROLLER, internalMonitor);
					membrane.startMembrane();
				} catch (Exception e) {
					e.printStackTrace();
					continue; // something fails
				}

				enableMonitoring(destComp);
			}

			// Singleton and Gathercast client interfaces
			else if (itfType.isFcClientItf() && (isSingleton || itfType.isGCMGathercastItf())) {

				if (isAlreadyBound(itfType.getFcItfName() + "-external-" + Constants.MONITOR_CONTROLLER, membrane)) {
					continue; // break loops
				}
				
				Component destComp = getItfOwnerComponentOrNull(itfType.getFcItfName(), bc);
		
				// ignore not PAComponentRepresentative (WSComponent for example)
				if ( !(destComp instanceof PAComponentRepresentative) ) continue;
				
				boolean isNotParent = !destComp.equals(parent);
				MonitorController externalMonitor = isNotParent ?
						getMonitorControllerOrNull(destComp) : getInternalMonitorControllerOrNull(destComp);
				
				if (externalMonitor == null) {
					continue; // assumes no monitors on purpose
				}

				try {
					membrane.stopMembrane();
					membrane.nfBindFc(itfType.getFcItfName() + "-external-" + Constants.MONITOR_CONTROLLER, externalMonitor);
					membrane.startMembrane();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (isNotParent) {
					enableMonitoring(destComp);
				}
			}
			
			// Multicast client interfaces
			else if (itfType.isFcClientItf() && itfType.isGCMMulticastItf()) {

				try {
					PAMulticastController pamc = Utils.getPAMulticastController(pacomponent);
					
					// Get destination components
					Object[] destinationItfs = pamc.lookupGCMMulticast(itfType.getFcItfName());
					Component[] destinations = new Component[destinationItfs.length];
					for(int i=0; i<destinationItfs.length; i++) {
						destinations[i] = ((PAInterface) destinationItfs[i]).getFcItfOwner();
					}
					
					// Get already NF bound destination components
					destinationItfs = pamc.lookupGCMMulticast(itfType.getFcItfName()+"-external-"+Constants.MONITOR_CONTROLLER);
					Component[] currentNFBoundDestinations = new Component[destinationItfs.length];
					for (int i = 0; i < destinationItfs.length; i++) {
						currentNFBoundDestinations[i] = ((PAInterface) destinationItfs[i]).getFcItfOwner();
					}

					for(Component destComp : destinations) {
						
						// discard already NF bound
						boolean alreadyBound = false;
						for (Component boundDestination : currentNFBoundDestinations) {
							if (destComp.equals(boundDestination)) {
								alreadyBound = true;
								break;
							}
						}
						if (alreadyBound) continue;
					
						//  ignore not PAComponentRepresentative (WSComponent for example)
						if (!(destComp instanceof PAComponentRepresentative)) continue;

						boolean isNotParent = !destComp.equals(parent);
						MonitorController externalMonitor = isNotParent ?
								getMonitorControllerOrNull(destComp) : getInternalMonitorControllerOrNull(destComp);
								
						if (externalMonitor == null) {
							continue; // assumes no monitors on purpose
						}
			
						try {
							membrane.stopMembrane();
							membrane.nfBindFc(itfType.getFcItfName() + "-external-" + Constants.MONITOR_CONTROLLER, externalMonitor);
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


	// UTILS

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
			return (MonitorController) comp.getFcInterface(INTERNAL_MON_ITF);
		} catch(Exception e) {
			return null;
		}
	}
	
	private static Component getItfOwnerComponentOrNull(String interfaceName, BindingController bindingController) {
		try {
			return ((PAInterface) bindingController.lookupFc(interfaceName)).getFcItfOwner();
		} catch(NoSuchInterfaceException nsie) {
			return null;
		} 
	}

	public static MonitorController getMonitorController(Component component) throws NoSuchInterfaceException {
		return (MonitorController) component.getFcInterface(Constants.MONITOR_CONTROLLER);
	}
	
	public static AnalysisController getAnalysisController(Component component) throws NoSuchInterfaceException {
		return (AnalysisController) component.getFcInterface(Constants.ANALYSIS_CONTROLLER);
	}
	
	public static ExecutionController getExecutionController(Component component) throws NoSuchInterfaceException {
		return (ExecutionController) component.getFcInterface(Constants.EXECUTION_CONTROLLER);
	}
}
