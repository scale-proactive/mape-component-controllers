package org.objectweb.proactive.extensions.autonomic.console;

import java.io.Serializable;

import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class ExecCommand extends AbstractCommand {

	public ExecCommand(Console console) {
		super(console);
	}

	@Override
	public String getName() {
		return "exec";
	}

	@Override
	public String getInfo() {
		return "Executes a GCMScript sentence";
	}

	@Override
	public void execute(String args) throws Exception {
		Wrapper<Serializable> result = console.executorController.execute(args);
		if (result.isValid()) {
			if (result.getValue() instanceof PAComponent) {
				console.printMsg(((PAComponent) result.getValue()).getComponentParameters().getName());
			} else {
				console.printMsg(result.getValue().toString());
			}
		} else {
			console.printError(result.getMessage());
		}
	}

}
