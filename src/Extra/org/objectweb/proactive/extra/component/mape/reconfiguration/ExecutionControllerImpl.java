/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.component.mape.reconfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.fscript.FScript;
import org.objectweb.fractal.fscript.FScriptEngine;
import org.objectweb.fractal.fscript.InvalidScriptException;
import org.objectweb.fractal.fscript.ScriptLoader;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extra.component.fscript.GCMScript;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;
import org.objectweb.proactive.extra.component.fscript.model.GCMNodeFactory;

/**
 * Reconfiguration component embedding a PAGCMScript engine.
 * It's an adaptation of the the PAReconfigurationController added by Bastien.
 * 
 * @author cruz
 *
 */

@SuppressWarnings("serial")
public class ExecutionControllerImpl extends AbstractPAComponentController implements ExecutionController {

    /** The {@link ScriptLoader} used by the controller. */
    private transient ScriptLoader loader;
    /** The {@link FScriptEngine} used the controller. */
    private transient FScriptEngine engine;

	private PAGCMTypeFactory patf;
	private PAGenericFactory pagf;

	// executable name --> executable
	private Map<String, Executable> executables = new HashMap<String, Executable>();


    /**
     * Instantiates a new ProActive/GCM Script engine from the default ProActive/GCM Script ADL file and set it as
     * default engine for the controller.
     *
     * @throws ReconfigurationException If an error occurred during the instantiation.
     */
	public void setNewEngineFromADL() throws ReconfigurationException {
		System.out.println("Initializing with "+ GCMScript.GCMSCRIPT_ADL);
		setNewEngineFromADL(GCMScript.GCMSCRIPT_ADL);		
	}

	/**
     * Instantiates a new ProActive/GCM Script engine from an ADL file and set it as default engine for the
     * controller.
     *
     * @param adlFile The ADL file name containing the ProActive/GCM Script architecture to instantiate and to set
     * as default engine for the controller.
     * @throws ReconfigurationException If an error occurred during the instantiation.
     */
	public void setNewEngineFromADL(String adlFile) throws ReconfigurationException {
		PAComponent owner = hostComponent;
		try {
			String defaultFcProvider = null;
			try {
				defaultFcProvider = System.getProperty("fractal.provider");
			} catch (NullPointerException npe) {
				// No fractal.provider system property defined
			}
			System.setProperty("fractal.provider", "org.objectweb.fractal.julia.Julia");
			Component fscript = GCMScript.newEngineFromAdl(adlFile);
			loader = FScript.getScriptLoader(fscript);
			engine = FScript.getFScriptEngine(fscript);
			engine.setGlobalVariable("this", ((GCMNodeFactory) FScript.getNodeFactory(fscript))
					.createGCMComponentNode(owner));
	
			if (defaultFcProvider != null) {
				System.setProperty("fractal.provider", defaultFcProvider);
			}
		} catch (Exception e) {
			throw new ReconfigurationException("Unable to set new engine for reconfiguration controller", e);
		}
	}

    /**
     * Checks if the {@link ScriptLoader} and the {@link FScriptEngine} have been initialized. If not, the
     * instantiation is done by using {@link #setNewEngineFromADL()}.
     *
     * @throws ReconfigurationException If an error occurred during the instantiation.
     */
    private void checkInitialized() throws ReconfigurationException {
        if ((loader == null) || (engine == null)) {
            setNewEngineFromADL();
        }
    }

    /**
     * Loads procedure definitions from a file containing source code, and make them available for later invocation
     * by name.
     *
     * @param fileName The name of the file containing the source code of the procedure definitions.
     * @return The names of all the procedures successfully loaded.
     * @throws ReconfigurationException If errors were detected in the procedure definitions.
     */
    public Set<String> load(String fileName) throws ReconfigurationException {
        checkInitialized();
        try {
            return loader.load(new FileReader(fileName));
        } catch (FileNotFoundException fnfe) {
            throw new ReconfigurationException("Unable to load procedure definitions", fnfe);
        } catch (InvalidScriptException ise) {
            throw new ReconfigurationException("Unable to load procedure definitions\n" + ise.getMessage());
        }
    }
    
    /**
     * Returns the names of all the currently defined global variables.
     *
     * @return The names of all the currently defined global variables.
     * @throws ReconfigurationException If an error occurred while getting global variable names.
     */
    public Set<String> getGlobals() throws ReconfigurationException {
        checkInitialized();
        return engine.getGlobals();
    }
    
    /**
     * Execute a code fragment: either an FPath expression or a single FScript statement.
     *
     * @param source The code fragment to execute.
     * @return The value of the code fragment, if successfully executed.
     */
    public Object execute(String source) {
        try {
        	checkInitialized();
        	return this.engine.execute(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "EXECUTION_FAIL";
    }

    private void executeSource(String source) {

    }
    
	@Override
	public boolean addAction(String name, Action action) {
		if (executables.containsKey(name)) return false;
		return executables.put(name, action) == null;
	}

	@Override
	public boolean addActionScript(String name, String script) {
		if (executables.containsKey(name)) return false;
		return executables.put(name, new ActionScript(script)) == null;
	}

	@Override
	public void removeAction(String name) {
		executables.remove(name);
	}

	@Override
	public Object executeAction(String actionName) {
		
		Executable exec = executables.get(actionName);
		if (exec == null) {
			(new Exception("Action \"" + actionName + "\" not found.")).printStackTrace();
		}
		
		if (exec instanceof Action) {
			checkFactories();
			return ((Action) exec).execute(this.hostComponent, patf, pagf);
		} else if (exec instanceof ActionScript){
			return execute(((ActionScript) exec).getScript());
		} else {
			(new Exception("Can not handle the action \"" + actionName + "\"")).printStackTrace();
		}
		return null;
	}

	@Override
	public Object executeAction(Action action) {
		checkFactories();
		return action.execute(this.hostComponent, patf, pagf);
	}

	/**
	 * Instantiates the factories needed to execute Actions
	 * 
	 */
	private void checkFactories() {
		if (patf == null || pagf == null) {
			try {
				Component boot = Utils.getBootstrapComponent();
				patf = Utils.getPAGCMTypeFactory(boot);
				pagf = Utils.getPAGenericFactory(boot);
			} catch (InstantiationException | NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}
	}

}
