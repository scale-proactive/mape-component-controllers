package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMInterfaceNode;

public class ActionAxis extends AbstractAxis {

	public ActionAxis(FractalModel model) {
        super(model, "jaction", "component", "jaction");
	}

	@Override
	public boolean isModifiable() {
		return true;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

    /**
     * Locates all the destination nodes the given source node is connected to through
     * this axis.
     * 
     * @param source
     *            the source node from which to select adjacent nodes.
     * @return all the destination nodes the given source node is connected to through
     *         this axis.
     */
    @Override
    public Set<Node> selectFrom(Node source) {
        Component comp = null;
        if (source instanceof GCMComponentNode) {
            comp = ((GCMComponentNode) source).getComponent();
        } else if (source instanceof GCMInterfaceNode) {
            comp = ((GCMInterfaceNode) source).getInterface().getFcItfOwner();
        } else {
        	throw new IllegalArgumentException("Invalid source node kind " + source.getKind());
        }

        Set<Node> result = new HashSet<Node>();
        try {
        	ExecutorController executorController = Remmos.getExecutorController(comp);
        	Wrapper<HashSet<String>> actionNames = executorController.getActionNames();
        	if (actionNames.isValid()) {
				for (String actionName : actionNames.getValue()) {
					Node node = ((AGCMModel) model).createActionNode(comp, actionName);
					result.add(node);
				}
        	} else {
        		// warn making some noise
        		String msg = "ExecutorController detected, but failed to get the action names: ";
        		(new Exception(msg + actionNames.getMessage())).printStackTrace();
        	}
		} catch (NoSuchInterfaceException e) {
			// continue silently
		}

        return result;
    }

    @Override
    public void connect(Node source, Node dest) {
    	
    }

    @Override
    public void disconnect(Node source, Node dest) {
    	
    }

}
