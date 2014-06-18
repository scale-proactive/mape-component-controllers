package org.objectweb.proactive.extensions.autonomic.adl;

import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.PABasicFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;

public class ABasicFactory extends PABasicFactory implements AFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Object newAutonomicComponent(String name, Map context) throws Exception {
		Component comp = (Component) super.newComponent(name, context);
		addAutonomicControlles(comp);
		return comp;
	}

	protected void addAutonomicControlles(Component comp) throws Exception {
		
		Remmos.addMonitoring(comp);
		Remmos.addAnalysis(comp);
		Remmos.addPlannerController(comp);
		Remmos.addExecutorController(comp);

		if (((PAComponent) comp).getComponentParameters().getHierarchicalType().equals(Constants.COMPOSITE)) {
			for (Component subComp : Utils.getPAContentController(comp).getFcSubComponents()) {
				addAutonomicControlles(subComp);
			}
		}
	}
}
