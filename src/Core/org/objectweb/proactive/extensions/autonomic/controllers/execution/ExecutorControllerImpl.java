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
package org.objectweb.proactive.extensions.autonomic.controllers.execution;

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
import org.objectweb.fractal.fscript.FScriptException;
import org.objectweb.fractal.fscript.InvalidScriptException;
import org.objectweb.fractal.fscript.ScriptLoader;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.Wrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongWrapper;
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
public class ExecutorControllerImpl extends AbstractPAComponentController implements ExecutorController {

	public static final String AGCMSCRIPT_ADL = "org.objectweb.proactive.extensions.autonomic.gcmscript.AGCMScript";
	private static final String MSG_NOT_SERIALIZABLE = "The result of action %s does not implement Serializable";
	private static final String MSG_NOT_FOUND = "No action found with name \"%s\"";
	
	// action name --> action
	private Map<String, Action> actions = new HashMap<String, Action>();

    private PAGCMTypeFactory patf;
    private PAGenericFactory pagf;

    private transient ScriptLoader loader;
    private transient FScriptEngine engine;

    // ----- ADL ------

    private void checkADLInitialized() throws ReconfigurationException {
        if ((this.loader == null) || (this.engine == null)) {
        	try {
                String defaultFcProvider = System.getProperty("fractal.provider");
                if (defaultFcProvider == null) {
                	defaultFcProvider = System.getProperty("gcm.provider");
                	if (defaultFcProvider == null) {
                		 throw new ReconfigurationException("Unable to find neither fractal nor gcm provier");
                	}
        		}

                System.setProperty("fractal.provider", "org.objectweb.fractal.julia.Julia");
                Component gcmScript = GCMScript.newEngineFromAdl(AGCMSCRIPT_ADL);
                this.loader = FScript.getScriptLoader(gcmScript);
                this.engine = FScript.getFScriptEngine(gcmScript);
                this.engine.setGlobalVariable("this", ((GCMNodeFactory) FScript.getNodeFactory(gcmScript))
                        .createGCMComponentNode(this.hostComponent));
                System.setProperty("fractal.provider", defaultFcProvider);
            } catch (Exception e) {
                throw new ReconfigurationException("Unable to set new engine for reconfiguration controller", e);
            }
        }
    }

    /** {@inheritDoc} */
	@Override
	public Set<String> load(String fileName) throws ReconfigurationException {
		checkADLInitialized();
		
        try {
            return this.loader.load(new FileReader(fileName));
        } catch (FileNotFoundException fnfe) {
            throw new ReconfigurationException("Unable to load procedure definitions", fnfe);
        } catch (InvalidScriptException ise) {
            throw new ReconfigurationException("Unable to load procedure definitions\n" + ise.getMessage());
        }
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getGlobals() throws ReconfigurationException {
		checkADLInitialized();
		return this.engine.getGlobals();
	}

	/** {@inheritDoc} */
	@Override
	public Wrapper<String> execute(String source) {
		try {

			checkADLInitialized();
			Object result = this.engine.execute(source);
			return new ValidWrapper<String>(result == null ? "(void)" : result.toString());

		} catch (ReconfigurationException | FScriptException re) {
			re.printStackTrace();
			return new WrongWrapper<String>("Fail to execute: " + source);
		}
	}

	// ----- API -----

	private void checkAPIInitialized() throws InstantiationException, NoSuchInterfaceException {
		if (patf == null || pagf == null) {
			Component boot = Utils.getBootstrapComponent();
			patf = Utils.getPAGCMTypeFactory(boot);
			pagf = Utils.getPAGenericFactory(boot);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAction(String name, Action action) {
		if (actions.containsKey(name)) return false;
		return actions.put(name, action) == null;
	}

	/** {@inheritDoc} */
	@Override
	public void removeAction(String name) {
		actions.remove(name);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends Serializable> Wrapper<T> executeAction(String actionName) {
		if (actions.containsKey(actionName)) {
			return executeAction(actions.get(actionName), actionName);
		}
		return new WrongWrapper<T>(String.format(MSG_NOT_FOUND, actionName));
	}

	/** {@inheritDoc} */
	@Override
	public <T extends Serializable> Wrapper<T> executeAction(Action action) {
		return executeAction(action, action.getClass().getCanonicalName());
	}

	@SuppressWarnings("unchecked")
	private <T extends Serializable> Wrapper<T> executeAction(Action action, String name) {
		try {
			checkAPIInitialized();
		} catch (InstantiationException | NoSuchInterfaceException e1) {
			return new WrongWrapper<T>("Fail to initialize the GCMScript engine");
		}
		
		Object result = action.execute(hostComponent, patf, pagf);

		if ( !(result instanceof Serializable) && result != null ) {
			return new WrongWrapper<T>((T) result.toString(), String.format(MSG_NOT_SERIALIZABLE, name));
		}

		return new ValidWrapper<T>((T) result);
	}

}
