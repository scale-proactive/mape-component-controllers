package org.objectweb.proactive.extensions.autonomic.controllers.execution;

import java.util.Set;

import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;


public interface ExecutorController {

    public static final String ITF_NAME = "execution-service-nf";

    /**
     * Loads procedure definitions from a file containing source code, and makes them available for later invocation
     * by name.
     *
     * @param fileName The name of the file containing the source code of the procedure definitions.
     * @return The names of all the procedures successfully loaded.
     * @throws ReconfigurationException If errors were detected in the procedure definitions.
     */
    Set<String> load(String fileName) throws ReconfigurationException;

    /**
     * Returns the names of all the currently defined global variables.
     *
     * @return The names of all the currently defined global variables.
     * @throws ReconfigurationException If an error occurred while getting global variable names.
     */
    Set<String> getGlobals() throws ReconfigurationException;

    /**
     * Executes a code fragment: either an FPath expression or a single FScript statement.
     *
     * @param source The code fragment to execute.
     * @return The value of the code fragment, if successfully executed.
     * @throws ReconfigurationException If an error occurred during the execution of the code fragment.
     */
    public ObjectWrapper execute(String source);

    // ----- API -----
    
	/**
	 * Adds a new Action. This Action will be stored and can be executed by {@link #executeAction(String)}
	 * using its name.
	 * @param name unique name to identify this action.
	 * @param action the action to be stored
	 * @return true if success
	 */
	public boolean addAction(String name, Action action);

	/**
	 * Remove the action identified by the given name.
	 * @param name the name of the action to be removed
	 */
	public void removeAction(String name);

	/**
	 * Executes the stored action identified by the given name
	 * @param actionName name of the stored Action to be executed
	 */
	public ObjectWrapper executeAction(String actionName);
	
	/**
	 * Executes the given Action
	 * @param action Action to be executed
	 */
	public ObjectWrapper executeAction(Action action);

}
