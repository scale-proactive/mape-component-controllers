package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.NoSuchElementException;

import org.objectweb.fractal.fscript.model.AbstractNode;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;

/**
 * A {@link Node} which represents a Metric.
 * 
 */
public class MetricNode extends AbstractNode {

	/**
     * The name of this metric.
     */
    private final String metricName;

	/**
     * The MonitorController used to access this metric.
     */
    private final MonitorController monitorController;

    /**
     * Creates a new {@link MetricNode}.
     *
     * @param model The GCM model the node is part of.
     * @param monitorController The MonitorController of the component containing this metric.
     * @param metricName The name of the metric.
     */
	public MetricNode(FractalModel model, MonitorController monitorController, String metricName) {
		super(model.getNodeKind("metric"));
		this.metricName = metricName;
		this.monitorController = monitorController;
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
        } else if ("value".equals(name)) {
            return getValue();
        } else if ("calculate".equals(name)) {
            return calculate();
        } else if ("state".equals(name)) {
            return getState();
        } else {
            throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
	}

	@Override
	public void setProperty(String name, Object value) {
        checkSetRequest(name, value);
        if ("state".equals(name)) {
            setState((boolean) value);
        } else {
            throw new NoSuchElementException("Invalid property name '" + name + "'");
        }
	}

	public String getName() {
		return metricName;
	}

	public Object getValue() {
		return monitorController.getMetricValue(metricName).getValue();
	}

	public Object calculate() {
		return monitorController.calculateMetric(metricName).getValue();
	}

	public boolean getState() {
		return monitorController.getMetricState(metricName).getValue();
	}

	private void setState(boolean enabled) {
		monitorController.setMetricState(metricName, enabled).getValue();
	}
}
