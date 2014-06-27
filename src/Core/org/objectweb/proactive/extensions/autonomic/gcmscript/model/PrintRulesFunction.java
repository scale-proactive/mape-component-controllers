package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

import java.util.HashSet;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

public class PrintRulesFunction extends AbstractGCMProcedure {

	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
		GCMComponentNode node = (GCMComponentNode) args.get(0);
		
		AnalyzerController analyzer = null;
		try {
			analyzer = Remmos.getAnalyzerController(node.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get analyzer controller from component " + node.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}
	
		Wrapper<HashSet<String>> rules = analyzer.getRulesNames();
		if (!rules.isValid()) {
			String msg = "Failed while trying to get the list of rules: " + rules.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}

		String info = "\t(NAME)\t(SUBSCRIPTIONS)\n";

		for (String ruleName : rules.getValue()) {
			Wrapper<HashSet<String>> subscriptions = analyzer.getRuleSubscriptions(ruleName);
			if (!subscriptions.isValid()) {
				String msg = "Failed while trying to get the subscriptions for metric \"" + ruleName + "\"";
				msg += ":" + subscriptions.getMessage();
		    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
			}
			
			String metricNames = "";
			for (String metricName : subscriptions.getValue()) {
				metricNames += metricName + ", ";
			}
			
			if (metricNames.equals("")) {
				metricNames = "(NO SUBSCRIPTIONS)";
			}

			info += "\t" + ruleName + "\t" + metricNames + "\n";
		}

		return info += "\t --- (END) ---\n";
	}

	@Override
	public String getName() {
		return "print-rules";
	}

	@Override
	public Signature getSignature() {
		return new Signature(STRING, model.getNodeKind("component"));
	}

	@Override
	public boolean isPureFunction() {
		return true;
	}
	

}
