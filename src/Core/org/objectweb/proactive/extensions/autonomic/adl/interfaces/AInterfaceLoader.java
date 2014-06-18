package org.objectweb.proactive.extensions.autonomic.adl.interfaces;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.proactive.core.component.adl.interfaces.PAInterfaceLoader;
import org.objectweb.proactive.core.component.adl.types.PATypeInterface;
import org.objectweb.proactive.extensions.autonomic.controllers.ACConstants;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorControllerMulticast;
import org.objectweb.proactive.extensions.autonomic.controllers.planning.PlannerController;

import com.google.gson.Gson;

public class AInterfaceLoader extends PAInterfaceLoader{

	private Gson gson = new Gson();

    /**
     * Looks for containers of &lt;interface&gt; nodes.
     * 
     * @param node
     * @throws ADLException
     */
	@Override
    protected void checkNode(final Object node, boolean functional) throws ADLException {

        //logger.debug("[PAInterfaceLoader] Analyzing node "+ node.toString()); 
        if (node instanceof InterfaceContainer) {
            checkInterfaceContainer((InterfaceContainer) node, functional);
        }

        // interfaces defined inside a <component> node are F, even if the component maybe NF
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, true);
            }
        }

        // interfaces defined inside a <controller> node are NF (i.e. they belong to the membrane)
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            if (ctrl != null) {
            	if (functional && node instanceof InterfaceContainer) {
            		addNFAutonomicInterfaces(ctrl, (InterfaceContainer) node);
            	}
                checkNode(ctrl, false);
            }
        }

    }
	
	/**
	 * Add the generated NF interfaces to the controller container, if it exists
	 * @param ctrl
	 * @param fItfContainer
	 * @throws ADLException 
	 */
	protected void addNFAutonomicInterfaces(Controller ctrl, InterfaceContainer itfContainer) throws ADLException {

		String hierarchy = ctrl.getDescriptor();
		if (hierarchy == null) {
			throw new ADLException(AInterfaceError.MISSING_CONTROLLER_DESCRIPTION);
		}
	
		InterfaceContainer nfItfContainer = (InterfaceContainer) ctrl;
		Interface[] functionalItfs = itfContainer.getInterfaces();

		// I need at least one interface as a reference.
		if (functionalItfs.length > 0) {

			// Generate a copy of this generated-class interface object
			Interface nfItf = gson.fromJson(gson.toJson(functionalItfs[0]), functionalItfs[0].getClass());
			
			Map<String, String> attr = nfItf.astGetAttributes();
			attr.put("name", ACConstants.MONITOR_CONTROLLER);
			attr.put("role", PATypeInterface.SERVER_ROLE);
			attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
			attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
			attr.put("signature", MonitorController.class.getName());
			nfItf.astSetAttributes(attr);
			nfItfContainer.addInterface(nfItf);
			
			nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
			attr = nfItf.astGetAttributes();
			attr.put("name", ACConstants.ANALYZER_CONTROLLER);
			attr.put("signature", AnalyzerController.class.getName());
			nfItf.astSetAttributes(attr);
			nfItfContainer.addInterface(nfItf);
			
			nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
			attr = nfItf.astGetAttributes();
			attr.put("name", ACConstants.PLANNER_CONTROLLER);
			attr.put("signature", PlannerController.class.getName());
			nfItf.astSetAttributes(attr);
			nfItfContainer.addInterface(nfItf);
			
			nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
			attr = nfItf.astGetAttributes();
			attr.put("name", ACConstants.EXECUTOR_CONTROLLER);
			attr.put("signature", ExecutorController.class.getName());
			nfItf.astSetAttributes(attr);
			nfItfContainer.addInterface(nfItf);

			// Add internal server nf autonomic interface on composite components.
			if (hierarchy.equals("composite")) {

				nfItf = gson.fromJson(gson.toJson(nfItf), nfItf.getClass());
				attr = nfItf.astGetAttributes();
				attr.put("name", ACConstants.INTERNAL_SERVER_NFITF);
				attr.put("role", PATypeInterface.INTERNAL_SERVER_ROLE);
				attr.put("signature", MonitorController.class.getName());
				nfItf.astSetAttributes(attr);
	
				nfItfContainer.addInterface(nfItf);
			}
		}

		for (final Interface fItf : functionalItfs) {

    		String role = fItf.astGetAttributes().get("role");

    		// Add internal client nf autonomic interface to monitor the inner bound component
    		if (role.equals(PATypeInterface.SERVER_ROLE) && hierarchy.equals("composite")) {
    			
    			Interface autonomicNFItf = gson.fromJson(gson.toJson(fItf), fItf.getClass());
    			
    			Map<String, String> attr = autonomicNFItf.astGetAttributes();
    			attr.put("name", attr.get("name") + ACConstants.INTERNAL_CLIENT_SUFFIX);
    			attr.put("role", PATypeInterface.INTERNAL_CLIENT_ROLE);
    			attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);
    			attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
    			attr.put("signature", MonitorController.class.getName());
        		autonomicNFItf.astSetAttributes(attr);

        		nfItfContainer.addInterface(autonomicNFItf);
    		}
  
    		// Add external client nf autonomic interface to monitor the external bound component
    		else if (role.equals(PATypeInterface.CLIENT_ROLE)) {
 
    			Interface autonomicNFItf = gson.fromJson(gson.toJson(fItf), fItf.getClass());
    			
    			Map<String, String> attr = autonomicNFItf.astGetAttributes();
    			attr.put("name", attr.get("name") + ACConstants.EXTERNAL_CLIENT_SUFFIX);
    			attr.put("role", PATypeInterface.CLIENT_ROLE);
    			attr.put("contingency", PATypeInterface.OPTIONAL_CONTINGENCY);

    			String cardinality = autonomicNFItf.astGetAttributes().get("cardinality");
  
    			if (cardinality != null && cardinality.equals(PATypeInterface.MULTICAST_CARDINALITY)) {
    				attr.put("signature", MonitorControllerMulticast.class.getName());
    			} else {
    				attr.put("cardinality", PATypeInterface.SINGLETON_CARDINALITY);
    				attr.put("signature", MonitorController.class.getName());
    			}
  
    			autonomicNFItf.astSetAttributes(attr);

        		nfItfContainer.addInterface(autonomicNFItf);
    		}   		
		}
	}

}
