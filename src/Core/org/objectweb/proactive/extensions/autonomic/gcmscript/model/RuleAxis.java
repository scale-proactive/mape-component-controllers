package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

/**
 * Implements the <code>rule</code> axis in FPath. This axis connects a component to its metrics (if any),
 * as defined in AnalyzerController. This axis is not modifiable.
 * 
 */
public class RuleAxis extends AbstractAxis {

	public RuleAxis(FractalModel model) {
        super(model, "rule", "component", "rule");
	}

	@Override
	public boolean isModifiable() {
		return false;
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
        } else {
        	throw new IllegalArgumentException("Invalid source node kind " + source.getKind());
        }

        Set<Node> result = new HashSet<Node>();
        try {
        	AnalyzerController analyzerController = Remmos.getAnalyzerController(comp);
        	Wrapper<HashSet<String>> ruleNames = analyzerController.getRulesNames();
        	if (ruleNames.isValid()) {
				for (String ruleName : ruleNames.getValue()) {
					Node node = ((AGCMModel) model).createRuleNode(comp, ruleName);
					result.add(node);
				}
        	} else {
        		// warn making some noise
        		String msg = "AnalyzerController detected, but failed to get the rule names: ";
        		(new Exception(msg + ruleNames.getMessage())).printStackTrace();
        	}
		} catch (NoSuchInterfaceException e) {
			// continue silently
		}

        return result;
    }

}
