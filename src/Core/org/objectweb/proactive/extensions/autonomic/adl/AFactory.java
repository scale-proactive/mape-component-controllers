package org.objectweb.proactive.extensions.autonomic.adl;

import java.util.Map;

import org.objectweb.proactive.core.component.adl.PAFactory;

public interface AFactory extends PAFactory {

	@SuppressWarnings("rawtypes")
	public Object newAutonomicComponent(String name, Map context) throws Exception;
	
}
