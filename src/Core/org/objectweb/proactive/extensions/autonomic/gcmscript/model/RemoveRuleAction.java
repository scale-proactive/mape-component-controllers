package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import java.util.List;

import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.VoidType;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;

public class RemoveRuleAction extends AbstractGCMProcedure {

	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
		RuleNode node = (RuleNode) args.get(0);
		Wrapper<Boolean> result = node.remove();
		if (!result.isValid() || !result.getValue()) {
			String msg = "Failed while trying to remove the rule: " + result.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}
		return null;
	}

	@Override
	public String getName() {
		return "remove-rule";
	}

	@Override
	public Signature getSignature() {
		return new Signature(VoidType.VOID_TYPE, model.getNodeKind("rule"));
	}

	@Override
	public boolean isPureFunction() {
		return false;
	}

}
