package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class SubscriptionAxis extends AbstractAxis {

	public SubscriptionAxis(FractalModel model) {
        super(model, "subscription", "rule", "metric");
	}

	@Override
	public boolean isModifiable() {
		return true;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public Set<Node> selectFrom(Node source) {
		
		RuleNode ruleNode = (RuleNode) source;
		Component owner = ruleNode.getOwner();

		Set<Node> metricNodes = new HashSet<Node>();

		Wrapper<HashSet<String>> subs;
		try {
			subs = Remmos.getAnalyzerController(owner).getRuleSubscriptions(ruleNode.getName());
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
			return metricNodes;
		}

		if (!subs.isValid()) {
			// make some noise
			String msg = "Fail to retrieve subscribed metrics: " + subs.getMessage();
			(new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg))).printStackTrace();
			return metricNodes;
		}

		AGCMModel amodel = (AGCMModel) model;
		for (String metricName : subs.getValue()) {
			metricNodes.add(amodel.createMetricNode(owner, metricName));
		}

		return metricNodes;
	}

	@Override
	public void connect(Node source, Node dest) {
		RuleNode ruleNode = (RuleNode) source;
		MetricNode metricNode = (MetricNode) dest;

		AnalyzerController analyzer;
		try {
			analyzer = Remmos.getAnalyzerController(ruleNode.getOwner());
		} catch (NoSuchInterfaceException e) {
			throw new IllegalArgumentException("Fail to get access to AnalyzerController", e);
		}
		
		Wrapper<Boolean> result = analyzer.subscribeRuleTo(ruleNode.getName(), metricNode.getName());

		if (!result.isValid() || !result.getValue()) {
			throw new IllegalArgumentException("Fail during the subscription: " + result.getMessage());
		}
	}

}
