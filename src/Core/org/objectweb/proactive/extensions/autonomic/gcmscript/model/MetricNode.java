package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.NoSuchElementException;

import org.objectweb.fractal.fscript.model.AbstractNode;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

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
            setState((String) value);
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

	public Object getState() {
		return monitorController.getMetricState(metricName).getValue().toString();
	}

	private void setState(String state) {
		if (state.equals(Metric.ENABLED)) {
			Wrapper<Boolean> result = monitorController.enableMetric(metricName);
			if (!result.isValid() || !result.getValue()) {
				String details = metricName + ": " + result.getMessage();
				System.err.println("[Warning] Error while enabling metric " + details);
			}
		} else if (state.equals(Metric.DISABLED)) {
			Wrapper<Boolean> result = monitorController.disableMetric(metricName);
			if (!result.isValid() || !result.getValue()) {
				String details = metricName + ": " + result.getMessage();
				System.err.println("[Warning] Error while disabling metric " + details);
			}
		} else {
			String details = metricName + ": the state must be " + Metric.ENABLED + " or " + Metric.DISABLED;
			System.err.println("[Warning] Error while trying to change the state of meitrc " + details);
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "#<metric: " + metricName + ">";
    }
}
