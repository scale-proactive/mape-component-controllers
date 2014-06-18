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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.ValidObjectWrapper;
import org.objectweb.proactive.extensions.autonomic.controllers.utils.WrongObjectWrapper;
import org.objectweb.proactive.extra.component.fscript.control.PAReconfigurationController;
import org.objectweb.proactive.extra.component.fscript.control.PAReconfigurationControllerImpl;
import org.objectweb.proactive.extra.component.fscript.exceptions.ReconfigurationException;

/**
 * Reconfiguration component embedding a PAGCMScript engine.
 * It's an adaptation of the the PAReconfigurationController added by Bastien.
 * 
 * @author cruz
 *
 */

@SuppressWarnings("serial")
public class ExecutorControllerImpl extends AbstractPAComponentController implements ExecutorController {

	// action name --> action
	private Map<String, Action> actions = new HashMap<String, Action>();

    private PAReconfigurationController reconfigurationObject;
    private PAGCMTypeFactory patf;
    private PAGenericFactory pagf;

    // ----- ADL ------

    private void checkReconfigurationObject() throws ReconfigurationException {
        if (reconfigurationObject == null) {
        	reconfigurationObject = new PAReconfigurationControllerImpl(this.hostComponent);
    		String fractalProvider = System.getProperty("fractal.provider");
    		if (fractalProvider == null) {
    			System.setProperty("fractal.provider", System.getProperty("gcm.provider"));
    		}
        }
    }

	@Override
	public void setNewEngineFromADL() throws ReconfigurationException {
		checkReconfigurationObject();
		reconfigurationObject.setNewEngineFromADL();
	}

	@Override
	public void setNewEngineFromADL(String adlFile) throws ReconfigurationException {
		checkReconfigurationObject();
		reconfigurationObject.setNewEngineFromADL(adlFile);
	}

	@Override
	public Set<String> load(String fileName) throws ReconfigurationException {
		checkReconfigurationObject();
		return reconfigurationObject.load(fileName);
	}

	@Override
	public Set<String> getGlobals() throws ReconfigurationException {
		checkReconfigurationObject();
		return reconfigurationObject.getGlobals();
	}

	@Override
	public ObjectWrapper execute(String source) {
		try {
			checkReconfigurationObject();
			return new ValidObjectWrapper(reconfigurationObject.execute(source));
		} catch (ReconfigurationException re) {
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
