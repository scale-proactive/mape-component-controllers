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
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.AnalyzerController;
import org.objectweb.proactive.extensions.autonomic.controllers.analysis.Rule;
import org.objectweb.proactive.extensions.autonomic.controllers.remmos.Remmos;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

/**
 * A GCM procedure to implement the <code>new-rule()</code> action which instantiates a new <code>Rule</code>.
 * 
 */
public class NewRuleAction extends AbstractGCMProcedure {

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
		String ruleName = (String) args.get(1);
		String ruleImplementation = (String) args.get(2);

		if (target instanceof GCMComponentNode) {
			return createRule((GCMComponentNode) target, ruleName, ruleImplementation, ctx);
		}
		
		if (target instanceof Set) {
			Set<RuleNode> resultSet = new HashSet<RuleNode>();
			for (Object t : (Set<?>) target) {
				resultSet.add(createRule((GCMComponentNode) t, ruleName, ruleImplementation, ctx));
			}
			return resultSet;
		}

		throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, "Unknown error"));
	}

	protected RuleNode createRule(GCMComponentNode componentNode, String ruleName, String ruleImplementation,
			Context ctx) throws ScriptExecutionError {

		Rule rule = null;
		try {
			Class<?> clazz = Class.forName(ruleImplementation);
			Constructor<?> constructor = clazz.getConstructor();
			rule = (Rule) constructor.newInstance();
		} catch (ClassNotFoundException e) {
			String msg = "Class not found: " + ruleImplementation;
	    	throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, msg), e);
		} catch (NoSuchMethodException | SecurityException e) {
			String msg = "Failed while trying to get a constructor for the rule " + ruleName;
			msg += " using " + ruleImplementation;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException e) {
			String msg = "Failed while trying to instantiate the rule " + ruleName + " using " + ruleImplementation;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		AnalyzerController analyzerController;
		try {
			analyzerController = Remmos.getAnalyzerController(componentNode.getComponent());
		} catch (NoSuchInterfaceException e) {
			String msg = "Failed while trying to get analyzer controller from component " + componentNode.getName();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}

		Wrapper<Boolean> result = analyzerController.addRule(ruleName, rule);
		if (!result.isValid() || !result.getValue()) {
			String msg = "Failed while trying to add the new rule to analyzer controller: " + result.getMessage();
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg));
		}
	
		return ((AGCMModel) model).createRuleNode(analyzerController, ruleName);
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
