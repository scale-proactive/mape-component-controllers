package org.objectweb.proactive.extensions.autonomic.console;

import java.util.Set;

import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;

public class ChangeCommand extends AbstractCommand {

	public ChangeCommand(Console console) {
		super(console);
	}

	@Override
	public String getName() {
		return "cc";
	}

	@Override
	public String getInfo() {
		return "Change the current terminal position to other component";
	}

	@Override
	public void execute(String args) throws Exception {
		Wrapper<PAComponent> result = console.executorController.execute(args);
		if (result.isValid()) {
			Object value = result.getValue();
			// case component
			if (value instanceof PAComponent) {
				console.currentComponent((PAComponent) value);
				return;
			}
			// case list of components
			if (value instanceof Set && ((Set<?>) value).size() >= 0) {
				Object element = ((Set<?>) value).toArray()[0];
				if (element instanceof PAComponent) {
					console.currentComponent((PAComponent) element);
					return;
				}
			}
			// invalid case
			console.printError("The argument must be a component");
		} else {
			console.printError(result.getMessage());
		}
	}

}
