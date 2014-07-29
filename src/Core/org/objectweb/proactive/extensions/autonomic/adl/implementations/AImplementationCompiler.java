package org.objectweb.proactive.extensions.autonomic.adl.implementations;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.implementations.PAImplementationCompiler;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;


public class AImplementationCompiler extends PAImplementationCompiler {

    public static final String AUTONOMIC_COMPONENT_CONFIG_FILE_LOCATION =
    		"/org/objectweb/proactive/extensions/autonomic/controllers/config/default-autonomic-component-config.xml";

    /**
     * This method override the {@link PAImplementationCompiler#compile} with the only purpose of replace
     * a call to the private method {@link PAImplementationCompiler#controllers} by the new method
     * {@link #setControllers}
     * 
     * @see PAImplementationCompiler#compile(List, ComponentContainer, TaskMap, Map)
     */
    @Override
    public void compile(List<ComponentContainer> path, ComponentContainer container, TaskMap tasks,
            Map<Object, Object> context) throws ADLException {

        //DEBUG
        String name = null;
        boolean f = true;
        if (container instanceof Definition) {
            name = ((Definition) container).getName();
        } else if (container instanceof Component) {
            name = ((Component) container).getName();
        }
        f = (container.astGetDecoration("NF") == null);
        logger.debug("[PAImplementationCompiler] Compiling " + (f ? "F" : "NF") + " component: " + name);
        //--DEBUG

        // collect info required for creating the component
        ObjectsContainer obj = init(path, container, tasks, context);
        // determines content description and controller description info
        setControllers(obj.getImplementation(), obj.getController(), obj.getName(), obj);
        // create the task that will be in charge of creating the component
        end(tasks, container, context, obj.getName(), obj.getDefinition(), obj.getControllerDesc(), obj
                .getContentDesc(), obj.getVn(), obj.isFunctional());
    }


    /** 
     * Completes the collected ObjectsContainer with the ContentDescription and the
     * ControllerDescription objects.<br/><br/>
     * 
     * Determines the hierarchical type (composite/primitive) by checking if the component has 
     * subcomponents or not. In fact, a composite may have an implementation class, in case that
     * the composite must provide an AttributeController interface (so, checking that implementation==null
     * is not enough).<br/><br/>
     * 
     * @param implementation the implementor class
     * @param controller the controller definition (composite, primitive or path to desc file)
     * @param name the name of the component
     * @param obj the ObjectContainers to update
     */
    protected void setControllers(String implementation, String controller, String name, ObjectsContainer obj) {
        ContentDescription contentDesc = null;
        ControllerDescription controllerDesc = null;

        Active active = new ComponentRunActive() {
			public void runComponentActivity(Body body) {
				(new ComponentMultiActiveService(body)).multiActiveServing();
			}
		};

        if (implementation == null) {
            // a composite component without attributes
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		getControllerPath(AUTONOMIC_COMPONENT_CONFIG_FILE_LOCATION, name));
                contentDesc = new ContentDescription(Composite.class.getName(), null, active, null);
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		getControllerPath(controller, name));
                // contentDesc ???
            }

        } else if (obj.hasSubcomponents()) {
            // a composite component with attributes 
            //    in that case it must have an Attributes node, and the class implementation must implement
            //    the Attributes signature
            contentDesc = new ContentDescription(implementation, null, active, null);

            // treat it as a composite
            if ("composite".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		getControllerPath(AUTONOMIC_COMPONENT_CONFIG_FILE_LOCATION, name));
            } else {
                controllerDesc = new ControllerDescription(name, Constants.COMPOSITE,
                		getControllerPath(controller, name));
            }

        } else {
            // a primitive component
            contentDesc = new ContentDescription(implementation, null, active, null);

            if ("primitive".equals(controller) || (controller == null)) {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE,
                		getControllerPath(AUTONOMIC_COMPONENT_CONFIG_FILE_LOCATION, name));
            } else {
                controllerDesc = new ControllerDescription(name, Constants.PRIMITIVE,
                		getControllerPath(controller, name));
            }
        }

        // update the ObjectsContainer object
        obj.setContentDesc(contentDesc);
        obj.setControllerDesc(controllerDesc);
    }

}
