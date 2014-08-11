package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractNode;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class ActionNode extends AbstractNode {

	/**
     * The name of this action.
     */
    private final String actionName;

	/**
     * The ExecutorController used to access this action.
     */
    private final ExecutorController executorController;

    /**
     * The owner component
     */
    private final Component owner;

    /**
     * Creates a new {@link ExecutorNode}.
     *
     * @param model The GCM model the node is part of.
     * @param monitorController The ExecutorController of the component containing this action.
     * @param metricName The name of the action.
     */
	public ActionNode(FractalModel model, Component owner, String actionName) {
		super(model.getNodeKind("jaction"));
		if (owner == null || actionName == null) {
			throw new NullPointerException();
		}

		this.owner = owner;
		this.actionName = actionName;
		try {
			this.executorController = Remmos.getExecutorController(owner);
		} catch (NoSuchInterfaceException e) {
			throw new NullPointerException(e.getMessage());
		}
	}

	/**
     * Returns the current value of one of the node's properties.
     *
     * @param name The name of the property to access.
     * @return The current value of the named property for the node.
     * @throws NoSuchElementException If the node does not have a property of the given name.
     */
	@Override
	public Object getProperty(String name) {
		if ("name".equals(name)) {
            return getName();
        } else if ("info".equals(name)) {
            return getInfo();
        } else if ("execute".equals(name)) {
            return execute();
        } else {
            throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
	}

	@Override
	public void setProperty(String name, Object value) {
		checkSetRequest(name, value);
        throw new NoSuchElementException("Invalid property name '" + name + "'");
	}

	public String getName() {
		return actionName;
	}

	public Object getInfo() {
		// TODO ---
		return "TODO....";
	}

	public Object execute() {
		Wrapper<Serializable> result = executorController.executeAction(actionName);
		return result.isValid() ? result.getValue() : result.getMessage();
 	}

	public Wrapper<Boolean> remove() {
		return executorController.removeAction(actionName);
	}

	public Component getOwner() {
		return owner;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "#<jaction: " + actionName + ">";
    }

}
