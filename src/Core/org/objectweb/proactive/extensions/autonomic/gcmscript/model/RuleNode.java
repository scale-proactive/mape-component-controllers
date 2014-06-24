package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.NoSuchElementException;

import org.objectweb.fractal.fscript.model.AbstractNode;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Alarm;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class RuleNode extends AbstractNode {

	/**
     * The name of this rule.
     */
    private final String ruleName;

	/**
     * The AnalyzerController used to access this rule.
     */
    private final AnalyzerController analyzerController;


    /**
     * Creates a new {@link RuleNode}.
     *
     * @param model The GCM model the node is part of.
     * @param monitorController The MonitorController of the component containing this metric.
     * @param metricName The name of the metric.
     */
	public RuleNode(FractalModel model, AnalyzerController analyzerController, String ruleName) {
		super(model.getNodeKind("rule"));
		if (analyzerController == null || ruleName == null) {
			throw new NullPointerException();
		}

		this.ruleName = ruleName;
		this.analyzerController = analyzerController;
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
        } else if ("check".equals(name)) {
            return check();
        } else {
            throw new NoSuchElementException("Invalid property name '" + name + "'.");
        }
	}

	@Override
	public void setProperty(String name, Object value) {
		throw new NoSuchElementException("Invalid property name '" + name + "'");
	}

	public String getName() {
		return ruleName;
	}

	public String check() throws NoSuchElementException {
		Wrapper<Alarm> alarm = analyzerController.checkRule(ruleName);
		if (alarm.isValid()) {
			return alarm.getValue().toString();
		}
		throw new NoSuchElementException(alarm.getMessage());
	}

}
