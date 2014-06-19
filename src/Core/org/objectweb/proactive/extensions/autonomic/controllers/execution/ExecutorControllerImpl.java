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
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongObjectWrapper;
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

	private static final String AGCMSCRIPT_ADL = "org.objectweb.proactive.extensions.autonomic.gcmscript.AGCMScript";
	
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

	@Override
	public Set<String> getGlobals() throws ReconfigurationException {
		checkADLInitialized();
		return this.engine.getGlobals();
	}

	@Override
	public ObjectWrapper execute(String source) {
		try {
			checkADLInitialized();
			Object result = this.engine.execute(source);
			return new ValidObjectWrapper(result == null ? "" : result.toString());
		} catch (ReconfigurationException | FScriptException re) {
			re.printStackTrace();
			return new WrongObjectWrapper("Fail to execute [" + source + "]", re);
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

	@Override
	public boolean addAction(String name, Action action) {
		if (actions.containsKey(name)) return false;
		return actions.put(name, action) == null;
	}

	@Override
	public void removeAction(String name) {
		actions.remove(name);
	}

	@Override
	public ObjectWrapper executeAction(String actionName) {
		if (actions.containsKey(actionName)) {
			try {
				checkAPIInitialized();
				return new ValidObjectWrapper(actions.get(actionName).execute(hostComponent, patf, pagf));
			} catch (Exception e) {
				e.printStackTrace();
				return new WrongObjectWrapper("Fail to execute \"" + actionName + "\" action.", e);
			}
		}
		return new WrongObjectWrapper("Action name \"" + actionName + "\" not found.");
	}

	@Override
	public ObjectWrapper executeAction(Action action) {
		try {
			checkAPIInitialized();
			return new ValidObjectWrapper(action.execute(this.hostComponent, patf, pagf));
		} catch (Exception e) {
			e.printStackTrace();
			return new WrongObjectWrapper("Fail to execute action (" + action.getClass().toString() + ").", e);
		}
	}

}
