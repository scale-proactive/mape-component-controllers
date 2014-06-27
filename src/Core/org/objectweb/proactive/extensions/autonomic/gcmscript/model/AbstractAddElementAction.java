package org.objectweb.proactive.extensions.autonomic.gcmscript.model;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.ast.SourceLocation;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.fractal.fscript.types.Type;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

/**
 * For element we must understand: Metric, Rule, Plan, Action.
 * 
 * This class is used instead of the Connector/Disconnector procedures to avoid
 * the necessity of create a element node before connect it with the component node.
 * To create a element node a reference to the owner component is needed, so the
 * post-instantiation connection is redundant.
 * 
 * @author mibanez
 *
 */
public abstract class AbstractAddElementAction extends AbstractGCMProcedure {

	/**
	 * Returns the return type of this add actions. <br/>
	 * Used to build the signature of the action.
	 * @return
	 */
	public abstract Type getReturnType();

	/**
	 * All the add actions has the same format. The component where to add the new element,
	 * the name of the new element, and implementation of the new element (as a string).<br/>
	 * The return type must be provided by implementing {@link #getReturnType()}.
	 */
	@Override
	public Signature getSignature() {
		return new Signature(getReturnType(), model.getNodeKind("component"), STRING, STRING);
	}

	/**
	 * False means that this procedure has side-effects on the system.
	 * @return false
	 */
	@Override
	public boolean isPureFunction() {
		return false;
	}

	@Override
	public Object apply(List<Object> args, Context ctx) throws ScriptExecutionError {

		Object target = args.get(0);
		String elementName = (String) args.get(1);
		String elementImpl = (String) args.get(2);

		if (target instanceof GCMComponentNode) {
			return createElement((GCMComponentNode) target, elementName, elementImpl, ctx);
		}
		
		if (target instanceof Set) {
			Set<Object> resultSet = new HashSet<Object>();
			for (Object t : (Set<?>) target) {
				resultSet.add(createElement((GCMComponentNode) t, elementName, elementImpl, ctx));
			}
			return resultSet;
		}

		throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, "Unknown error"));
	}

	/**
	 * Here the element is instantiated. The details and the final returned object must be implemented
	 * on {@link #addElement(GCMComponentNode, String, Object)}.
	 * @param target the component node where the element will be added
	 * @param elementName the name of the element
	 * @param elementImpl the name of the class that implements the element
	 * @param ctx the context
	 * @return any desired object (see {@link #addElement(GCMComponentNode, String, Object)})
	 * @throws ScriptExecutionError in case of failure
	 */
	protected Object createElement(GCMComponentNode target, String elementName, String elementImpl, Context ctx)
			throws ScriptExecutionError {

		try {

			Class<?> clazz = Class.forName(elementImpl);
			Constructor<?> constructor = clazz.getConstructor();
			Object obj = constructor.newInstance();
	
			return addElement(target, elementName, obj);
	
		} catch (ClassNotFoundException e) {
			String msg = "Class not found: " + elementImpl;
	    	throw new ScriptExecutionError(Diagnostic.error(SourceLocation.UNKNOWN, msg), e);
		} catch (NoSuchMethodException | SecurityException e) {
			String msg = "Failed while trying to get a constructor for " + elementImpl;
			msg += " using " + elementImpl;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException e) {
			String msg = "Failed while trying to instantiate " + elementName + " using " + elementImpl;
	    	throw new ScriptExecutionError(Diagnostic.warning(SourceLocation.UNKNOWN, msg), e);
		}
	}

	/**
	 * Adds the instance of the element into the target component. A node representing this new element is expected
	 * to e returned.
	 * @param target the node of the component to put the element in
	 * @param elementName the name of the element
	 * @param element and instantiation of the element
	 * @return the node of the new added element
	 * @throws ScriptExecutionError in case of failure
	 */
	protected abstract Object addElement(GCMComponentNode target, String elementName, Object element)
			throws ScriptExecutionError;

}
