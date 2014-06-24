package org.objectweb.proactive.extensions.autonomic.console;

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
		Wrapper<String> result = console.executor.execute(args);
		if (result.isValid()) {
			console.printMsg(result.getValue());
		} else {
			console.printError(result.getMessage());
		}
	}

}
