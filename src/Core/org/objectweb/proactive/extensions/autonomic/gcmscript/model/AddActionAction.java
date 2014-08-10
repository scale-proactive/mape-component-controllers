package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.Action;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.ExecutorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

public class AddActionAction extends AbstractAddElementAction {

	@Override
	public String getName() {
		return "add-jaction";
	}

	@Override
	public Type getReturnType() {
		return model.getNodeKind("jaction");
	}

	@Override
	protected Object addElement(GCMComponentNode target, String elementName, Object element)
			throws ScriptExecutionError {

		Action action = (Action) element;
		ExecutorController executorController;
		try {
			executorController = Remmos.getExecutorController(target.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get executor controller from component " + target.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		Wrapper<Boolean> result = executorController.addAction(elementName, action);
		if (!result.isValid() || !result.getValue()) {
			String msg = "Failed while trying to add the new metric to monitor controller: " + result.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}

		return ((AGCMModel) model).createActionNode(target.getComponent(), elementName);
	}

}
