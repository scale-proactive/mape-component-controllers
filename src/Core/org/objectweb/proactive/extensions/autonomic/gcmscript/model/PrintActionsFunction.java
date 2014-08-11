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
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

public class PrintActionsFunction extends AbstractGCMProcedure {

	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
		GCMComponentNode node = (GCMComponentNode) args.get(0);
		
		ExecutorController executor = null;
		try {
			executor = Remmos.getExecutorController(node.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get executor controller from component " + node.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}
	
		Wrapper<HashSet<String>> actions = executor.getActionNames();
		if (!actions.isValid()) {
			String msg = "Failed while trying to get the list of metrics: " + actions.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}

		String msg = "\t(NAME)\t(INFO)\n";

		for (String actionName : actions.getValue()) {
			Wrapper<String> info = new ValidWrapper<String>("--"); //executor.getActionInfo(actionName);
			if (!info.isValid()) {
				String err = "Failed while trying to get the info for action \"" + actionName + "\"";
				err += ":" + info.getMessage();
		    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, err));
			}
			
			msg += "\t" + actionName + "\t" + info.getValue() + "\n";
		}

		return msg += "\t --- (END) ---\n";
	}

	@Override
	public String getName() {
		return "print-jactions";
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
