package tests.actions;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.execution.Action;

public class FooAction extends Action {

	private static final long serialVersionUID = 1L;

	@Override
	public Object execute(Component component, PAGCMTypeFactory typeFactory, PAGenericFactory genericFactory) {
		System.out.println("It works! ................................. ");
		return "It works!";
	}

}
