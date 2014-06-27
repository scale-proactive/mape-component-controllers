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
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

public class PrintMetricsFunction extends AbstractGCMProcedure {

	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {
		GCMComponentNode node = (GCMComponentNode) args.get(0);
		
		MonitorController monitor = null;
		try {
			monitor = Remmos.getMonitorController(node.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get monitor controller from component " + node.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}
	
		Wrapper<HashSet<String>> metrics = monitor.getMetricList();
		if (!metrics.isValid()) {
			String msg = "Failed while trying to get the list of metrics: " + metrics.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}

		String info = "\t(NAME)\t(STATE)\n";

		for (String metricName : metrics.getValue()) {
			Wrapper<String> state = monitor.getMetricState(metricName);
			if (!state.isValid()) {
				String msg = "Failed while trying to get the state for metric \"" + metricName + "\"";
				msg += ":" + state.getMessage();
		    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
			}
			
			info += "\t" + metricName + "\t" + state.getValue() + "\n";
		}

		return info += "\t --- (END) ---\n";
	}

	@Override
	public String getName() {
		return "print-metrics";
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
