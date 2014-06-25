package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.MonitorController;
import org.objectweb.proactive.extensions.autonomic.controllers.monitoring.metrics.Metric;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

public class NewMetricAction extends AbstractGCMProcedure {

	/**
	 * Apply this procedure with the specified arguments.
	 * @param args the arguments of the procedure call
	 * @param ctx the execution context in which to execute the procedure
	 * @return the value returned by the procedure
	 * @throws ScriptExecutionError if any error occurred during the execution of the procedure
	 */
	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {

		Object target = args.get(0);
		String metricName = (String) args.get(1);
		String metricImplementation = (String) args.get(2);

		if (target instanceof GCMComponentNode) {
			return createMetric((GCMComponentNode) target, metricName, metricImplementation, ctx);
		}
		
		if (target instanceof Set) {
			Set<MetricNode> resultSet = new HashSet<MetricNode>();
			for (Object t : (Set<?>) target) {
				resultSet.add(createMetric((GCMComponentNode) t, metricName, metricImplementation, ctx));
			}
			return resultSet;
		}

		throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, "Unknown error"));
	}

	protected MetricNode createMetric(GCMComponentNode componentNode, String metricName, String metricImplementation,
			Context ctx) throws ScriptExecutionError {

		Metric<?> metric = null;
		try {
			Class<?> clazz = Class.forName(metricImplementation);
			Constructor<?> constructor = clazz.getConstructor();
			metric = (Metric<?>) constructor.newInstance();
		} catch (ClassNotFoundException e) {
			String msg = "Class not found: " + metricImplementation;
	    	throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, msg), e);
		} catch (NoSuchMethodException | SecurityException e) {
			String msg = "Failed while trying to get a constructor for the metric " + metricName;
			msg += " using " + metricImplementation;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException e) {
			String msg = "Failed while trying to instantiate the metric " + metricName + " using " + metricImplementation;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		MonitorController monitorController;
		try {
			monitorController = Remmos.getMonitorController(componentNode.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get monitor controller from component " + componentNode.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		Wrapper<Boolean> result = monitorController.addMetric(metricName, metric);
		if (!result.isValid() || !result.getValue()) {
			String msg = "Failed while trying to add the new metric to monitor controller: " + result.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}
	
		return ((AGCMModel) model).createMetricNode(monitorController, metricName);
	}

	@Override
	public String getName() {
		return "new-rule";
	}

	@Override
	public Signature getSignature() {
		return new Signature(model.getNodeKind("rule"), model.getNodeKind("component"), STRING, STRING);
	}

	@Override
	public boolean isPureFunction() {
		return false;
	}

}
