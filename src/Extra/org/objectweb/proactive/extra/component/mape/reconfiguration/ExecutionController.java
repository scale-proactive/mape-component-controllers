package org.objectweb.proactive.extra.component.mape.reconfiguration;

import org.objectweb.proactive.extra.component.fscript.control.PAReconfigurationController;

public interface ExecutionController extends PAReconfigurationController {

	// DEFAULT NAME
	public static final String ITF_NAME = "execution-controller-nf";

	/**
	 * Adds a new Action. This Action will be stored and can be executed by {@link #executeAction(String)}
	 * using its name.
	 * @param name unique name to identify this action.
	 * @param action the action to be stored
	 * @return true if success
	 */
	public boolean addAction(String name, Action action);
	
	/**
	 * Adds a new Action. This Action will be stored and can be executed by {@link #executeAction(String)}
	 * using its name.
	 * @param name unique name to identify this action.
	 * @param script a script in GCMScript language.
	 * @return true if success
	 */
	public boolean addActionScript(String name, String script);


	/**
	 * Remove the action identified by the given name.
	 * @param name the name of the action to be removed
	 */
	public void removeAction(String name);

	/**
	 * Executes the stored action identified by the given name
	 * @param actionName name of the stored Action to be executed
	 */
	public void executeAction(String actionName);
	
	/**
	 * Executes the given Action
	 * @param action Action to be executed
	 */
	public void executeAction(Action action);

}
