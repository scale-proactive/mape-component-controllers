package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

/**
 * A GCM procedure to implement the <code>add-rule()</code> action which instantiates a new <code>Rule</code>.
 * 
 */
public class AddRuleAction extends AbstractAddElementAction {

	@Override
	public String getName() {
		return "add-rule";
	}

	@Override
	public Type getReturnType() {
		return model.getNodeKind("rule");
	}

	@Override
	protected Object addElement(GCMComponentNode target, String elementName, Object element)
			throws ScriptExecutionError {

		Rule rule = (Rule) element;
		AnalyzerController analyzerController;
		try {
			analyzerController = Remmos.getAnalyzerController(target.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get analyzer controller from component " + target.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		Wrapper<Boolean> result = analyzerController.addRule(elementName, rule);
		if (!result.isValid() || !result.getValue()) {
			String msg = "Failed while trying to add the new rule to analyzer controller: " + result.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}
	
		return ((AGCMModel) model).createRuleNode(analyzerController, elementName);
	}

}
